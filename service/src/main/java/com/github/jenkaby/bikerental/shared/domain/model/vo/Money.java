package com.github.jenkaby.bikerental.shared.domain.model.vo;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record Money(BigDecimal amount) implements Comparable<Money> {
    private static final int MONEY_SCALE = 2;

    private static final RoundingMode MONEY_ROUNDING_MODE = RoundingMode.HALF_UP;
    public Money {
        if (amount == null) {
            throw new IllegalArgumentException("Amount cannot be null");
        }
        if (amount.scale() > MONEY_SCALE) {
            throw new IllegalArgumentException("Amount cannot have more than " + MONEY_SCALE + " decimal places");
        }
    }

    /**
     * Creates Money from BigDecimal, automatically rounding to 2 decimal places if needed.
     *
     * @param amount amount to create Money from
     * @return Money instance with rounded amount
     */
    public static Money of(BigDecimal amount) {
        if (amount == null) {
            throw new IllegalArgumentException("Amount cannot be null");
        }
        BigDecimal roundedAmount = amount.scale() > MONEY_SCALE
                ? amount.setScale(MONEY_SCALE, MONEY_ROUNDING_MODE)
                : amount;
        return new Money(roundedAmount);
    }

    public static Money of(String amount) {
        return new Money(new BigDecimal(amount));
    }

    public static Money zero() {
        return new Money(BigDecimal.ZERO);
    }

    public boolean isPositive() {
        return amount.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean isNegative() {
        return amount.compareTo(BigDecimal.ZERO) < 0;
    }

    public boolean isZero() {
        return amount.compareTo(BigDecimal.ZERO) == 0;
    }

    public boolean isNegativeOrZero() {
        return amount.compareTo(BigDecimal.ZERO) <= 0;
    }

    public Money abs() {
        return new Money(amount.abs());
    }

    public boolean isMoreThan(Money other) {
        return this.compareTo(other) > 0;
    }

    public boolean isLessThan(Money other) {
        return this.compareTo(other) < 0;
    }

    public Money add(Money other) {
        return new Money(this.amount.add(other.amount));
    }

    public Money subtract(Money other) {
        return new Money(this.amount.subtract(other.amount));
    }

    public Money multiply(BigDecimal multiplier) {
        return Money.of(this.amount.multiply(multiplier));
    }

    public Money divide(int divisor) {
        return Money.of(amount.divide(BigDecimal.valueOf(divisor), MONEY_SCALE, MONEY_ROUNDING_MODE));
    }

    @Override
    public String toString() {
        return amount.stripTrailingZeros().toPlainString();
    }

    @Override
    public int compareTo(Money o) {
        return this.amount.compareTo(o.amount);
    }
}
