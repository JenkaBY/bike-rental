package com.github.jenkaby.bikerental.shared.domain.model.vo;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Value object representing a discount percentage (0-100) as BigDecimal.
 */
public record DiscountPercent(BigDecimal percent) {

    private static final DiscountPercent ZERO_DISCOUNT = new DiscountPercent(BigDecimal.ZERO);
    private static final BigDecimal ONE_HUNDRED = new BigDecimal(100);

    public DiscountPercent {
        if (percent == null) {
            throw new IllegalArgumentException("Discount percent cannot be null");
        }
        if (percent.stripTrailingZeros().scale() > 0) {
            throw new IllegalArgumentException("Discount percent must not contain fractional digits");
        }
        if (percent.compareTo(BigDecimal.ZERO) < 0 || percent.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new IllegalArgumentException("Discount percent must be between 0 and 100");
        }
    }

    public static DiscountPercent of(Integer percent) {
        return new DiscountPercent(new BigDecimal(percent));
    }

    public static DiscountPercent zero() {
        return ZERO_DISCOUNT;
    }

    public Money multiply(Money amountToBeDiscounted) {
        return amountToBeDiscounted.multiply(getDecimals());
    }

    private BigDecimal getDecimals() {
        return percent.divide(ONE_HUNDRED, 2, RoundingMode.HALF_UP);
    }
}

