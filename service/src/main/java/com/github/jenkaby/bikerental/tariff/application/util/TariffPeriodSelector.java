package com.github.jenkaby.bikerental.tariff.application.util;

import com.github.jenkaby.bikerental.tariff.domain.model.TariffPeriod;
import org.springframework.stereotype.Component;

import java.time.Duration;


@Component
public class TariffPeriodSelector {


    public Duration selectPeriodDuration(Duration rentalDuration) {
        Duration halfHour = TariffPeriod.HALF_HOUR.getDuration();
        Duration hour = TariffPeriod.HOUR.getDuration();
        Duration day = TariffPeriod.DAY.getDuration();

        if (rentalDuration.compareTo(halfHour) <= 0) {
            return halfHour;
        } else if (rentalDuration.compareTo(hour) <= 0) {
            return hour;
        } else {
            return day;
        }
    }
}
