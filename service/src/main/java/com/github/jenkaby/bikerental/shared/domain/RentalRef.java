package com.github.jenkaby.bikerental.shared.domain;

public record RentalRef(Long id) implements RentalId {

    public RentalRef {
        if (id == null) {
            throw new IllegalArgumentException("Rental id must not be null");
        }
    }

    public static RentalRef of(Long id) {
        return new RentalRef(id);
    }
}
