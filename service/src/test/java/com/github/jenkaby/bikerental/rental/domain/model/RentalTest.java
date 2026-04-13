package com.github.jenkaby.bikerental.rental.domain.model;

import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

@DisplayName("Rental Domain Model Tests")
class RentalTest {

    @Test
    @DisplayName("Should create draft rental with DRAFT status and createdAt")
    void shouldCreateDraftRental() {
        Rental rental = Rental.createDraft();

        assertThat(rental.getStatus()).isEqualTo(RentalStatus.DRAFT);
        assertThat(rental.getCreatedAt()).isCloseTo(Instant.now(), within(200, ChronoUnit.MILLIS));
        assertThat(rental.getCustomerId()).isNull();
        assertThat(rental.getEquipments()).isEmpty();
    }

    @Test
    @DisplayName("isPrepaymentSufficient returns false when estimated cost is not set")
    void isPrepaymentSufficientReturnsFalseWhenEstimatedCostNotSet() {
        Rental rental = Rental.createDraft();
        rental.selectCustomer(UUID.randomUUID());
        rental.addEquipment(RentalEquipment.assigned(1L, null, null));
        rental.selectTariff(1L);
        rental.setPlannedDuration(Duration.ofHours(2));

        assertThat(rental.isPrepaymentSufficient(Money.of("100.00"))).isFalse();
    }

    @Test
    @DisplayName("isPrepaymentSufficient returns false when amount is below estimated cost")
    void isPrepaymentSufficientReturnsFalseWhenAmountBelowEstimatedCost() {
        Rental rental = createRentalWithEstimatedCost("100.00");

        assertThat(rental.isPrepaymentSufficient(Money.of("50.00"))).isFalse();
    }

    @Test
    @DisplayName("isPrepaymentSufficient returns true when amount equals estimated cost")
    void isPrepaymentSufficientReturnsTrueWhenAmountEqualsEstimatedCost() {
        Rental rental = createRentalWithEstimatedCost("100.00");

        assertThat(rental.isPrepaymentSufficient(Money.of("100.00"))).isTrue();
    }

    @Test
    @DisplayName("isPrepaymentSufficient returns true when amount exceeds estimated cost")
    void isPrepaymentSufficientReturnsTrueWhenAmountExceedsEstimatedCost() {
        Rental rental = createRentalWithEstimatedCost("100.00");

        assertThat(rental.isPrepaymentSufficient(Money.of("150.00"))).isTrue();
    }

    private static Rental createRentalWithEstimatedCost(String amount) {
        Rental rental = Rental.createDraft();
        rental.selectCustomer(UUID.randomUUID());
        rental.addEquipment(RentalEquipment.assigned(1L, null, null));
        rental.selectTariff(1L);
        rental.setPlannedDuration(Duration.ofHours(2));
        rental.setEstimatedCost(Money.of(amount));
        return rental;
    }
}
