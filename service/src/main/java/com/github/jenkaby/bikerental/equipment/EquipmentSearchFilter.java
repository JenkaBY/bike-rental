package com.github.jenkaby.bikerental.equipment;

public record EquipmentSearchFilter(String q) {

    public static EquipmentSearchFilter empty() {
        return new EquipmentSearchFilter(null);
    }
}
