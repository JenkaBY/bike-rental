package com.github.jenkaby.bikerental.equipment.application.service;

import com.github.jenkaby.bikerental.equipment.application.usecase.SearchEquipmentsUseCase;
import com.github.jenkaby.bikerental.equipment.domain.model.Equipment;
import com.github.jenkaby.bikerental.equipment.domain.repository.EquipmentRepository;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Page;
import com.github.jenkaby.bikerental.shared.domain.model.vo.PageRequest;
import org.springframework.stereotype.Service;

@Service
class SearchEquipmentsService implements SearchEquipmentsUseCase {

    private final EquipmentRepository repository;

    SearchEquipmentsService(EquipmentRepository repository) {
        this.repository = repository;
    }

    @Override
    public Page<Equipment> execute(SearchEquipmentsQuery query) {
        PageRequest pageRequest = query.pageRequest();

        return repository.findAll(query.statusSlug(), query.typeSlug(), query.searchText(), pageRequest);
    }
}
