package com.github.jenkaby.bikerental.rental.application.service;

import com.github.jenkaby.bikerental.rental.domain.service.RentalDurationCalculator;
import com.github.jenkaby.bikerental.rental.domain.service.RentalDurationResult;
import com.github.jenkaby.bikerental.shared.config.RentalProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("RentalDurationCalculator Tests")
class RentalDurationCalculatorTest {

    private RentalDurationCalculator calculator;
    private static final LocalDateTime BASE_START_TIME = LocalDateTime.of(2026, 2, 18, 10, 0);

    @BeforeEach
    void setUp() {
        RentalProperties properties = new RentalProperties(
                Duration.ofMinutes(5),
                null // forgiveness not implemented yet
        );
        calculator = new RentalDurationCalculatorImpl(properties);
    }

    @Test
    @DisplayName("Should return correct time increment from configuration")
    void shouldReturnCorrectTimeIncrement() {
        assertThat(((RentalDurationCalculatorImpl) calculator).getTimeIncrementMinutes()).isEqualTo(5);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 4, 5, 6, 9, 10, 23, 61, 120, 121, 125, 180, 240, 1440})
    @DisplayName("Should calculate actual minutes correctly (no rounding)")
    void shouldCalculateActualMinutesCorrectly(int minutes) {
        LocalDateTime end = BASE_START_TIME.plusMinutes(minutes);
        RentalDurationResult result = calculator.calculate(BASE_START_TIME, end);
        assertThat(result.actualMinutes()).isEqualTo(minutes);
    }

    @ParameterizedTest
    @CsvSource({
            "23, 25",
            "61, 65",
            "120, 120",
            "121, 125",
            "124, 125",
            "125, 125",
            "126, 130",
            "179, 180",
            "181, 185",
            "239, 240",
            "241, 245",
            "1439, 1440",
            "1441, 1445",
            "1444, 1445",
            "1445, 1445",
            "1446, 1450"
    })
    @DisplayName("Should round up billable minutes to nearest 5-minute increment")
    void shouldRoundUpBillableMinutes(int actualMinutes, int expectedBillableMinutes) {
        LocalDateTime end = BASE_START_TIME.plusMinutes(actualMinutes);
        RentalDurationResult result = calculator.calculate(BASE_START_TIME, end);
        assertThat(result.billableMinutes()).isEqualTo(expectedBillableMinutes);
    }

    @ParameterizedTest
    @CsvSource({
            "0, 0",
            "1, 5",
            "4, 5",
            "5, 5",
            "6, 10",
            "9, 10",
            "10, 10",
            "11, 15"
    })
    @DisplayName("Should handle small durations correctly")
    void shouldHandleSmallDurations(int actualMinutes, int expectedBillableMinutes) {
        LocalDateTime end = BASE_START_TIME.plusMinutes(actualMinutes);
        RentalDurationResult result = calculator.calculate(BASE_START_TIME, end);
        assertThat(result.billableMinutes()).isEqualTo(expectedBillableMinutes);
    }

    @ParameterizedTest
    @CsvSource({
            "120, 120",
            "121, 125",
            "125, 125",
            "126, 130",
            "180, 180",
            "181, 185"
    })
    @DisplayName("Should handle cancellation window correctly (around 120 minutes)")
    void shouldHandleCancellationWindow(int actualMinutes, int expectedBillableMinutes) {
        LocalDateTime end = BASE_START_TIME.plusMinutes(actualMinutes);
        RentalDurationResult result = calculator.calculate(BASE_START_TIME, end);
        assertThat(result.billableMinutes()).isEqualTo(expectedBillableMinutes);
    }

    @ParameterizedTest
    @MethodSource("timeIncrementTestCases")
    @DisplayName("Should work with different time increments from configuration")
    void shouldWorkWithDifferentTimeIncrements(int incrementMinutes, int actualMinutes, int expectedBillableMinutes) {
        RentalProperties properties = new RentalProperties(Duration.ofMinutes(incrementMinutes), null);
        RentalDurationCalculatorImpl testCalculator = new RentalDurationCalculatorImpl(properties);

        RentalDurationResult result = testCalculator.calculate(BASE_START_TIME, BASE_START_TIME.plusMinutes(actualMinutes));
        assertThat(result.actualMinutes()).isEqualTo(actualMinutes);
        assertThat(result.billableMinutes()).isEqualTo(expectedBillableMinutes);
    }

    private static Stream<Arguments> timeIncrementTestCases() {
        return Stream.of(
                Arguments.of(5, 23, 25),
                Arguments.of(5, 61, 65),
                Arguments.of(5, 121, 125),
                Arguments.of(10, 23, 30),
                Arguments.of(10, 61, 70),
                Arguments.of(10, 121, 130),
                Arguments.of(15, 23, 30),
                Arguments.of(15, 61, 75),
                Arguments.of(15, 121, 135),
                Arguments.of(30, 23, 30),
                Arguments.of(30, 61, 90),
                Arguments.of(30, 121, 150)
        );
    }

    @Test
    @DisplayName("Should handle very long durations (multiple days)")
    void shouldHandleVeryLongDurations() {
        // 2 days = 2880 minutes
        LocalDateTime end = BASE_START_TIME.plusDays(2);
        RentalDurationResult result = calculator.calculate(BASE_START_TIME, end);
        assertThat(result.actualMinutes()).isEqualTo(2880);
        assertThat(result.billableMinutes()).isEqualTo(2880); // Exactly divisible by 5
    }

    @Test
    @DisplayName("Should handle durations not divisible by increment")
    void shouldHandleNonDivisibleDurations() {
        // 123 minutes should round up to 125
        LocalDateTime end = BASE_START_TIME.plusMinutes(123);
        RentalDurationResult result = calculator.calculate(BASE_START_TIME, end);
        assertThat(result.actualMinutes()).isEqualTo(123);
        assertThat(result.billableMinutes()).isEqualTo(125);
    }

    @Test
    @DisplayName("Should return same actualDuration in result")
    void shouldReturnSameActualDuration() {
        LocalDateTime end = BASE_START_TIME.plusMinutes(123);
        RentalDurationResult result = calculator.calculate(BASE_START_TIME, end);

        Duration expectedDuration = Duration.between(BASE_START_TIME, end);
        assertThat(result.actualDuration()).isEqualTo(expectedDuration);
        assertThat(result.actualMinutes()).isEqualTo(123);
    }

    @Test
    @DisplayName("Should handle zero duration")
    void shouldHandleZeroDuration() {
        RentalDurationResult result = calculator.calculate(BASE_START_TIME, BASE_START_TIME);
        assertThat(result.actualMinutes()).isEqualTo(0);
        assertThat(result.billableMinutes()).isEqualTo(0);
        assertThat(result.actualDuration()).isEqualTo(Duration.ZERO);
    }

    @Test
    @DisplayName("Should handle negative duration (should not happen in practice but test edge case)")
    void shouldHandleNegativeDuration() {
        // This should not happen in practice, but let's test the behavior
        LocalDateTime end = BASE_START_TIME.minusMinutes(5);
        RentalDurationResult result = calculator.calculate(BASE_START_TIME, end);

        // Duration.between will return negative duration
        assertThat(result.actualDuration().toMinutes()).isNegative();
    }
}
