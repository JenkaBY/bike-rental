package com.github.jenkaby.bikerental.equipment.domain.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class EquipmentStatus {
    private final String slug;
    private final String name;
    private final String description;
}
