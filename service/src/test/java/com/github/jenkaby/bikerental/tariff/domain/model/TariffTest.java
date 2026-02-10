package com.github.jenkaby.bikerental.tariff.domain.model;

import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Tariff Domain Model Tests")
class TariffTest {

    @Test
    @DisplayName("Should create Tariff with all fields")
    void shouldCreateTariffWithAllFields() {
        Tariff tariff = Tariff.builder()
                .id(1L)
                .name("Hourly Rate")
                .description("Standard hourly rental")
                .equipmentTypeSlug("bicycle")
                .basePrice(Money.of("100.00"))
                .halfHourPrice(Money.of("60.00"))
                .hourPrice(Money.of("100.00"))
                .dayPrice(Money.of("500.00"))
                .hourDiscountedPrice(Money.of("90.00"))
                .validFrom(LocalDate.of(2026, 1, 1))
                .validTo(LocalDate.of(2026, 12, 31))
                .status(TariffStatus.ACTIVE)
                .build();

        assertThat(tariff.getId()).isEqualTo(1L);
        assertThat(tariff.getName()).isEqualTo("Hourly Rate");
        assertThat(tariff.getDescription()).isEqualTo("Standard hourly rental");
        assertThat(tariff.getEquipmentTypeSlug()).isEqualTo("bicycle");
        assertThat(tariff.getBasePrice().amount()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(tariff.getHalfHourPrice().amount()).isEqualByComparingTo(new BigDecimal("60.00"));
        assertThat(tariff.getHourPrice().amount()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(tariff.getDayPrice().amount()).isEqualByComparingTo(new BigDecimal("500.00"));
        assertThat(tariff.getHourDiscountedPrice().amount()).isEqualByComparingTo(new BigDecimal("90.00"));
        assertThat(tariff.getValidFrom()).isEqualTo(LocalDate.of(2026, 1, 1));
        assertThat(tariff.getValidTo()).isEqualTo(LocalDate.of(2026, 12, 31));
        assertThat(tariff.getStatus()).isEqualTo(TariffStatus.ACTIVE);
    }

    @Test
    @DisplayName("Should identify active tariff")
    void shouldIdentifyActiveTariff() {
        Tariff tariff = Tariff.builder()
                .name("Test Tariff")
                .equipmentTypeSlug("bicycle")
                .basePrice(Money.of("100.00"))
                .halfHourPrice(Money.of("60.00"))
                .hourPrice(Money.of("100.00"))
                .dayPrice(Money.of("500.00"))
                .hourDiscountedPrice(Money.of("90.00"))
                .validFrom(LocalDate.now())
                .status(TariffStatus.ACTIVE)
                .build();

        assertThat(tariff.isActive()).isTrue();
    }

    @Test
    @DisplayName("Should identify inactive tariff")
    void shouldIdentifyInactiveTariff() {
        Tariff tariff = Tariff.builder()
                .name("Test Tariff")
                .equipmentTypeSlug("bicycle")
                .basePrice(Money.of("100.00"))
                .halfHourPrice(Money.of("60.00"))
                .hourPrice(Money.of("100.00"))
                .dayPrice(Money.of("500.00"))
                .hourDiscountedPrice(Money.of("90.00"))
                .validFrom(LocalDate.now())
                .status(TariffStatus.INACTIVE)
                .build();

        assertThat(tariff.isActive()).isFalse();
    }

    @Test
    @DisplayName("Should validate tariff is valid on date within range")
    void shouldValidateTariffIsValidOnDateWithinRange() {
        Tariff tariff = Tariff.builder()
                .name("Test Tariff")
                .equipmentTypeSlug("bicycle")
                .basePrice(Money.of("100.00"))
                .halfHourPrice(Money.of("60.00"))
                .hourPrice(Money.of("100.00"))
                .dayPrice(Money.of("500.00"))
                .hourDiscountedPrice(Money.of("90.00"))
                .validFrom(LocalDate.of(2026, 1, 1))
                .validTo(LocalDate.of(2026, 12, 31))
                .status(TariffStatus.ACTIVE)
                .build();

        assertThat(tariff.isValidOn(LocalDate.of(2026, 6, 15))).isTrue();
    }

    @Test
    @DisplayName("Should validate tariff is valid on start date")
    void shouldValidateTariffIsValidOnStartDate() {
        Tariff tariff = Tariff.builder()
                .name("Test Tariff")
                .equipmentTypeSlug("bicycle")
                .basePrice(Money.of("100.00"))
                .halfHourPrice(Money.of("60.00"))
                .hourPrice(Money.of("100.00"))
                .dayPrice(Money.of("500.00"))
                .hourDiscountedPrice(Money.of("90.00"))
                .validFrom(LocalDate.of(2026, 1, 1))
                .validTo(LocalDate.of(2026, 12, 31))
                .status(TariffStatus.ACTIVE)
                .build();

        assertThat(tariff.isValidOn(LocalDate.of(2026, 1, 1))).isTrue();
    }

    @Test
    @DisplayName("Should validate tariff is valid on end date")
    void shouldValidateTariffIsValidOnEndDate() {
        Tariff tariff = Tariff.builder()
                .name("Test Tariff")
                .equipmentTypeSlug("bicycle")
                .basePrice(Money.of("100.00"))
                .halfHourPrice(Money.of("60.00"))
                .hourPrice(Money.of("100.00"))
                .dayPrice(Money.of("500.00"))
                .hourDiscountedPrice(Money.of("90.00"))
                .validFrom(LocalDate.of(2026, 1, 1))
                .validTo(LocalDate.of(2026, 12, 31))
                .status(TariffStatus.ACTIVE)
                .build();

        assertThat(tariff.isValidOn(LocalDate.of(2026, 12, 31))).isTrue();
    }

    @Test
    @DisplayName("Should validate tariff is not valid before start date")
    void shouldValidateTariffIsNotValidBeforeStartDate() {
        Tariff tariff = Tariff.builder()
                .name("Test Tariff")
                .equipmentTypeSlug("bicycle")
                .basePrice(Money.of("100.00"))
                .halfHourPrice(Money.of("60.00"))
                .hourPrice(Money.of("100.00"))
                .dayPrice(Money.of("500.00"))
                .hourDiscountedPrice(Money.of("90.00"))
                .validFrom(LocalDate.of(2026, 1, 1))
                .validTo(LocalDate.of(2026, 12, 31))
                .status(TariffStatus.ACTIVE)
                .build();

        assertThat(tariff.isValidOn(LocalDate.of(2025, 12, 31))).isFalse();
    }

    @Test
    @DisplayName("Should validate tariff is not valid after end date")
    void shouldValidateTariffIsNotValidAfterEndDate() {
        Tariff tariff = Tariff.builder()
                .name("Test Tariff")
                .equipmentTypeSlug("bicycle")
                .basePrice(Money.of("100.00"))
                .halfHourPrice(Money.of("60.00"))
                .hourPrice(Money.of("100.00"))
                .dayPrice(Money.of("500.00"))
                .hourDiscountedPrice(Money.of("90.00"))
                .validFrom(LocalDate.of(2026, 1, 1))
                .validTo(LocalDate.of(2026, 12, 31))
                .status(TariffStatus.ACTIVE)
                .build();

        assertThat(tariff.isValidOn(LocalDate.of(2027, 1, 1))).isFalse();
    }

    @Test
    @DisplayName("Should validate tariff with no end date is always valid after start")
    void shouldValidateTariffWithNoEndDateIsAlwaysValidAfterStart() {
        Tariff tariff = Tariff.builder()
                .name("Test Tariff")
                .equipmentTypeSlug("bicycle")
                .basePrice(Money.of("100.00"))
                .halfHourPrice(Money.of("60.00"))
                .hourPrice(Money.of("100.00"))
                .dayPrice(Money.of("500.00"))
                .hourDiscountedPrice(Money.of("90.00"))
                .validFrom(LocalDate.of(2026, 1, 1))
                .validTo(null)
                .status(TariffStatus.ACTIVE)
                .build();

        assertThat(tariff.isValidOn(LocalDate.of(2030, 1, 1))).isTrue();
    }

    @Test
    @DisplayName("Should allow setting id after creation")
    void shouldAllowSettingIdAfterCreation() {
        Tariff tariff = Tariff.builder()
                .name("Test Tariff")
                .equipmentTypeSlug("bicycle")
                .basePrice(Money.of("100.00"))
                .halfHourPrice(Money.of("60.00"))
                .hourPrice(Money.of("100.00"))
                .dayPrice(Money.of("500.00"))
                .hourDiscountedPrice(Money.of("90.00"))
                .validFrom(LocalDate.now())
                .status(TariffStatus.ACTIVE)
                .build();

        tariff.setId(123L);

        assertThat(tariff.getId()).isEqualTo(123L);
    }

    @Test
    @DisplayName("Should return halfHourPrice for HALF_HOUR period")
    void shouldReturnHalfHourPriceForHalfHourPeriod() {
        Tariff tariff = Tariff.builder()
                .name("Test Tariff")
                .description("Test")
                .equipmentTypeSlug("bike")
                .basePrice(Money.of("100.00"))
                .halfHourPrice(Money.of("60.00"))
                .hourPrice(Money.of("100.00"))
                .dayPrice(Money.of("500.00"))
                .hourDiscountedPrice(Money.of("90.00"))
                .validFrom(LocalDate.of(2026, 1, 1))
                .validTo(null)
                .status(TariffStatus.ACTIVE)
                .build();

        Money price = tariff.getPriceForPeriod(TariffPeriod.HALF_HOUR);

        assertThat(price.amount()).isEqualByComparingTo(Money.of("60.00").amount());
    }

    @Test
    @DisplayName("Should return hourPrice for HOUR period")
    void shouldReturnHourPriceForHourPeriod() {
        Tariff tariff = Tariff.builder()
                .name("Test Tariff")
                .description("Test")
                .equipmentTypeSlug("bike")
                .basePrice(Money.of("100.00"))
                .halfHourPrice(Money.of("60.00"))
                .hourPrice(Money.of("100.00"))
                .dayPrice(Money.of("500.00"))
                .hourDiscountedPrice(Money.of("90.00"))
                .validFrom(LocalDate.of(2026, 1, 1))
                .validTo(null)
                .status(TariffStatus.ACTIVE)
                .build();

        Money price = tariff.getPriceForPeriod(TariffPeriod.HOUR);

        assertThat(price.amount()).isEqualByComparingTo(Money.of("100.00").amount());
    }

    @Test
    @DisplayName("Should return dayPrice for DAY period")
    void shouldReturnDayPriceForDayPeriod() {
        Tariff tariff = Tariff.builder()
                .name("Test Tariff")
                .description("Test")
                .equipmentTypeSlug("bike")
                .basePrice(Money.of("100.00"))
                .halfHourPrice(Money.of("60.00"))
                .hourPrice(Money.of("100.00"))
                .dayPrice(Money.of("500.00"))
                .hourDiscountedPrice(Money.of("90.00"))
                .validFrom(LocalDate.of(2026, 1, 1))
                .validTo(null)
                .status(TariffStatus.ACTIVE)
                .build();

        Money price = tariff.getPriceForPeriod(TariffPeriod.DAY);

        assertThat(price.amount()).isEqualByComparingTo(Money.of("500.00").amount());
    }
}
