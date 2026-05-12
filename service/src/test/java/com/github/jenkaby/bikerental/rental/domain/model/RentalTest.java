package com.github.jenkaby.bikerental.rental.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

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
}
