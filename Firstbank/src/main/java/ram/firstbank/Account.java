package ram.firstbank;

/**
 * Abstract base class representing a bank account.
 * Defines common state and enforces the OOP contract via abstract method.
 */
public abstract class Account {

    private String firstName;
    private String lastName;
    private String nin;
    private String email;
    private String phone;
    private String dateOfBirth;
    private int age;
    private String branch;
    private double openingDeposit;
    private String accountNumber;
    private String pin;

    public Account(String firstName, String lastName, String nin, String email,
                   String phone, String dateOfBirth, int age, String branch,
                   double openingDeposit, String pin) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.nin = nin;
        this.email = email;
        this.phone = phone;
        this.dateOfBirth = dateOfBirth;
        this.age = age;
        this.branch = branch;
        this.openingDeposit = openingDeposit;
        this.pin = pin;
    }

    /** Returns the minimum opening deposit required for this account type. */
    public abstract long minimumDeposit();

    /** Returns the account type name (e.g. "Savings"). */
    public abstract String getAccountType();

    /** Returns a special rule note for this account type. */
    public abstract String getSpecialRule();

    // ── Getters ──────────────────────────────────────────────────────────────

    public String getFirstName()      { return firstName; }
    public String getLastName()       { return lastName; }
    public String getNin()            { return nin; }
    public String getEmail()          { return email; }
    public String getPhone()          { return phone; }
    public String getDateOfBirth()    { return dateOfBirth; }
    public int    getAge()            { return age; }
    public String getBranch()         { return branch; }
    public double getOpeningDeposit() { return openingDeposit; }
    public String getAccountNumber()  { return accountNumber; }
    public String getPin()            { return pin; }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    /**
     * Builds the formatted summary line shown in the read-only area.
     * Format: ACC: KLA-2026-000142 | Okello Allan | Savings | Kampala | DOB 2004-02-29 | +256... | Deposit 50,000 | email
     */
    public String toSummaryLine() {
        return String.format("ACC: %s | %s %s | %s | %s | DOB %s | %s | Deposit %,d | %s",
                accountNumber,
                firstName, lastName,
                getAccountType(),
                branch,
                dateOfBirth,
                phone,
                (long) openingDeposit,
                email.toLowerCase());
    }
}
