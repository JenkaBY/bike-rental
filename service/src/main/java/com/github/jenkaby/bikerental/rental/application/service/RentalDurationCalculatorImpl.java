package com.github.jenkaby.bikerental.rental.application.service;

import com.github.jenkaby.bikerental.rental.domain.service.BaseRentalDurationResult;
import com.github.jenkaby.bikerental.rental.domain.service.RentalDurationCalculator;
import com.github.jenkaby.bikerental.rental.domain.service.RentalDurationResult;
import com.github.jenkaby.bikerental.shared.config.RentalProperties;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;


@Service
public class RentalDurationCalculatorImpl implements RentalDurationCalculator {

    private final RentalProperties properties;

    public RentalDurationCalculatorImpl(RentalProperties properties) {
        this.properties = properties;
    }

    public int getTimeIncrementMinutes() {
        return (int) properties.timeIncrement().toMinutes();
    }


    @Override
    public RentalDurationResult calculate(@NonNull LocalDateTime start, @NonNull LocalDateTime end) {
        // Calculate once
        Duration actualDuration = Duration.between(start, end);
        long actualMinutes = actualDuration.toMinutes();

        int increment = getTimeIncrementMinutes();
        int billableMinutes = (int) ((actualMinutes + increment - 1) / increment) * increment;

        return new BaseRentalDurationResult(
                billableMinutes,
                actualDuration
        );
    }
}
