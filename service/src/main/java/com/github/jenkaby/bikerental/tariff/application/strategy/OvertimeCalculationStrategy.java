package com.github.jenkaby.bikerental.tariff.application.strategy;

import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;
import com.github.jenkaby.bikerental.tariff.domain.model.TariffPeriod;
import org.jspecify.annotations.NonNull;


public interface OvertimeCalculationStrategy {

    @NonNull
    Money calculateOvertimeCost(
            @NonNull Money basePrice,
            @NonNull TariffPeriod period,
            int overtimeMinutes,
            int forgivenMinutes);
}
