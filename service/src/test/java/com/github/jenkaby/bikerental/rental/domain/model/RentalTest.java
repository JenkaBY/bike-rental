package com.github.jenkaby.bikerental.rental.domain.model;

import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Rental Domain Model Tests")
class RentalTest {

    @Test
    @DisplayName("Should create draft rental with DRAFT status and createdAt")
    void shouldCreateDraftRental() {
        Rental rental = Rental.createDraft();

        assertThat(rental.getStatus()).isEqualTo(RentalStatus.DRAFT);
        assertThat(rental.getCreatedAt()).isNotNull();
        assertThat(rental.getCustomerId()).isNull();
        assertThat(rental.getEquipmentId()).isNull();
        assertThat(rental.getTariffId()).isNull();
    }

    @Test
    @DisplayName("Should select customer when rental is in DRAFT status")
    void shouldSelectCustomerWhenDraft() {
        UUID customerId = UUID.randomUUID();
        Rental rental = Rental.createDraft();
        Instant beforeUpdate = rental.getUpdatedAt();

        rental.selectCustomer(customerId);

        assertThat(rental.getCustomerId()).isEqualTo(customerId);
        assertThat(rental.getUpdatedAt()).isNotNull();
        assertThat(rental.getUpdatedAt()).isAfterOrEqualTo(beforeUpdate);
    }

    @Test
    @DisplayName("Should throw exception when selecting customer for non-DRAFT rental")
    void shouldThrowExceptionWhenSelectingCustomerForNonDraftRental() {
        UUID customerId = UUID.randomUUID();
        Rental rental = Rental.builder()
                .status(RentalStatus.ACTIVE)
                .createdAt(Instant.now())
                .build();

        assertThatThrownBy(() -> rental.selectCustomer(customerId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot select customer for rental that is not in DRAFT status");
    }

    @Test
    @DisplayName("Should select equipment when rental is in DRAFT status")
    void shouldSelectEquipmentWhenDraft() {
        Long equipmentId = 123L;
        Rental rental = Rental.createDraft();
        Instant beforeUpdate = rental.getUpdatedAt();

        rental.selectEquipment(equipmentId);

        assertThat(rental.getEquipmentId()).isEqualTo(equipmentId);
        assertThat(rental.getUpdatedAt()).isNotNull();
        assertThat(rental.getUpdatedAt()).isAfterOrEqualTo(beforeUpdate);
    }

    @Test
    @DisplayName("Should throw exception when selecting equipment for non-DRAFT rental")
    void shouldThrowExceptionWhenSelectingEquipmentForNonDraftRental() {
        Long equipmentId = 123L;
        Rental rental = Rental.builder()
                .status(RentalStatus.ACTIVE)
                .createdAt(Instant.now())
                .build();

        assertThatThrownBy(() -> rental.selectEquipment(equipmentId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot select equipment for rental that is not in DRAFT status");
    }

    @Test
    @DisplayName("Should select tariff when rental is in DRAFT status")
    void shouldSelectTariffWhenDraft() {
        Long tariffId = 456L;
        Rental rental = Rental.createDraft();
        Instant beforeUpdate = rental.getUpdatedAt();

        rental.selectTariff(tariffId);

        assertThat(rental.getTariffId()).isEqualTo(tariffId);
        assertThat(rental.getUpdatedAt()).isNotNull();
        assertThat(rental.getUpdatedAt()).isAfterOrEqualTo(beforeUpdate);
    }

    @Test
    @DisplayName("Should throw exception when selecting tariff for non-DRAFT rental")
    void shouldThrowExceptionWhenSelectingTariffForNonDraftRental() {
        Long tariffId = 456L;
        Rental rental = Rental.builder()
                .status(RentalStatus.ACTIVE)
                .createdAt(Instant.now())
                .build();

        assertThatThrownBy(() -> rental.selectTariff(tariffId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot select tariff for rental that is not in DRAFT status");
    }

    @Test
    @DisplayName("Should set planned duration and calculate expected return time")
    void shouldSetPlannedDurationAndCalculateExpectedReturnTime() {
        Rental rental = Rental.createDraft();
        Duration duration = Duration.ofHours(2);
        LocalDateTime startTime = LocalDateTime.of(2026, 2, 6, 10, 0);
        Instant beforeUpdate = rental.getUpdatedAt();

        rental.setPlannedDuration(duration, startTime);

        assertThat(rental.getPlannedDuration()).isEqualTo(duration);
        assertThat(rental.getStartedAt()).isEqualTo(startTime);
        assertThat(rental.getExpectedReturnAt()).isEqualTo(startTime.plus(duration));
        assertThat(rental.getUpdatedAt()).isNotNull();
        assertThat(rental.getUpdatedAt()).isAfterOrEqualTo(beforeUpdate);
    }

    @Test
    @DisplayName("Should set estimated cost and update timestamp")
    void shouldSetEstimatedCost() {
        Rental rental = Rental.createDraft();
        Money estimatedCost = Money.of("150.00");
        Instant beforeUpdate = rental.getUpdatedAt();

        rental.setEstimatedCost(estimatedCost);

        assertThat(rental.getEstimatedCost()).isEqualTo(estimatedCost);
        assertThat(rental.getUpdatedAt()).isNotNull();
        assertThat(rental.getUpdatedAt()).isAfterOrEqualTo(beforeUpdate);
    }

    @Test
    @DisplayName("Should return true when rental can be activated with all required fields")
    void shouldReturnTrueWhenRentalCanBeActivated() {
        UUID customerId = UUID.randomUUID();
        Long equipmentId = 123L;
        Long tariffId = 456L;
        Duration duration = Duration.ofHours(2);
        Money estimatedCost = Money.of("150.00");

        Rental rental = Rental.builder()
                .status(RentalStatus.DRAFT)
                .customerId(customerId)
                .equipmentId(equipmentId)
                .tariffId(tariffId)
                .plannedDuration(duration)
                .estimatedCost(estimatedCost)
                .createdAt(Instant.now())
                .build();

        assertThat(rental.canBeActivated()).isTrue();
    }

    @Test
    @DisplayName("Should return false when rental status is not DRAFT")
    void shouldReturnFalseWhenRentalStatusIsNotDraft() {
        UUID customerId = UUID.randomUUID();
        Long equipmentId = 123L;
        Long tariffId = 456L;
        Duration duration = Duration.ofHours(2);
        Money estimatedCost = Money.of("150.00");

        Rental rental = Rental.builder()
                .status(RentalStatus.ACTIVE)
                .customerId(customerId)
                .equipmentId(equipmentId)
                .tariffId(tariffId)
                .plannedDuration(duration)
                .estimatedCost(estimatedCost)
                .createdAt(Instant.now())
                .build();

        assertThat(rental.canBeActivated()).isFalse();
    }

    @Test
    @DisplayName("Should return false when customer is not selected")
    void shouldReturnFalseWhenCustomerIsNotSelected() {
        Long equipmentId = 123L;
        Long tariffId = 456L;
        Duration duration = Duration.ofHours(2);
        Money estimatedCost = Money.of("150.00");

        Rental rental = Rental.builder()
                .status(RentalStatus.DRAFT)
                .customerId(null)
                .equipmentId(equipmentId)
                .tariffId(tariffId)
                .plannedDuration(duration)
                .estimatedCost(estimatedCost)
                .createdAt(Instant.now())
                .build();

        assertThat(rental.canBeActivated()).isFalse();
    }

    @Test
    @DisplayName("Should return false when equipment is not selected")
    void shouldReturnFalseWhenEquipmentIsNotSelected() {
        UUID customerId = UUID.randomUUID();
        Long tariffId = 456L;
        Duration duration = Duration.ofHours(2);
        Money estimatedCost = Money.of("150.00");

        Rental rental = Rental.builder()
                .status(RentalStatus.DRAFT)
                .customerId(customerId)
                .equipmentId(null)
                .tariffId(tariffId)
                .plannedDuration(duration)
                .estimatedCost(estimatedCost)
                .createdAt(Instant.now())
                .build();

        assertThat(rental.canBeActivated()).isFalse();
    }

    @Test
    @DisplayName("Should return false when tariff is not selected")
    void shouldReturnFalseWhenTariffIsNotSelected() {
        UUID customerId = UUID.randomUUID();
        Long equipmentId = 123L;
        Duration duration = Duration.ofHours(2);
        Money estimatedCost = Money.of("150.00");

        Rental rental = Rental.builder()
                .status(RentalStatus.DRAFT)
                .customerId(customerId)
                .equipmentId(equipmentId)
                .tariffId(null)
                .plannedDuration(duration)
                .estimatedCost(estimatedCost)
                .createdAt(Instant.now())
                .build();

        assertThat(rental.canBeActivated()).isFalse();
    }

    @Test
    @DisplayName("Should return false when planned duration is not set")
    void shouldReturnFalseWhenPlannedDurationIsNotSet() {
        UUID customerId = UUID.randomUUID();
        Long equipmentId = 123L;
        Long tariffId = 456L;
        Money estimatedCost = Money.of("150.00");

        Rental rental = Rental.builder()
                .status(RentalStatus.DRAFT)
                .customerId(customerId)
                .equipmentId(equipmentId)
                .tariffId(tariffId)
                .plannedDuration(null)
                .estimatedCost(estimatedCost)
                .createdAt(Instant.now())
                .build();

        assertThat(rental.canBeActivated()).isFalse();
    }

    @Test
    @DisplayName("Should return false when estimated cost is not set")
    void shouldReturnFalseWhenEstimatedCostIsNotSet() {
        UUID customerId = UUID.randomUUID();
        Long equipmentId = 123L;
        Long tariffId = 456L;
        Duration duration = Duration.ofHours(2);

        Rental rental = Rental.builder()
                .status(RentalStatus.DRAFT)
                .customerId(customerId)
                .equipmentId(equipmentId)
                .tariffId(tariffId)
                .plannedDuration(duration)
                .estimatedCost(null)
                .createdAt(Instant.now())
                .build();

        assertThat(rental.canBeActivated()).isFalse();
    }

    @Test
    @DisplayName("Should update updatedAt when selecting customer")
    void shouldUpdateUpdatedAtWhenSelectingCustomer() {
        Rental rental = Rental.createDraft();
        UUID customerId = UUID.randomUUID();
        Instant initialUpdatedAt = rental.getUpdatedAt();

        rental.selectCustomer(customerId);

        assertThat(rental.getUpdatedAt()).isAfterOrEqualTo(initialUpdatedAt);
    }

    @Test
    @DisplayName("Should handle setPlannedDuration with different duration values")
    void shouldHandleSetPlannedDurationWithDifferentDurations() {
        Rental rental = Rental.createDraft();
        LocalDateTime startTime = LocalDateTime.of(2026, 2, 6, 10, 0);

        rental.setPlannedDuration(Duration.ofMinutes(30), startTime);
        assertThat(rental.getExpectedReturnAt()).isEqualTo(startTime.plusMinutes(30));

        rental.setPlannedDuration(Duration.ofHours(1), startTime);
        assertThat(rental.getExpectedReturnAt()).isEqualTo(startTime.plusHours(1));

        rental.setPlannedDuration(Duration.ofDays(1), startTime);
        assertThat(rental.getExpectedReturnAt()).isEqualTo(startTime.plusDays(1));
    }
}
