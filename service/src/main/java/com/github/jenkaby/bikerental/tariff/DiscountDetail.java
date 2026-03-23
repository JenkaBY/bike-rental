package com.github.jenkaby.bikerental.tariff;

import com.github.jenkaby.bikerental.shared.domain.model.vo.DiscountPercent;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;

public record DiscountDetail(
        DiscountPercent percent,
        Money amount
) {

    private static final DiscountDetail NONE = new DiscountDetail(DiscountPercent.zero(), Money.zero());

    public static DiscountDetail none() {
        return NONE;
    }
}
