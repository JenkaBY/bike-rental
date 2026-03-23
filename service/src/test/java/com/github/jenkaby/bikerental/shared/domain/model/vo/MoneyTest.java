package com.github.jenkaby.bikerental.shared.domain.model.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Money Value Object Tests")
class MoneyTest {

    @Test
    @DisplayName("Should create Money with valid amount")
    void shouldCreateMoneyWithValidAmount() {
        Money money = Money.of(new BigDecimal("100.50"));

        assertThat(money.amount()).isEqualByComparingTo(new BigDecimal("100.50"));
    }

    @Test
    @DisplayName("Should create Money from string")
    void shouldCreateMoneyFromString() {
        Money money = Money.of("99.99");

        assertThat(money.amount()).isEqualByComparingTo(new BigDecimal("99.99"));
    }

    @Test
    @DisplayName("Should create zero Money")
    void shouldCreateZeroMoney() {
        Money money = Money.zero();

        assertThat(money.amount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(money.isZero()).isTrue();
    }

    @Test
    @DisplayName("Should throw exception when amount is null")
    void shouldThrowExceptionWhenAmountIsNull() {
        assertThatThrownBy(() -> Money.of((BigDecimal) null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Amount cannot be null");
    }

    @Test
    @DisplayName("Should round amount with more than 2 decimal places to 2 decimal places")
    void shouldRoundAmountWithMoreThan2DecimalPlaces() {
        Money money = Money.of(new BigDecimal("100.123"));

        assertThat(money.amount()).isEqualByComparingTo(new BigDecimal("100.12"));
    }

    @Test
    @DisplayName("Should round amount with more than 2 decimal places using HALF_UP rounding")
    void shouldRoundAmountUsingHalfUpRounding() {
        Money money = Money.of(new BigDecimal("100.125"));

        assertThat(money.amount()).isEqualByComparingTo(new BigDecimal("100.13"));
    }

    @Test
    @DisplayName("Should identify positive amount")
    void shouldIdentifyPositiveAmount() {
        Money money = Money.of("100.00");

        assertThat(money.isPositive()).isTrue();
        assertThat(money.isNegative()).isFalse();
        assertThat(money.isZero()).isFalse();
        assertThat(money.isNegativeOrZero()).isFalse();
    }

    @Test
    @DisplayName("Should identify negative amount")
    void shouldIdentifyNegativeAmount() {
        Money money = Money.of("-50.00");

        assertThat(money.isNegative()).isTrue();
        assertThat(money.isPositive()).isFalse();
        assertThat(money.isZero()).isFalse();
        assertThat(money.isNegativeOrZero()).isTrue();
    }

    @Test
    @DisplayName("Should identify zero amount")
    void shouldIdentifyZeroAmount() {
        Money money = Money.zero();

        assertThat(money.isZero()).isTrue();
        assertThat(money.isPositive()).isFalse();
        assertThat(money.isNegative()).isFalse();
        assertThat(money.isNegativeOrZero()).isTrue();
    }

    @Test
    @DisplayName("Should add two Money values")
    void shouldAddTwoMoneyValues() {
        Money money1 = Money.of("100.50");
        Money money2 = Money.of("50.25");

        Money result = money1.add(money2);

        assertThat(result.amount()).isEqualByComparingTo(new BigDecimal("150.75"));
    }

    @Test
    @DisplayName("Should subtract two Money values")
    void shouldSubtractTwoMoneyValues() {
        Money money1 = Money.of("100.50");
        Money money2 = Money.of("50.25");

        Money result = money1.subtract(money2);

        assertThat(result.amount()).isEqualByComparingTo(new BigDecimal("50.25"));
    }

    @Test
    @DisplayName("Should multiply Money by multiplier")
    void shouldMultiplyMoneyByMultiplier() {
        Money money = Money.of("100.00");

        Money result = money.multiply(new BigDecimal("1.5"));

        assertThat(result.amount()).isEqualByComparingTo(new BigDecimal("150.00"));
    }

    @Test
    @DisplayName("Should round multiplication result to 2 decimal places")
    void shouldRoundMultiplicationResultTo2DecimalPlaces() {
        Money money = Money.of("100.00");

        Money result = money.multiply(new BigDecimal("0.333"));

        assertThat(result.amount()).isEqualByComparingTo(new BigDecimal("33.30"));
    }

    @Test
    @DisplayName("Should return plain string representation")
    void shouldReturnPlainStringRepresentation() {
        Money money = Money.of("100.50");

        assertThat(money.toString()).isEqualTo("100.5");
    }
}
