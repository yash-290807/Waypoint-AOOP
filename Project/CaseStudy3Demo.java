package casestudy3;

public class CaseStudy3Demo {
    public static void main(String[] args) {
        System.out.println("=== CASE STUDY 3: BANK ACCOUNT & MONEY TRANSFERS DEMO ===");

        AccountNumber accNumA = AccountNumber.of("ACCN100001");
        AccountNumber accNumB = AccountNumber.of("ACCN200002");

        BankAccount accA = new BankAccount(accNumA, "Alice Sharma", Currency.INR, OverdraftLimit.of("5000.00", Currency.INR));
        BankAccount accB = new BankAccount(accNumB, "Bob Verma", Currency.INR, OverdraftLimit.none(Currency.INR));

        System.out.println("Initial State:");
        System.out.println("  " + accA);
        System.out.println("  " + accB);

        // Deposit into Account A
        accA.deposit(PositiveMoney.of("10000.00", Currency.INR), "Salary Deposit");
        System.out.println("\nDeposited 10,000 INR into Alice's account -> Balance: " + accA.getFormattedBalance());

        // Perform Transfer of 5,000 INR from A to B
        TransferService transferService = new TransferService();
        System.out.println("\nExecuting Transfer of 5,000 INR from Alice to Bob...");
        FundTransfer ft = transferService.executeTransfer(accA, accB, PositiveMoney.of("5000.00", Currency.INR));
        System.out.println("Transfer Result: " + ft);
        System.out.println("  Alice's Balance: " + accA.getFormattedBalance());
        System.out.println("  Bob's Balance: " + accB.getFormattedBalance());

        // Test Overdraft Limit Enforcement
        System.out.println("\n--- Testing Overdraft Protection ---");
        // Alice has 5,000 balance + 5,000 overdraft limit = 10,000 max withdrawal capacity
        System.out.println("Attempting to withdraw 12,000 INR from Alice (Exceeds 5,000 bal + 5,000 overdraft)...");
        try {
            accA.withdraw(PositiveMoney.of("12000.00", Currency.INR), "ATM Cash Withdrawal");
        } catch (IllegalStateException e) {
            System.out.println("Successfully blocked: " + e.getMessage());
        }

        // Test Frozen Account Protection
        System.out.println("\n--- Testing Frozen Account Protection ---");
        accB.freeze();
        System.out.println("Frozen Bob's account: " + accB.getStatus());
        try {
            System.out.println("Attempting to withdraw from Bob's frozen account...");
            accB.withdraw(PositiveMoney.of("1000.00", Currency.INR), "ATM Withdrawal");
        } catch (IllegalStateException e) {
            System.out.println("Successfully blocked: " + e.getMessage());
        }

        // Test Zero or Negative Amount Rejection
        System.out.println("\n--- Testing Zero/Negative Rejection ---");
        try {
            System.out.println("Attempting to create zero PositiveMoney...");
            PositiveMoney.of("0.00", Currency.INR);
        } catch (IllegalArgumentException e) {
            System.out.println("Successfully blocked: " + e.getMessage());
        }

        System.out.println("\n--- Alice's Ledger History ---");
        for (LedgerEntry entry : accA.getLedger()) {
            System.out.println("  " + entry);
        }

        System.out.println("\n=== Case Study 3 Verified Successfully! ===");
    }
}
