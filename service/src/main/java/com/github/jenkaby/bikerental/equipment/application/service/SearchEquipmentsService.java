package com.github.jenkaby.bikerental.equipment.application.service;

import com.github.jenkaby.bikerental.equipment.application.usecase.SearchEquipmentsUseCase;
import com.github.jenkaby.bikerental.equipment.domain.model.Equipment;
import com.github.jenkaby.bikerental.equipment.domain.repository.EquipmentRepository;
import com.github.jenkaby.bikerental.equipment.shared.domain.model.vo.SerialNumber;
import com.github.jenkaby.bikerental.equipment.shared.domain.model.vo.Uid;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Page;
import com.github.jenkaby.bikerental.shared.domain.model.vo.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class SearchEquipmentsService implements SearchEquipmentsUseCase {

    private final EquipmentRepository repository;

    SearchEquipmentsService(EquipmentRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Equipment> execute(SearchEquipmentsQuery query) {
        // If serial number provided - exact match
        PageRequest pageRequest = query.pageRequest();
        if (query.serialNumber().isPresent()) {
            var serial = new SerialNumber(query.serialNumber().get());
            return (Page<Equipment>) repository.findBySerialNumber(serial)
                    .map(l -> Page.of(l, pageRequest))
                    .orElseGet(() -> Page.empty(pageRequest));
        }

        // If UID provided - exact match
        if (query.uid().isPresent()) {
            var uid = new Uid(query.uid().get());
            return (Page<Equipment>) repository.findByUid(uid)
                    .map(l -> Page.of(l, pageRequest))
                    .orElseGet(() -> Page.empty(pageRequest));
        }

        // Otherwise, search by optional filters (statusSlug, typeSlug)
        return repository.findAll(query.status(), query.type(), pageRequest);
    }
}
