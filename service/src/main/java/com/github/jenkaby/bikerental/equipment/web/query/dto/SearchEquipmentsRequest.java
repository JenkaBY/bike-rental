package com.github.jenkaby.bikerental.equipment.web.query.dto;

public record SearchEquipmentsRequest(
        String status,
        String type,
        String serialNumber,
        String uid
) {
}
