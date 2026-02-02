package com.github.jenkaby.bikerental.equipment.application.usecase;

import com.github.jenkaby.bikerental.equipment.domain.model.Equipment;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Page;
import com.github.jenkaby.bikerental.shared.domain.model.vo.PageRequest;

import java.util.Optional;

public interface SearchEquipmentsUseCase {
    Page<Equipment> execute(SearchEquipmentsQuery query);

    record SearchEquipmentsQuery(
            String statusSlug,
            String typeSlug,
            PageRequest pageRequest
    ) {
        public Optional<String> status() {
            return Optional.ofNullable(statusSlug);
        }

        public Optional<String> type() {
            return Optional.ofNullable(typeSlug);
        }
    }
}
