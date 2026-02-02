package com.github.jenkaby.bikerental.equipment.application.service;

import com.github.jenkaby.bikerental.equipment.application.usecase.GetEquipmentByUidUseCase;
import com.github.jenkaby.bikerental.equipment.domain.model.Equipment;
import com.github.jenkaby.bikerental.equipment.domain.repository.EquipmentRepository;
import com.github.jenkaby.bikerental.equipment.shared.domain.model.vo.Uid;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
class GetEquipmentByUidService implements GetEquipmentByUidUseCase {

    private final EquipmentRepository repository;

    GetEquipmentByUidService(EquipmentRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<Equipment> execute(Uid uid) {
        return repository.findByUid(uid);
    }
}
