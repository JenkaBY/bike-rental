package com.github.jenkaby.bikerental.componenttest.model;

import com.github.jenkaby.bikerental.tariff.web.query.dto.CostCalculationV2Request;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record CostCalculationV2RequestBuilder(
        Instant startAt,
        Integer plannedDurationMinutes,
        Integer discountPercent,
        Long specialTariffId,
        BigDecimal specialPrice
) {

    public CostCalculationV2Request toRequest(List<CostCalculationV2Request.EquipmentItemRequest> equipments) {
        return new CostCalculationV2Request(
                equipments,
                startAt,
                plannedDurationMinutes,
                discountPercent,
                specialTariffId,
                specialPrice
        );
    }
}
