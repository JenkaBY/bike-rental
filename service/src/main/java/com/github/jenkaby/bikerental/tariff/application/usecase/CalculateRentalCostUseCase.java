package com.github.jenkaby.bikerental.tariff.application.usecase;

import com.github.jenkaby.bikerental.tariff.RentalCost;
import org.jspecify.annotations.NonNull;

import java.time.Duration;


public interface CalculateRentalCostUseCase {


    @NonNull
    RentalCost execute(@NonNull CalculateRentalCostCommand command);

    record CalculateRentalCostCommand(
            @NonNull Long tariffId,
            @NonNull Duration actualDuration,
            int billableMinutes,
            @NonNull Duration plannedDuration
    ) {
    }
}
