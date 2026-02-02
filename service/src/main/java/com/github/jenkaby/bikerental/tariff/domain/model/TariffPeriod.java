package com.github.jenkaby.bikerental.tariff.domain.model;

import lombok.Getter;

import java.time.Duration;

@Getter
public enum TariffPeriod {
    HALF_HOUR(Duration.ofMinutes(30)),
    HOUR(Duration.ofHours(1)),
    DAY(Duration.ofDays(1));

    private final Duration duration;

    TariffPeriod(Duration duration) {
        this.duration = duration;
    }

    public long getMinutes() {
        return duration.toMinutes();
    }
}
