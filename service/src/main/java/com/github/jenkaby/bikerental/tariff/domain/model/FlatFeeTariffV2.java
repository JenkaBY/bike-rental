package com.github.jenkaby.bikerental.tariff.domain.model;

import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;
import com.github.jenkaby.bikerental.tariff.BreakdownCostDetails;
import com.github.jenkaby.bikerental.tariff.RentalCostV2;
import com.github.jenkaby.bikerental.tariff.domain.service.BaseRentalCostV2;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;

@Getter
public final class FlatFeeTariffV2 extends TariffV2 {

    private final Money issuanceFee;

    public FlatFeeTariffV2(Long id, String name, String description, String equipmentTypeSlug,
                           String version, LocalDate validFrom, LocalDate validTo, TariffV2Status status,
                           Money issuanceFee) {
        super(id, name, description, equipmentTypeSlug, PricingType.FLAT_FEE, version, validFrom, validTo, status);
        this.issuanceFee = issuanceFee;
    }

    @Override
    public RentalCostV2 calculateCost(Duration duration) {
        int durationMinutes = (int) duration.toMinutes();
        int days = durationMinutes <= 0 ? 1 : (int) Math.ceil((double) durationMinutes / MINUTES_PER_DAY);
        Money cost = issuanceFee.multiply(BigDecimal.valueOf(days));
        String message = String.format("Flat fee: %s*%dd = %s", issuanceFee, days, cost);
        return new BaseRentalCostV2(cost, new BreakdownCostDetails.FlatFee(message,
                new BreakdownCostDetails.FlatFee.Details(issuanceFee.toString(), days, cost.toString())));
    }
}
