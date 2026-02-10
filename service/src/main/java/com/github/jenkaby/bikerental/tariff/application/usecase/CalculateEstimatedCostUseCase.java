package com.github.jenkaby.bikerental.tariff.application.usecase;

import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;

import java.time.Duration;

public interface CalculateEstimatedCostUseCase {
    Money execute(CalculateEstimatedCostCommand command);

    record CalculateEstimatedCostCommand(
            Long tariffId,
            Duration duration
    ) {
    }
}
