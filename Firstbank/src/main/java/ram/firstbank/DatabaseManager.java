package ram.firstbank;

import java.io.File;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.Year;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * MS Access persistence via UCanAccess 5.0.1 + jackcess 4.0.6 (from Maven central).
 *
 * UCanAccess does NOT support "CREATE TABLE IF NOT EXISTS" syntax.
 * We use DatabaseMetaData.getTables() to check existence before creating.
 *
 * DB file is created automatically at: ~/FirstBankUganda.accdb
 */
public class DatabaseManager {

    private static final Logger logger = Logger.getLogger(DatabaseManager.class.getName());

    private static final String DB_PATH = System.getProperty("user.home")
            + File.separator + "FirstBankUganda.accdb";

    private static final String JDBC_URL =
            "jdbc:ucanaccess://" + DB_PATH + ";newDatabaseVersion=V2010";

    private static final Map<String, String> BRANCH_CODES = new LinkedHashMap<>();
    static {
        BRANCH_CODES.put("Kampala", "KLA");
        BRANCH_CODES.put("Gulu",    "GUL");
        BRANCH_CODES.put("Mbarara", "MBA");
        BRANCH_CODES.put("Jinja",   "JIN");
        BRANCH_CODES.put("Mbale",   "MBL");
    }

    /** Creates tables if they don't exist. Called once at startup. */
    public static void initialiseDatabase() {
        try (Connection conn = DriverManager.getConnection(JDBC_URL)) {
            DatabaseMetaData meta = conn.getMetaData();

            // Accounts table
            if (!tableExists(meta, "Accounts")) {
                try (Statement st = conn.createStatement()) {
                    st.executeUpdate(
                        "CREATE TABLE Accounts ("
                        + "AccountNumber  TEXT(20)  NOT NULL PRIMARY KEY, "
                        + "FirstName      TEXT(30)  NOT NULL, "
                        + "LastName       TEXT(30)  NOT NULL, "
                        + "NIN            TEXT(14)  NOT NULL, "
                        + "Email          TEXT(100) NOT NULL, "
                        + "Phone          TEXT(15)  NOT NULL, "
                        + "DateOfBirth    TEXT(10)  NOT NULL, "
                        + "Age            INTEGER   NOT NULL, "
                        + "AccountType    TEXT(20)  NOT NULL, "
                        + "Branch         TEXT(20)  NOT NULL, "
                        + "OpeningDeposit DOUBLE    NOT NULL, "
                        + "SecondNIN      TEXT(14), "
                        + "CreatedAt      TEXT(25)  NOT NULL)"
                    );
                    logger.info("Created table: Accounts");
                }
            }

            // AcctCounters table
            if (!tableExists(meta, "AcctCounters")) {
                try (Statement st = conn.createStatement()) {
                    st.executeUpdate(
                        "CREATE TABLE AcctCounters ("
                        + "BranchCode TEXT(5)  NOT NULL, "
                        + "AcctYear   INTEGER  NOT NULL, "
                        + "LastSeq    INTEGER  NOT NULL, "
                        + "CONSTRAINT pk_counter PRIMARY KEY (BranchCode, AcctYear))"
                    );
                    logger.info("Created table: AcctCounters");
                }
            }

        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Database initialisation failed: " + ex.getMessage(), ex);
        }
    }

    /** Returns true if the named table exists in the Access DB. */
    private static boolean tableExists(DatabaseMetaData meta, String name) throws SQLException {
        try (ResultSet rs = meta.getTables(null, null, name.toUpperCase(), new String[]{"TABLE"})) {
            if (rs.next()) return true;
        }
        try (ResultSet rs = meta.getTables(null, null, name, new String[]{"TABLE"})) {
            return rs.next();
        }
    }

    /** Generates BRANCHCODE-YYYY-000001 sequential account numbers. */
    public static String generateAccountNumber(String branch) {
        String code = BRANCH_CODES.getOrDefault(branch, "UNK");
        int year = Year.now().getValue();

        try (Connection conn = DriverManager.getConnection(JDBC_URL)) {
            conn.setAutoCommit(false);
            int next;

            try (PreparedStatement sel = conn.prepareStatement(
                    "SELECT LastSeq FROM AcctCounters WHERE BranchCode=? AND AcctYear=?")) {
                sel.setString(1, code);
                sel.setInt(2, year);
                ResultSet rs = sel.executeQuery();

                if (rs.next()) {
                    next = rs.getInt(1) + 1;
                    try (PreparedStatement upd = conn.prepareStatement(
                            "UPDATE AcctCounters SET LastSeq=? WHERE BranchCode=? AND AcctYear=?")) {
                        upd.setInt(1, next);
                        upd.setString(2, code);
                        upd.setInt(3, year);
                        upd.executeUpdate();
                    }
                } else {
                    next = 1;
                    try (PreparedStatement ins = conn.prepareStatement(
                            "INSERT INTO AcctCounters (BranchCode, AcctYear, LastSeq) VALUES (?,?,?)")) {
                        ins.setString(1, code);
                        ins.setInt(2, year);
                        ins.setInt(3, next);
                        ins.executeUpdate();
                    }
                }
            }
            conn.commit();
            return String.format("%s-%d-%06d", code, year, next);

        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Account number generation failed: " + ex.getMessage(), ex);
            return String.format("%s-%d-%06d", code, year, (int)(System.currentTimeMillis() % 1_000_000));
        }
    }

    /** Saves a validated Account to the Accounts table. */
    public static boolean saveAccount(Account account) {
        String sql =
            "INSERT INTO Accounts (AccountNumber, FirstName, LastName, NIN, Email, Phone, "
          + "DateOfBirth, Age, AccountType, Branch, OpeningDeposit, SecondNIN, CreatedAt) "
          + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";

        try (Connection conn = DriverManager.getConnection(JDBC_URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1,  account.getAccountNumber());
            ps.setString(2,  account.getFirstName());
            ps.setString(3,  account.getLastName());
            ps.setString(4,  account.getNin());
            ps.setString(5,  account.getEmail());
            ps.setString(6,  account.getPhone());
            ps.setString(7,  account.getDateOfBirth());
            ps.setInt(8,     account.getAge());
            ps.setString(9,  account.getAccountType());
            ps.setString(10, account.getBranch());
            ps.setDouble(11, account.getOpeningDeposit());
            ps.setString(12, (account instanceof JointAccount)
                    ? ((JointAccount) account).getSecondNin() : null);
            ps.setString(13, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));

            ps.executeUpdate();
            return true;

        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Failed to save account: " + ex.getMessage(), ex);
            return false;
        }
    }

    /** Login lookup – finds by AccountNumber or NIN. */
    public static String[] findAccountByIdentifier(String identifier) {
        String sql =
            "SELECT AccountNumber, FirstName, LastName, AccountType, Branch, OpeningDeposit, NIN "
          + "FROM Accounts WHERE AccountNumber=? OR NIN=?";

        try (Connection conn = DriverManager.getConnection(JDBC_URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, identifier);
            ps.setString(2, identifier);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new String[]{
                    rs.getString("AccountNumber"),
                    rs.getString("FirstName"),
                    rs.getString("LastName"),
                    rs.getString("AccountType"),
                    rs.getString("Branch"),
                    String.valueOf(rs.getDouble("OpeningDeposit")),
                    rs.getString("NIN")
                };
            }
        } catch (SQLException ex) {
            logger.log(Level.WARNING, "Login lookup failed: " + ex.getMessage(), ex);
        }
        return null;
    }

    public static String getDbPath() { return DB_PATH; }
}
