package ram.firstbank;

// ─────────────────────────────────────────────────────────────────────────────
// SavingsAccount  – minimum 50,000 UGX, earns interest, no overdraft
// ─────────────────────────────────────────────────────────────────────────────
class SavingsAccount extends Account {
    public SavingsAccount(String firstName, String lastName, String nin, String email,
                          String phone, String dateOfBirth, int age, String branch,
                          double openingDeposit, String pin) {
        super(firstName, lastName, nin, email, phone, dateOfBirth, age, branch, openingDeposit, pin);
    }
    @Override public long   minimumDeposit() { return 50_000L; }
    @Override public String getAccountType() { return "Savings"; }
    @Override public String getSpecialRule()  { return "Earns interest, no overdraft"; }
}

// ─────────────────────────────────────────────────────────────────────────────
// CurrentAccount – minimum 200,000 UGX, overdraft allowed, no interest
// ─────────────────────────────────────────────────────────────────────────────
class CurrentAccount extends Account {
    public CurrentAccount(String firstName, String lastName, String nin, String email,
                          String phone, String dateOfBirth, int age, String branch,
                          double openingDeposit, String pin) {
        super(firstName, lastName, nin, email, phone, dateOfBirth, age, branch, openingDeposit, pin);
    }
    @Override public long   minimumDeposit() { return 200_000L; }
    @Override public String getAccountType() { return "Current"; }
    @Override public String getSpecialRule()  { return "Overdraft allowed, no interest"; }
}

// ─────────────────────────────────────────────────────────────────────────────
// FixedDepositAccount – minimum 1,000,000 UGX, locked term, highest interest
// ─────────────────────────────────────────────────────────────────────────────
class FixedDepositAccount extends Account {
    public FixedDepositAccount(String firstName, String lastName, String nin, String email,
                               String phone, String dateOfBirth, int age, String branch,
                               double openingDeposit, String pin) {
        super(firstName, lastName, nin, email, phone, dateOfBirth, age, branch, openingDeposit, pin);
    }
    @Override public long   minimumDeposit() { return 1_000_000L; }
    @Override public String getAccountType() { return "Fixed Deposit"; }
    @Override public String getSpecialRule()  { return "Locked term, highest interest"; }
}

// ─────────────────────────────────────────────────────────────────────────────
// StudentAccount – minimum 10,000 UGX, applicant age must be 18-25
// ─────────────────────────────────────────────────────────────────────────────
class StudentAccount extends Account {
    public StudentAccount(String firstName, String lastName, String nin, String email,
                          String phone, String dateOfBirth, int age, String branch,
                          double openingDeposit, String pin) {
        super(firstName, lastName, nin, email, phone, dateOfBirth, age, branch, openingDeposit, pin);
    }
    @Override public long   minimumDeposit() { return 10_000L; }
    @Override public String getAccountType() { return "Student"; }
    @Override public String getSpecialRule()  { return "Applicant age must be 18-25"; }
}

// ─────────────────────────────────────────────────────────────────────────────
// JointAccount – minimum 100,000 UGX, requires a second NIN
// ─────────────────────────────────────────────────────────────────────────────
class JointAccount extends Account {
    private String secondNin;

    public JointAccount(String firstName, String lastName, String nin, String email,
                        String phone, String dateOfBirth, int age, String branch,
                        double openingDeposit, String pin, String secondNin) {
        super(firstName, lastName, nin, email, phone, dateOfBirth, age, branch, openingDeposit, pin);
        this.secondNin = secondNin;
    }
    @Override public long   minimumDeposit() { return 100_000L; }
    @Override public String getAccountType() { return "Joint"; }
    @Override public String getSpecialRule()  { return "Requires a second NIN"; }
    public String getSecondNin() { return secondNin; }
}
