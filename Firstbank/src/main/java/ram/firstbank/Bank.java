package ram.firstbank;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * NEW BANK ACCOUNT OPENING FORM – First Bank Uganda
 *
 * GUI layout defined via NetBeans Form Editor (Bank.form / initComponents()).
 * All business logic is implemented here without touching the generated block.
 *
 * Field mapping (NetBeans names → logical names):
 *   jTextField6   → First Name (Surname label = "Surname" in form)
 *   jTextField7   → Last Name
 *   jTextField8   → National ID (NIN)
 *   jTextField9   → Email
 *   jTextField10  → Confirm Email
 *   jComboBox3    → DOB Year
 *   jComboBox2    → DOB Month
 *   jComboBox1    → DOB Day
 *   jComboBox4    → Age (read-only derived)
 *   jTextField19  → Phone Number
 *   jPasswordField2 → PIN
 *   jPasswordField1 → Confirm PIN
 *   jComboBox5    → Account Type
 *   jComboBox6    → Branch
 *   jComboBox7    → Opening Deposit (text field backed by combo – repurposed as JTextField via init)
 *   jTextField21  → Second NIN (Joint only)
 *   jButton1      → Submit
 *   jButton3      → Reset
 *   jButton2      → Login (opens Login form)
 *   jTable1       → Minimum deposit info table
 */
public class Bank extends javax.swing.JFrame {

    private static final Logger logger = Logger.getLogger(Bank.class.getName());

    // ── Inline error labels (added programmatically, not in form) ────────────
    private JLabel errFirstName   = new JLabel();
    private JLabel errLastName    = new JLabel();
    private JLabel errNIN         = new JLabel();
    private JLabel errEmail       = new JLabel();
    private JLabel errConfirmEmail= new JLabel();
    private JLabel errPhone       = new JLabel();
    private JLabel errDOB         = new JLabel();
    private JLabel errPIN         = new JLabel();
    private JLabel errConfirmPIN  = new JLabel();
    private JLabel errDeposit     = new JLabel();
    private JLabel errSecondNIN   = new JLabel();

    // Summary display area (read-only)
    private JTextArea summaryArea = new JTextArea(4, 40);

    // Opening deposit field – we add it to jPanel2 after init
    private JTextField depositField = new JTextField(15);

    public Bank() {
        initComponents();
        postInit();
    }

    // =========================================================================
    // POST-INIT: populate combos, attach listeners, wire error labels
    // =========================================================================
    private void postInit() {
        setTitle("First Bank Uganda – New Account Opening Form");

        // ── Populate DOB Year combo (jComboBox3) ──────────────────────────────
        DefaultComboBoxModel<String> yearModel = new DefaultComboBoxModel<>();
        yearModel.addElement("-- Year --");
        int currentYear = LocalDate.now().getYear();
        for (int y = currentYear - 75; y <= currentYear - 18; y++) {
            yearModel.addElement(String.valueOf(y));
        }
        jComboBox3.setModel(yearModel);
        // FIX: set minimum sizes so 4-digit year and full month names are visible
        jComboBox3.setPreferredSize(new java.awt.Dimension(70, 22));
        jComboBox3.setMinimumSize(new java.awt.Dimension(70, 22));

        // ── Populate DOB Month combo (jComboBox2) ─────────────────────────────
        DefaultComboBoxModel<String> monthModel = new DefaultComboBoxModel<>();
        monthModel.addElement("-- Month --");
        String[] months = {"January","February","March","April","May","June",
                           "July","August","September","October","November","December"};
        for (String m : months) monthModel.addElement(m);
        jComboBox2.setModel(monthModel);
        jComboBox2.setPreferredSize(new java.awt.Dimension(100, 22));
        jComboBox2.setMinimumSize(new java.awt.Dimension(100, 22));

        // ── Populate DOB Day combo (jComboBox1) ───────────────────────────────
        populateDayCombo(31);
        jComboBox1.setPreferredSize(new java.awt.Dimension(55, 22));
        jComboBox1.setMinimumSize(new java.awt.Dimension(55, 22));

        // ── Age display (jComboBox4 repurposed as read-only label) ────────────
        jComboBox4.setModel(new DefaultComboBoxModel<>(new String[]{"Auto"}));
        jComboBox4.setEnabled(false);

        // ── Account Type combo (jComboBox5) ───────────────────────────────────
        jComboBox5.setModel(new DefaultComboBoxModel<>(new String[]{
            "-- Select --", "Savings", "Current", "Fixed Deposit", "Student", "Joint"
        }));

        // ── Branch combo (jComboBox6) ─────────────────────────────────────────
        jComboBox6.setModel(new DefaultComboBoxModel<>(new String[]{
            "-- Select --", "Kampala", "Gulu", "Mbarara", "Jinja", "Mbale"
        }));

        // ── Opening Deposit field (replace jComboBox7 usage) ─────────────────
        // jComboBox7 is the widget in jPanel2; we overlay a JTextField there.
        // We hide jComboBox7 and add depositField instead.
        jComboBox7.setVisible(false);
        jPanel2.setLayout(null);
        // Re-layout jPanel2 manually (keep existing labels visible)
        relayoutPanel2();

        // ── Minimum deposit table (jTable1) ───────────────────────────────────
        Object[][] tableData = {
            {"Savings",       "50,000",     "Earns interest, no overdraft"},
            {"Current",       "200,000",    "Overdraft allowed, no interest"},
            {"Fixed Deposit", "1,000,000",  "Locked term, highest interest"},
            {"Student",       "10,000",     "Applicant age must be 18-25"},
            {"Joint",         "100,000",    "Requires a second NIN"},
        };
        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            tableData,
            new String[]{"ACCOUNT TYPE","MINIMUM DEPOSIT (UGX)","SPECIAL RULE"}
        ) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        });

        // ── Summary area ──────────────────────────────────────────────────────
        summaryArea.setEditable(false);
        summaryArea.setLineWrap(true);
        summaryArea.setWrapStyleWord(true);
        summaryArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        summaryArea.setBorder(BorderFactory.createTitledBorder("Account Summary is Below:"));
        summaryArea.setBackground(new Color(240, 248, 255));
        JScrollPane summaryScroll = new JScrollPane(summaryArea);
        summaryScroll.setPreferredSize(new Dimension(480, 90));

        // Add summary below the main content area
        getContentPane().add(summaryScroll, BorderLayout.SOUTH);

        // ── Style error labels ────────────────────────────────────────────────
        for (JLabel err : new JLabel[]{errFirstName, errLastName, errNIN, errEmail,
                errConfirmEmail, errPhone, errDOB, errPIN, errConfirmPIN,
                errDeposit, errSecondNIN}) {
            err.setForeground(Color.RED);
            err.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        }

        // ── Listeners ─────────────────────────────────────────────────────────
        // DOB month/year change → update day count
        jComboBox2.addActionListener(e -> updateDayCombo());
        jComboBox3.addActionListener(e -> updateDayCombo());
        jComboBox1.addActionListener(e -> deriveAge());

        // Account type → toggle Second NIN visibility
        jComboBox5.addActionListener(e -> toggleSecondNin());

        // Submit
        jButton1.addActionListener(e -> handleSubmit());

        // Reset
        jButton3.addActionListener(e -> handleReset());

        // Login button → open Login screen
        jButton2.addActionListener(e -> {
            new Login().setVisible(true);
            this.dispose();
        });

        // Initialise database
        DatabaseManager.initialiseDatabase();

        // Initially hide second NIN panel
        jPanel3.setVisible(false);

        pack();
        setLocationRelativeTo(null);
    }

    // =========================================================================
    // Re-layout jPanel2 using absolute positioning so we can replace the combo
    // =========================================================================
    private void relayoutPanel2() {
        // Remove all and re-add with absolute layout
        jPanel2.removeAll();
        jPanel2.setLayout(null);

        JLabel titleLbl = new JLabel("ACCOUNT DETAILS");
        titleLbl.setForeground(new Color(0, 153, 255));
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        titleLbl.setBounds(80, 5, 180, 20);
        jPanel2.add(titleLbl);

        JLabel typeLabel = new JLabel("Account Type");
        typeLabel.setBounds(10, 32, 100, 22);
        jPanel2.add(typeLabel);
        jComboBox5.setBounds(120, 30, 161, 22);
        jPanel2.add(jComboBox5);

        JLabel branchLabel = new JLabel("Branch");
        branchLabel.setBounds(10, 60, 100, 22);
        jPanel2.add(branchLabel);
        jComboBox6.setBounds(120, 58, 161, 22);
        jPanel2.add(jComboBox6);

        JLabel depLabel = new JLabel("Opening Deposit");
        depLabel.setBounds(10, 88, 110, 22);
        jPanel2.add(depLabel);
        depositField.setBounds(120, 86, 161, 22);
        depositField.setToolTipText("Enter amount in UGX e.g. 50000");
        jPanel2.add(depositField);

        errDeposit.setBounds(120, 110, 200, 16);
        jPanel2.add(errDeposit);

        jPanel2.setPreferredSize(new Dimension(295, 135));
    }

    // =========================================================================
    // DOB helpers
    // =========================================================================
    private void populateDayCombo(int maxDay) {
        DefaultComboBoxModel<String> dayModel = new DefaultComboBoxModel<>();
        dayModel.addElement("-- Day --");
        for (int d = 1; d <= maxDay; d++) dayModel.addElement(String.valueOf(d));
        jComboBox1.setModel(dayModel);
    }

    private void updateDayCombo() {
        int month = jComboBox2.getSelectedIndex(); // 0 = placeholder
        String yearStr = (String) jComboBox3.getSelectedItem();
        if (month == 0 || yearStr == null || yearStr.startsWith("--")) {
            populateDayCombo(31);
            return;
        }
        int year;
        try { year = Integer.parseInt(yearStr); } catch (NumberFormatException e) { return; }
        int[] daysInMonth = {31, isLeapYear(year) ? 29 : 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
        String prevDay = (String) jComboBox1.getSelectedItem();
        populateDayCombo(daysInMonth[month - 1]);
        // Restore previous day if still valid
        if (prevDay != null && !prevDay.startsWith("--")) jComboBox1.setSelectedItem(prevDay);
        deriveAge();
    }

    private boolean isLeapYear(int year) {
        return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0);
    }

    private void deriveAge() {
        try {
            LocalDate dob = parseDOB();
            if (dob == null) { jComboBox4.setModel(new DefaultComboBoxModel<>(new String[]{"--"})); return; }
            int age = Period.between(dob, LocalDate.now()).getYears();
            jComboBox4.setModel(new DefaultComboBoxModel<>(new String[]{String.valueOf(age)}));
        } catch (Exception ignored) {}
    }

    private LocalDate parseDOB() {
        String yearStr  = (String) jComboBox3.getSelectedItem();
        int    monthIdx = jComboBox2.getSelectedIndex();
        String dayStr   = (String) jComboBox1.getSelectedItem();
        if (yearStr == null || yearStr.startsWith("--") || monthIdx == 0
                || dayStr == null || dayStr.startsWith("--")) return null;
        try {
            return LocalDate.of(Integer.parseInt(yearStr), monthIdx, Integer.parseInt(dayStr));
        } catch (Exception e) { return null; }
    }

    // =========================================================================
    // Toggle Second NIN panel
    // =========================================================================
    private void toggleSecondNin() {
        String selected = (String) jComboBox5.getSelectedItem();
        boolean isJoint = "Joint".equals(selected);
        jPanel3.setVisible(isJoint);
        pack();
    }

    // =========================================================================
    // SUBMIT
    // =========================================================================
    private void handleSubmit() {
        clearErrors();
        List<String> errors = new ArrayList<>();

        // ── First Name ────────────────────────────────────────────────────────
        String firstName = jTextField6.getText().trim();
        if (!isValidName(firstName)) {
            String msg = "First name: letters only, 2-30 chars";
            errFirstName.setText(msg); errors.add(msg);
        }

        // ── Last Name ─────────────────────────────────────────────────────────
        String lastName = jTextField7.getText().trim();
        if (!isValidName(lastName)) {
            String msg = "Last name: letters only, 2-30 chars";
            errLastName.setText(msg); errors.add(msg);
        }

        // ── NIN ───────────────────────────────────────────────────────────────
        String nin = jTextField8.getText().trim();
        if (!nin.matches("[A-Z0-9]{14}")) {
            String msg = "NIN: exactly 14 uppercase alphanumeric characters";
            errNIN.setText(msg); errors.add(msg);
        }

        // ── Email ─────────────────────────────────────────────────────────────
        String email        = jTextField9.getText().trim();
        String confirmEmail = jTextField10.getText().trim();
        if (!email.matches("^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$")) {
            String msg = "Email: invalid format";
            errEmail.setText(msg); errors.add(msg);
        } else if (!email.equalsIgnoreCase(confirmEmail)) {
            String msg = "Confirm email: emails do not match";
            errConfirmEmail.setText(msg); errors.add(msg);
        }

        // ── Phone ─────────────────────────────────────────────────────────────
        String phone = jTextField19.getText().trim();
        if (!phone.matches("\\+256\\d{9}")) {
            String msg = "Phone: must be +256XXXXXXXXX (12 digits)";
            errPhone.setText(msg); errors.add(msg);
        }

        // ── DOB & Age ─────────────────────────────────────────────────────────
        LocalDate dob = parseDOB();
        int age = 0;
        if (dob == null) {
            String msg = "Date of Birth: please select a valid date";
            errDOB.setText(msg); errors.add(msg);
        } else {
            age = Period.between(dob, LocalDate.now()).getYears();
            if (age < 18 || age > 75) {
                String msg = "Age must be between 18 and 75 (derived: " + age + ")";
                errDOB.setText(msg); errors.add(msg);
            }
        }

        // ── PIN ───────────────────────────────────────────────────────────────
        String pin        = new String(jPasswordField2.getPassword()).trim();
        String confirmPin = new String(jPasswordField1.getPassword()).trim();
        if (!pin.matches("\\d{4,6}")) {
            String msg = "PIN: 4-6 digits only";
            errPIN.setText(msg); errors.add(msg);
        } else if (pin.chars().distinct().count() == 1) {
            String msg = "PIN: must not be all identical digits (e.g. 0000)";
            errPIN.setText(msg); errors.add(msg);
        } else if (!pin.equals(confirmPin)) {
            String msg = "Confirm PIN: PINs do not match";
            errConfirmPIN.setText(msg); errors.add(msg);
        }

        // ── Account Type ──────────────────────────────────────────────────────
        String accountType = (String) jComboBox5.getSelectedItem();
        if (accountType == null || accountType.startsWith("--")) {
            String msg = "Account Type: please select one";
            errors.add(msg);
        }

        // ── Student age check ─────────────────────────────────────────────────
        if ("Student".equals(accountType) && dob != null && (age < 18 || age > 25)) {
            String msg = "Student account: applicant must be 18-25 (you are " + age + ")";
            errDOB.setText(msg); errors.add(msg);
        }

        // ── Branch ────────────────────────────────────────────────────────────
        String branch = (String) jComboBox6.getSelectedItem();
        if (branch == null || branch.startsWith("--")) {
            errors.add("Branch: please select one");
        }

        // ── Opening Deposit ───────────────────────────────────────────────────
        double deposit = 0;
        try {
            deposit = Double.parseDouble(depositField.getText().trim().replace(",", ""));
            if (deposit <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            String msg = "Opening Deposit: enter a valid positive number";
            errDeposit.setText(msg); errors.add(msg);
        }

        // Minimum deposit check (polymorphism via account subclass)
        if (errors.isEmpty() && accountType != null && !accountType.startsWith("--")) {
            long min = getMinimumDeposit(accountType);
            if (deposit < min) {
                String msg = String.format("Opening Deposit: minimum for %s is UGX %,d", accountType, min);
                errDeposit.setText(msg); errors.add(msg);
            }
        }

        // ── Second NIN (Joint only) ───────────────────────────────────────────
        String secondNin = "";
        if ("Joint".equals(accountType)) {
            secondNin = jTextField21.getText().trim();
            if (!secondNin.matches("[A-Z0-9]{14}")) {
                String msg = "Second NIN: exactly 14 uppercase alphanumeric characters";
                errSecondNIN.setText(msg); errors.add(msg);
            }
        }

        // ── Show errors or save ───────────────────────────────────────────────
        if (!errors.isEmpty()) {
            StringBuilder sb = new StringBuilder("Please fix the following:\n\n");
            for (String e : errors) sb.append("• ").append(e).append("\n");
            JOptionPane.showMessageDialog(this, sb.toString(),
                    "Validation Errors", JOptionPane.ERROR_MESSAGE);
            revalidate(); repaint();
            return;
        }

        // Build account using polymorphism
        String dobStr = dob.format(DateTimeFormatter.ISO_LOCAL_DATE);
        Account account = buildAccount(accountType, firstName, lastName, nin, email,
                phone, dobStr, age, branch, deposit, pin, secondNin);

        String accNumber = DatabaseManager.generateAccountNumber(branch);
        account.setAccountNumber(accNumber);

        boolean saved = DatabaseManager.saveAccount(account);
        String summary = account.toSummaryLine();
        summaryArea.append(summary + "\n");

        String msg = saved
            ? "Account created successfully!\n\n" + summary + "\n\nSaved to database: " + DatabaseManager.getDbPath()
            : "Account created but database save failed. Record:\n\n" + summary;

        JOptionPane.showMessageDialog(this, msg, "Account Created",
                saved ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE);
    }

    // =========================================================================
    // RESET
    // =========================================================================
    private void handleReset() {
        jTextField6.setText("");
        jTextField7.setText("");
        jTextField8.setText("");
        jTextField9.setText("");
        jTextField10.setText("");
        jTextField19.setText("");
        jPasswordField2.setText("");
        jPasswordField1.setText("");
        jTextField21.setText("");
        depositField.setText("");
        jComboBox3.setSelectedIndex(0);
        jComboBox2.setSelectedIndex(0);
        jComboBox1.setSelectedIndex(0);
        jComboBox5.setSelectedIndex(0);
        jComboBox6.setSelectedIndex(0);
        jComboBox4.setModel(new DefaultComboBoxModel<>(new String[]{"Auto"}));
        jPanel3.setVisible(false);
        clearErrors();
    }

    // =========================================================================
    // Helpers
    // =========================================================================
    private void clearErrors() {
        for (JLabel e : new JLabel[]{errFirstName, errLastName, errNIN, errEmail,
                errConfirmEmail, errPhone, errDOB, errPIN, errConfirmPIN,
                errDeposit, errSecondNIN}) {
            e.setText("");
        }
    }

    private boolean isValidName(String name) {
        return name != null && name.matches("[a-zA-Z]{2,30}");
    }

    private long getMinimumDeposit(String accountType) {
        return switch (accountType) {
            case "Savings"       -> 50_000L;
            case "Current"       -> 200_000L;
            case "Fixed Deposit" -> 1_000_000L;
            case "Student"       -> 10_000L;
            case "Joint"         -> 100_000L;
            default              -> 0L;
        };
    }

    /** Factory method – returns the correct Account subclass (polymorphism). */
    private Account buildAccount(String type, String fn, String ln, String nin,
                                 String email, String phone, String dob, int age,
                                 String branch, double deposit, String pin, String secondNin) {
        return switch (type) {
            case "Savings"       -> new SavingsAccount(fn, ln, nin, email, phone, dob, age, branch, deposit, pin);
            case "Current"       -> new CurrentAccount(fn, ln, nin, email, phone, dob, age, branch, deposit, pin);
            case "Fixed Deposit" -> new FixedDepositAccount(fn, ln, nin, email, phone, dob, age, branch, deposit, pin);
            case "Student"       -> new StudentAccount(fn, ln, nin, email, phone, dob, age, branch, deposit, pin);
            case "Joint"         -> new JointAccount(fn, ln, nin, email, phone, dob, age, branch, deposit, pin, secondNin);
            default              -> new SavingsAccount(fn, ln, nin, email, phone, dob, age, branch, deposit, pin);
        };
    }

    // =========================================================================
    // Generated initComponents (DO NOT MODIFY)
    // =========================================================================
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jTextField6 = new javax.swing.JTextField();
        jTextField7 = new javax.swing.JTextField();
        jTextField8 = new javax.swing.JTextField();
        jTextField9 = new javax.swing.JTextField();
        jTextField10 = new javax.swing.JTextField();
        jComboBox3 = new javax.swing.JComboBox<>();
        jComboBox2 = new javax.swing.JComboBox<>();
        jComboBox1 = new javax.swing.JComboBox<>();
        jComboBox4 = new javax.swing.JComboBox<>();
        jButton1 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        jTextField19 = new javax.swing.JTextField();
        jPasswordField2 = new javax.swing.JPasswordField();
        jPasswordField1 = new javax.swing.JPasswordField();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        jLabel20 = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        jButton2 = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jComboBox5 = new javax.swing.JComboBox<>();
        jComboBox6 = new javax.swing.JComboBox<>();
        jComboBox7 = new javax.swing.JComboBox<>();
        jLabel6 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jTextField21 = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jPanel6 = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setBackground(new java.awt.Color(0, 0, 255));

        jTextField6.addActionListener(this::jTextField6ActionPerformed);

        jTextField8.addActionListener(this::jTextField8ActionPerformed);

        jComboBox3.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jComboBox2.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jComboBox2.addActionListener(this::jComboBox2ActionPerformed);

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jComboBox1.addActionListener(this::jComboBox1ActionPerformed);

        jComboBox4.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jButton1.setBackground(new java.awt.Color(0, 204, 51));
        jButton1.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jButton1.setForeground(new java.awt.Color(255, 255, 255));
        jButton1.setText("Submit");

        jButton3.setBackground(new java.awt.Color(255, 0, 0));
        jButton3.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jButton3.setForeground(new java.awt.Color(255, 255, 255));
        jButton3.setText("Reset");

        jLabel5.setForeground(new java.awt.Color(0, 153, 255));
        jLabel5.setText("PERSONAL INFORMATION");

        jTextField19.addActionListener(this::jTextField19ActionPerformed);

        jLabel13.setText("Last Name");

        jLabel14.setText("National ID");

        jLabel15.setText("Email");

        jLabel16.setText("Confirm Email");

        jLabel12.setText("Surname");

        jLabel17.setText("Date of Birh");

        jLabel18.setText("Age");

        jLabel19.setText("Contact");

        jLabel20.setText("PIN");

        jLabel21.setText("Confirm PIN");

        jButton2.setBackground(new java.awt.Color(0, 0, 255));
        jButton2.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jButton2.setForeground(new java.awt.Color(255, 255, 255));
        jButton2.setText("Login");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel18, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel16, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel14, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel15, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel17, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jTextField6, javax.swing.GroupLayout.PREFERRED_SIZE, 197, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jTextField7, javax.swing.GroupLayout.PREFERRED_SIZE, 197, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jTextField8, javax.swing.GroupLayout.PREFERRED_SIZE, 197, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jTextField9, javax.swing.GroupLayout.PREFERRED_SIZE, 197, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jTextField10, javax.swing.GroupLayout.PREFERRED_SIZE, 197, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jComboBox3, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(6, 6, 6)
                                .addComponent(jComboBox2, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(6, 6, 6)
                                .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jComboBox4, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(96, 96, 96))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                        .addComponent(jLabel20, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                    .addComponent(jLabel21))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel19, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGap(53, 53, 53)))
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jPasswordField2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 134, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jPasswordField1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 134, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jTextField19, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 133, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(164, 164, 164))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jButton3)
                                .addGap(40, 40, 40)
                                .addComponent(jButton2)
                                .addGap(111, 111, 111))))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap(7, Short.MAX_VALUE)
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextField6, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel13)
                        .addGap(12, 12, 12)
                        .addComponent(jLabel14, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jTextField7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jTextField8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel15, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextField9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(3, 3, 3)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel16, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jComboBox3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jComboBox2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel17, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jComboBox4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel18))
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(9, 9, 9)
                        .addComponent(jLabel19, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextField19, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel20)
                    .addComponent(jPasswordField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(jPasswordField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jButton1)
                            .addComponent(jButton3)
                            .addComponent(jButton2)))
                    .addComponent(jLabel21))
                .addGap(29, 29, 29))
        );

        jComboBox5.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jComboBox5.addActionListener(this::jComboBox5ActionPerformed);

        jComboBox6.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jComboBox6.addActionListener(this::jComboBox6ActionPerformed);

        jComboBox7.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jLabel6.setForeground(new java.awt.Color(0, 153, 255));
        jLabel6.setText("ACCOUNT DETAILS");

        jLabel8.setText("Account Type");

        jLabel9.setText("Branch");

        jLabel10.setText("Opening Deposit");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(jLabel10, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel9, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                        .addGap(36, 36, 36)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jComboBox5, javax.swing.GroupLayout.PREFERRED_SIZE, 161, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jComboBox6, javax.swing.GroupLayout.PREFERRED_SIZE, 159, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jComboBox7, javax.swing.GroupLayout.PREFERRED_SIZE, 159, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(120, 120, 120)
                        .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 133, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jComboBox5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jComboBox6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel9))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jComboBox7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel10))
                .addContainerGap())
        );

        jTextField21.addActionListener(this::jTextField21ActionPerformed);

        jLabel4.setForeground(new java.awt.Color(51, 153, 255));
        jLabel4.setText("JOINT ACCOUNT(IF ONLY ACCOUNT = JOINT)");

        jLabel11.setText("Second NIN");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(32, 32, 32)
                        .addComponent(jTextField21, javax.swing.GroupLayout.PREFERRED_SIZE, 173, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 69, Short.MAX_VALUE))
                    .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jTextField21)
                    .addComponent(jLabel11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(16, 16, 16))
        );

        jPanel4.setBackground(new java.awt.Color(0, 0, 204));

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setText("NEW BANK ACCOUNT OPENNING FORM");
        jLabel2.setVerticalAlignment(javax.swing.SwingConstants.TOP);

        jLabel3.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(255, 255, 255));
        jLabel3.setText("It's Possible");

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("FIRST BANK UGANDA");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 141, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel2)
                .addGap(153, 153, 153)
                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 74, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(20, 20, 20))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addGap(0, 7, Short.MAX_VALUE)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        jLabel7.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        jLabel7.setForeground(new java.awt.Color(0, 153, 255));
        jLabel7.setText("MINIMUM OPENING DEPOSIT BY ACCOUNT TYPE");

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String [] {
                "ACCOUNT TYPE", "MINIMUM DEPOSIT(UGX)", "SPECIAL RULE"
            }
        ));
        jScrollPane1.setViewportView(jTable1);

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGap(45, 45, 45)
                .addComponent(jLabel7)
                .addContainerGap(218, Short.MAX_VALUE))
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 482, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 111, Short.MAX_VALUE)
                .addGap(15, 15, 15))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(9, 9, 9)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 377, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(98, 98, 98)
                                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                        .addGap(28, 28, 28))))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(30, 30, 30)
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(98, 98, 98)
                        .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jTextField6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField6ActionPerformed
    }//GEN-LAST:event_jTextField6ActionPerformed

    private void jTextField8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField8ActionPerformed
    }//GEN-LAST:event_jTextField8ActionPerformed

    private void jComboBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox1ActionPerformed
    }//GEN-LAST:event_jComboBox1ActionPerformed

    private void jTextField19ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField19ActionPerformed
    }//GEN-LAST:event_jTextField19ActionPerformed

    private void jTextField21ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField21ActionPerformed
    }//GEN-LAST:event_jTextField21ActionPerformed

    private void jComboBox6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox6ActionPerformed
    }//GEN-LAST:event_jComboBox6ActionPerformed

    private void jComboBox5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox5ActionPerformed
    }//GEN-LAST:event_jComboBox5ActionPerformed

    private void jComboBox2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox2ActionPerformed
    }//GEN-LAST:event_jComboBox2ActionPerformed

    public static void main(String args[]) {
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        java.awt.EventQueue.invokeLater(() -> new Bank().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JComboBox<String> jComboBox1;
    private javax.swing.JComboBox<String> jComboBox2;
    private javax.swing.JComboBox<String> jComboBox3;
    private javax.swing.JComboBox<String> jComboBox4;
    private javax.swing.JComboBox<String> jComboBox5;
    private javax.swing.JComboBox<String> jComboBox6;
    private javax.swing.JComboBox<String> jComboBox7;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPasswordField jPasswordField1;
    private javax.swing.JPasswordField jPasswordField2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JTextField jTextField10;
    private javax.swing.JTextField jTextField19;
    private javax.swing.JTextField jTextField21;
    private javax.swing.JTextField jTextField6;
    private javax.swing.JTextField jTextField7;
    private javax.swing.JTextField jTextField8;
    private javax.swing.JTextField jTextField9;
    // End of variables declaration//GEN-END:variables
}
