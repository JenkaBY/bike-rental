package com.github.jenkaby.bikerental.agreement;

import com.github.jenkaby.bikerental.shared.domain.event.BikeRentalEvent;

public record AgreementSignedEvent(Long rentalId, Long signatureId) implements BikeRentalEvent {
}
