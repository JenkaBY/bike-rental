package com.github.jenkaby.bikerental.equipment;

import com.github.jenkaby.bikerental.shared.domain.model.Condition;

public record EquipmentInfo(
        Long id,
        String serialNumber,
        String uid,
        String typeSlug,
        String statusSlug,
        String model,
        Condition conditionSlug
) {

    public boolean isAvailable() {
        return "AVAILABLE".equals(statusSlug);
    }
}
