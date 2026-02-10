package com.github.jenkaby.bikerental.tariff.application.util;

import com.github.jenkaby.bikerental.tariff.domain.model.TariffPeriod;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Duration;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TariffPeriodSelector Tests")
class TariffPeriodSelectorTest {

    private final TariffPeriodSelector selector = new TariffPeriodSelector();

    @Test
    @DisplayName("Should return HALF_HOUR for duration up to 30 minutes")
    void shouldReturnHalfHourForUpTo30Minutes() {
        assertThat(selector.selectPeriod(Duration.ofMinutes(1))).isEqualTo(TariffPeriod.HALF_HOUR);
        assertThat(selector.selectPeriod(Duration.ofMinutes(15))).isEqualTo(TariffPeriod.HALF_HOUR);
        assertThat(selector.selectPeriod(Duration.ofMinutes(30))).isEqualTo(TariffPeriod.HALF_HOUR);
    }

    @Test
    @DisplayName("Should return HOUR for duration from 31 minutes to 3 hours 59 minutes")
    void shouldReturnHourFor31MinutesTo3Hours59Minutes() {
        assertThat(selector.selectPeriod(Duration.ofMinutes(31))).isEqualTo(TariffPeriod.HOUR);
        assertThat(selector.selectPeriod(Duration.ofHours(1))).isEqualTo(TariffPeriod.HOUR);
        assertThat(selector.selectPeriod(Duration.ofHours(2))).isEqualTo(TariffPeriod.HOUR);
        assertThat(selector.selectPeriod(Duration.ofHours(3))).isEqualTo(TariffPeriod.HOUR);
        assertThat(selector.selectPeriod(Duration.ofMinutes(239))).isEqualTo(TariffPeriod.HOUR);
    }

    @Test
    @DisplayName("Should return DAY for duration of 4 hours or more")
    void shouldReturnDayFor4HoursOrMore() {
        assertThat(selector.selectPeriod(Duration.ofHours(4))).isEqualTo(TariffPeriod.DAY);
        assertThat(selector.selectPeriod(Duration.ofHours(5))).isEqualTo(TariffPeriod.DAY);
        assertThat(selector.selectPeriod(Duration.ofDays(1))).isEqualTo(TariffPeriod.DAY);
        assertThat(selector.selectPeriod(Duration.ofDays(2))).isEqualTo(TariffPeriod.DAY);
    }

    @ParameterizedTest
    @MethodSource("periodBoundaryTestCases")
    @DisplayName("Should correctly select period at boundaries")
    void shouldCorrectlySelectPeriodAtBoundaries(Duration duration, TariffPeriod expectedPeriod) {
        assertThat(selector.selectPeriod(duration)).isEqualTo(expectedPeriod);
    }

    private static Stream<Arguments> periodBoundaryTestCases() {
        return Stream.of(
                Arguments.of(Duration.ofMinutes(0), TariffPeriod.HALF_HOUR),
                Arguments.of(Duration.ofMinutes(30), TariffPeriod.HALF_HOUR),
                Arguments.of(Duration.ofMinutes(31), TariffPeriod.HOUR),
                Arguments.of(Duration.ofMinutes(239), TariffPeriod.HOUR),
                Arguments.of(Duration.ofHours(4), TariffPeriod.DAY),
                Arguments.of(Duration.ofHours(4).plusMinutes(1), TariffPeriod.DAY)
        );
    }
}
