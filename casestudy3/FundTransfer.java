package casestudy3;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Entity representing a Fund Transfer operation across accounts.
 * Has its own identity, lifecycle states, and timestamp.
 */
public class FundTransfer {
    private final String transferId;
    private final AccountNumber sourceAccount;
    private final AccountNumber destinationAccount;
    private final PositiveMoney amount;
    private final Instant createdAt;
    private TransferStatus status;
    private String failureReason;

    public FundTransfer(AccountNumber sourceAccount, AccountNumber destinationAccount, PositiveMoney amount) {
        this.transferId = UUID.randomUUID().toString();
        this.sourceAccount = Objects.requireNonNull(sourceAccount, "Source account cannot be null");
        this.destinationAccount = Objects.requireNonNull(destinationAccount, "Destination account cannot be null");
        this.amount = Objects.requireNonNull(amount, "Amount cannot be null");
        if (sourceAccount.equals(destinationAccount)) {
            throw new IllegalArgumentException("Source and destination accounts cannot be identical");
        }
        this.createdAt = Instant.now();
        this.status = TransferStatus.INITIATED;
    }

    public String getTransferId() {
        return transferId;
    }

    public AccountNumber getSourceAccount() {
        return sourceAccount;
    }

    public AccountNumber getDestinationAccount() {
        return destinationAccount;
    }

    public PositiveMoney getAmount() {
        return amount;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public TransferStatus getStatus() {
        return status;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void markCompleted() {
        if (this.status != TransferStatus.INITIATED) {
            throw new IllegalStateException("Transfer cannot be completed from status: " + this.status);
        }
        this.status = TransferStatus.COMPLETED;
    }

    public void markFailed(String reason) {
        if (this.status != TransferStatus.INITIATED) {
            throw new IllegalStateException("Transfer cannot be failed from status: " + this.status);
        }
        this.status = TransferStatus.FAILED;
        this.failureReason = reason;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FundTransfer that)) return false;
        return transferId.equals(that.transferId);
    }

    @Override
    public int hashCode() {
        return transferId.hashCode();
    }

    @Override
    public String toString() {
        return String.format("FundTransfer[ID=%s, From=%s, To=%s, Amount=%s, Status=%s]",
                transferId.substring(0, 8), sourceAccount, destinationAccount, amount, status);
    }
}
