package com.github.jenkaby.bikerental.tariff.application.util;

import com.github.jenkaby.bikerental.tariff.domain.model.TariffPeriod;
import org.springframework.stereotype.Component;

import java.time.Duration;


@Component
public class TariffPeriodSelector {

    private static final Duration HALF_HOUR_THRESHOLD = Duration.ofMinutes(30);
    private static final Duration FOUR_HOURS_THRESHOLD = Duration.ofHours(4);

    public TariffPeriod selectPeriod(Duration rentalDuration) {
        if (rentalDuration.compareTo(HALF_HOUR_THRESHOLD) <= 0) {
            return TariffPeriod.HALF_HOUR;
        } else if (rentalDuration.compareTo(FOUR_HOURS_THRESHOLD) < 0) {
            return TariffPeriod.HOUR;
        } else {
            return TariffPeriod.DAY;
        }
    }
}
