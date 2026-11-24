package casestudy3;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Value Object representing an immutable ledger record in an account's history.
 * Invariant: Once created, cannot be mutated or deleted. Balance after entry is recorded.
 */
public final class LedgerEntry {
    private final String entryId;
    private final Instant timestamp;
    private final EntryType type;
    private final PositiveMoney amount;
    private final long balanceAfterMinor;
    private final String description;

    public LedgerEntry(EntryType type, PositiveMoney amount, long balanceAfterMinor, String description) {
        this.entryId = UUID.randomUUID().toString();
        this.timestamp = Instant.now();
        this.type = Objects.requireNonNull(type, "Entry type cannot be null");
        this.amount = Objects.requireNonNull(amount, "Amount cannot be null");
        this.balanceAfterMinor = balanceAfterMinor;
        this.description = Objects.requireNonNull(description, "Description cannot be null");
    }

    public String getEntryId() {
        return entryId;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public EntryType getType() {
        return type;
    }

    public PositiveMoney getAmount() {
        return amount;
    }

    public long getBalanceAfterMinor() {
        return balanceAfterMinor;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s | %s: %s | Resulting Balance Minor: %d | %s",
                entryId.substring(0, 8), timestamp, type, amount, balanceAfterMinor, description);
    }
}
