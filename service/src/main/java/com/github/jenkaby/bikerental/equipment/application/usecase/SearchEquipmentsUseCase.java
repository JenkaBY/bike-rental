package com.github.jenkaby.bikerental.equipment.application.usecase;

import com.github.jenkaby.bikerental.equipment.domain.model.Equipment;

import java.util.List;
import java.util.Optional;

public interface SearchEquipmentsUseCase {
    List<Equipment> execute(SearchEquipmentsQuery query);

    record SearchEquipmentsQuery(
            Optional<String> statusSlug,
            Optional<String> typeSlug,
            Optional<String> serialNumber,
            Optional<String> uid
    ) {
    }
}
