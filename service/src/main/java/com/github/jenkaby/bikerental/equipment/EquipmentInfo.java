package com.github.jenkaby.bikerental.equipment;


public record EquipmentInfo(
        Long id,
        String serialNumber,
        String uid,
        String typeSlug,
        String statusSlug,
        String model
) {

    public boolean isAvailable() {
        return "AVAILABLE".equals(statusSlug);
    }
}
