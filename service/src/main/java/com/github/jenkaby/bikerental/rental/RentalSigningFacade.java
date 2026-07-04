package com.github.jenkaby.bikerental.rental;

import java.time.Instant;

public interface RentalSigningFacade {

    RentalSigningSnapshot getSigningSnapshot(Long rentalId);

    void completeSigning(Long rentalId, Long expectedVersion, Instant signedAt);
}
