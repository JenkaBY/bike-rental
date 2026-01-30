package com.github.jenkaby.bikerental.equipment.domain.model;

import lombok.*;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class EquipmentStatus {
    @Setter
    private Long id;
    private final String slug;
    private final String name;
    private final String description;
}
