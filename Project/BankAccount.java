package casestudy3;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Aggregate Root representing a Bank Account.
 * Invariants Enforced:
 * 1. Balance = sum of all historical ledger entries (minor units).
 * 2. Withdrawals strictly prohibited when account is FROZEN or CLOSED.
 * 3. Deposits strictly prohibited when account is CLOSED.
 * 4. Account balance cannot drop below (-overdraftLimit).
 * 5. All monetary operations use PositiveMoney (zero and negative amounts rejected at type level).
 * 6. Fields are encapsulated; internal state is strictly protected.
 */
public class BankAccount {
    private final AccountNumber accountNumber;
    private final Currency currency;
    private final String ownerName;
    private long balanceMinor; // in minor units
    private OverdraftLimit overdraftLimit;
    private AccountStatus status;
    private final List<LedgerEntry> ledger = new ArrayList<>();

    public BankAccount(AccountNumber accountNumber, String ownerName, Currency currency, OverdraftLimit overdraftLimit) {
        this.accountNumber = Objects.requireNonNull(accountNumber, "Account number cannot be null");
        if (ownerName == null || ownerName.isBlank()) {
            throw new IllegalArgumentException("Owner name cannot be blank");
        }
        this.ownerName = ownerName;
        this.currency = Objects.requireNonNull(currency, "Currency cannot be null");
        this.overdraftLimit = Objects.requireNonNull(overdraftLimit, "Overdraft limit cannot be null");
        if (overdraftLimit.getCurrency() != currency) {
            throw new IllegalArgumentException("Overdraft limit currency must match account currency");
        }
        this.balanceMinor = 0;
        this.status = AccountStatus.ACTIVE;
    }

    public AccountNumber getAccountNumber() {
        return accountNumber;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public Currency getCurrency() {
        return currency;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public OverdraftLimit getOverdraftLimit() {
        return overdraftLimit;
    }

    public synchronized String getFormattedBalance() {
        BigDecimal bd = BigDecimal.valueOf(balanceMinor).movePointLeft(currency.getMinorUnits());
        return bd.toPlainString() + " " + currency.name();
    }

    public synchronized long getBalanceMinor() {
        return balanceMinor;
    }

    public synchronized List<LedgerEntry> getLedger() {
        return Collections.unmodifiableList(new ArrayList<>(ledger));
    }

    public synchronized void setOverdraftLimit(OverdraftLimit newLimit) {
        Objects.requireNonNull(newLimit, "Overdraft limit cannot be null");
        if (newLimit.getCurrency() != this.currency) {
            throw new IllegalArgumentException("Overdraft limit currency must match account currency");
        }
        if (balanceMinor < -newLimit.getMinorLimit()) {
            throw new IllegalStateException("Cannot lower overdraft limit below current negative balance debt");
        }
        this.overdraftLimit = newLimit;
    }

    public synchronized void freeze() {
        if (this.status == AccountStatus.CLOSED) {
            throw new IllegalStateException("Cannot freeze an already closed account");
        }
        this.status = AccountStatus.FROZEN;
    }

    public synchronized void unfreeze() {
        if (this.status == AccountStatus.CLOSED) {
            throw new IllegalStateException("Cannot unfreeze a closed account");
        }
        this.status = AccountStatus.ACTIVE;
    }

    public synchronized void close() {
        if (this.balanceMinor != 0) {
            throw new IllegalStateException("Cannot close account with non-zero balance: " + getFormattedBalance());
        }
        this.status = AccountStatus.CLOSED;
    }

    public synchronized void deposit(PositiveMoney amount, String description) {
        Objects.requireNonNull(amount, "Deposit amount cannot be null");
        amount.requireSameCurrency(this.currency);
        if (status == AccountStatus.CLOSED) {
            throw new IllegalStateException("Cannot deposit into a closed account");
        }

        this.balanceMinor = Math.addExact(this.balanceMinor, amount.getMinorAmount());
        ledger.add(new LedgerEntry(EntryType.DEPOSIT, amount, this.balanceMinor, description));
    }

    public synchronized void withdraw(PositiveMoney amount, String description) {
        Objects.requireNonNull(amount, "Withdrawal amount cannot be null");
        amount.requireSameCurrency(this.currency);

        if (status == AccountStatus.FROZEN) {
            throw new IllegalStateException("Account " + accountNumber + " is FROZEN. Withdrawals are not permitted.");
        }
        if (status == AccountStatus.CLOSED) {
            throw new IllegalStateException("Account " + accountNumber + " is CLOSED. Withdrawals are not permitted.");
        }

        long potentialBalance = this.balanceMinor - amount.getMinorAmount();
        long maxPermittedDebt = -overdraftLimit.getMinorLimit();

        if (potentialBalance < maxPermittedDebt) {
            throw new IllegalStateException(String.format(
                    "Withdrawal rejected! Exceeds overdraft limit. Current balance: %s, Overdraft limit: %s, Requested: %s",
                    getFormattedBalance(), overdraftLimit, amount));
        }

        this.balanceMinor = potentialBalance;
        ledger.add(new LedgerEntry(EntryType.WITHDRAWAL, amount, this.balanceMinor, description));
    }

    // Package-private methods for atomic fund transfers
    synchronized void transferOut(PositiveMoney amount, AccountNumber toAccount, String transferId) {
        withdraw(amount, "Transfer to " + toAccount + " [Ref: " + transferId + "]");
    }

    synchronized void transferIn(PositiveMoney amount, AccountNumber fromAccount, String transferId) {
        deposit(amount, "Transfer from " + fromAccount + " [Ref: " + transferId + "]");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BankAccount that)) return false;
        return accountNumber.equals(that.accountNumber);
    }

    @Override
    public int hashCode() {
        return accountNumber.hashCode();
    }

    @Override
    public String toString() {
        return String.format("BankAccount[No=%s, Owner=%s, Balance=%s, Status=%s]",
                accountNumber, ownerName, getFormattedBalance(), status);
    }
}
