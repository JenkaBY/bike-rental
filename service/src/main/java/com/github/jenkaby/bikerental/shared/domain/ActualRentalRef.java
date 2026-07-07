package com.github.jenkaby.bikerental.shared.domain;

public record ActualRentalRef(Long id, Long rentalVersion) implements RentalId {

    public ActualRentalRef {
        if (id == null) {
            throw new IllegalArgumentException("Rental id must not be null");
        }
        if (rentalVersion == null) {
            throw new IllegalArgumentException("Rental version must not be null");
        }
    }

    public static ActualRentalRef of(Long rentalId, Long rentalVersion) {
        return new ActualRentalRef(rentalId, rentalVersion);
    }

    public RentalRef toRentalRef() {
        return new RentalRef(id);
    }
}
