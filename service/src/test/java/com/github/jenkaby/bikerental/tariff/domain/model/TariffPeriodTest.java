package com.github.jenkaby.bikerental.tariff.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TariffPeriod Enum Tests")
class TariffPeriodTest {

    @Test
    @DisplayName("Should have HALF_HOUR with 30 minutes duration")
    void shouldHaveHalfHourWith30Minutes() {
        TariffPeriod period = TariffPeriod.HALF_HOUR;

        assertThat(period.getDuration()).isEqualTo(Duration.ofMinutes(30));
        assertThat(period.getMinutes()).isEqualTo(30);
    }

    @Test
    @DisplayName("Should have HOUR with 60 minutes duration")
    void shouldHaveHourWith60Minutes() {
        TariffPeriod period = TariffPeriod.HOUR;

        assertThat(period.getDuration()).isEqualTo(Duration.ofHours(1));
        assertThat(period.getMinutes()).isEqualTo(60);
    }

    @Test
    @DisplayName("Should have DAY with 1440 minutes duration")
    void shouldHaveDayWith1440Minutes() {
        TariffPeriod period = TariffPeriod.DAY;

        assertThat(period.getDuration()).isEqualTo(Duration.ofDays(1));
        assertThat(period.getMinutes()).isEqualTo(1440);
    }
}

