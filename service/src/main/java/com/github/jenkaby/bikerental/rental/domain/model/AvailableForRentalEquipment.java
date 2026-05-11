package com.github.jenkaby.bikerental.rental.domain.model;

public record AvailableForRentalEquipment(
        Long id,
        String serialNumber,
        String uid,
        String typeSlug,
        String model
) {
}