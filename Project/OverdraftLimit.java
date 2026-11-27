package casestudy3;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Value Object representing an Account Overdraft Limit.
 * Invariant: Limit minor units >= 0. Overdraft cannot be negative.
 */
public final class OverdraftLimit {
    private final long minorLimit;
    private final Currency currency;

    private OverdraftLimit(long minorLimit, Currency currency) {
        if (minorLimit < 0) {
            throw new IllegalArgumentException("Overdraft limit cannot be negative: " + minorLimit);
        }
        this.minorLimit = minorLimit;
        this.currency = Objects.requireNonNull(currency, "Currency cannot be null");
    }

    public static OverdraftLimit ofMinor(long minorLimit, Currency currency) {
        return new OverdraftLimit(minorLimit, currency);
    }

    public static OverdraftLimit of(String decimalStr, Currency currency) {
        Objects.requireNonNull(decimalStr, "Amount string cannot be null");
        Objects.requireNonNull(currency, "Currency cannot be null");
        BigDecimal bd = new BigDecimal(decimalStr);
        if (bd.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Overdraft limit cannot be negative: " + decimalStr);
        }
        BigDecimal scaled = bd.setScale(currency.getMinorUnits());
        long minor = scaled.movePointRight(currency.getMinorUnits()).longValueExact();
        return new OverdraftLimit(minor, currency);
    }

    public static OverdraftLimit none(Currency currency) {
        return new OverdraftLimit(0, currency);
    }

    public long getMinorLimit() {
        return minorLimit;
    }

    public Currency getCurrency() {
        return currency;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OverdraftLimit that)) return false;
        return minorLimit == that.minorLimit && currency == that.currency;
    }

    @Override
    public int hashCode() {
        return Objects.hash(minorLimit, currency);
    }

    @Override
    public String toString() {
        BigDecimal bd = BigDecimal.valueOf(minorLimit).movePointLeft(currency.getMinorUnits());
        return bd.toPlainString() + " " + currency.name();
    }
}
