package com.github.jenkaby.bikerental.shared.domain;

public record RentalRef(Long id) {

    public RentalRef {
        if (id == null) {
            throw new IllegalArgumentException("Rental id must not be null");
        }
    }

    public static RentalRef of(Long id) {
        return new RentalRef(id);
    }
}
