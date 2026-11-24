package casestudy3;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Value Object representing a Bank Account Number.
 * Invariant: Must match bank format (e.g., 10-16 alphanumeric characters), non-blank, non-null.
 */
public final class AccountNumber {
    private static final Pattern PATTERN = Pattern.compile("^[A-Z0-9]{8,16}$");
    private final String value;

    private AccountNumber(String value) {
        this.value = value;
    }

    public static AccountNumber of(String value) {
        if (value == null || !PATTERN.matcher(value.trim()).matches()) {
            throw new IllegalArgumentException("Invalid account number format: " + value + ". Must be 8-16 alphanumeric characters.");
        }
        return new AccountNumber(value.trim());
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AccountNumber that)) return false;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return value;
    }
}
