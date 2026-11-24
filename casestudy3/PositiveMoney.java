package casestudy3;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Value Object representing a strictly positive Monetary amount for financial transactions.
 * Invariant: minor amount > 0.
 * Zero or negative money transactions are made impossible to represent.
 */
public final class PositiveMoney implements Comparable<PositiveMoney> {
    private final long minorAmount;
    private final Currency currency;

    private PositiveMoney(long minorAmount, Currency currency) {
        if (minorAmount <= 0) {
            throw new IllegalArgumentException("Transaction amount must be strictly positive (> 0), received: " + minorAmount);
        }
        this.minorAmount = minorAmount;
        this.currency = Objects.requireNonNull(currency, "Currency cannot be null");
    }

    public static PositiveMoney ofMinor(long minorAmount, Currency currency) {
        return new PositiveMoney(minorAmount, currency);
    }

    public static PositiveMoney of(String decimalStr, Currency currency) {
        Objects.requireNonNull(decimalStr, "Amount string cannot be null");
        Objects.requireNonNull(currency, "Currency cannot be null");
        BigDecimal bd = new BigDecimal(decimalStr);
        if (bd.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be strictly positive: " + decimalStr);
        }
        BigDecimal scaled = bd.setScale(currency.getMinorUnits());
        long minor = scaled.movePointRight(currency.getMinorUnits()).longValueExact();
        return new PositiveMoney(minor, currency);
    }

    public long getMinorAmount() {
        return minorAmount;
    }

    public Currency getCurrency() {
        return currency;
    }

    public PositiveMoney plus(PositiveMoney other) {
        requireSameCurrency(other.currency);
        return new PositiveMoney(Math.addExact(this.minorAmount, other.minorAmount), currency);
    }

    public void requireSameCurrency(Currency otherCurrency) {
        if (this.currency != otherCurrency) {
            throw new IllegalArgumentException("Currency mismatch: " + this.currency + " vs " + otherCurrency);
        }
    }

    @Override
    public int compareTo(PositiveMoney o) {
        requireSameCurrency(o.currency);
        return Long.compare(this.minorAmount, o.minorAmount);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PositiveMoney that)) return false;
        return minorAmount == that.minorAmount && currency == that.currency;
    }

    @Override
    public int hashCode() {
        return Objects.hash(minorAmount, currency);
    }

    @Override
    public String toString() {
        BigDecimal bd = BigDecimal.valueOf(minorAmount).movePointLeft(currency.getMinorUnits());
        return bd.toPlainString() + " " + currency.name();
    }
}
