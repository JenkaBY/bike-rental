package com.github.jenkaby.bikerental.equipment.infrastructure.persistence.adapter;

import com.github.jenkaby.bikerental.equipment.domain.model.Equipment;
import com.github.jenkaby.bikerental.equipment.domain.repository.EquipmentRepository;
import com.github.jenkaby.bikerental.equipment.infrastructure.persistence.mapper.EquipmentJpaMapper;
import com.github.jenkaby.bikerental.equipment.infrastructure.persistence.repository.EquipmentJpaRepository;
import com.github.jenkaby.bikerental.equipment.shared.domain.model.vo.SerialNumber;
import com.github.jenkaby.bikerental.equipment.shared.domain.model.vo.Uid;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
class EquipmentRepositoryAdapter implements EquipmentRepository {

    private final EquipmentJpaRepository jpaRepository;
    private final EquipmentJpaMapper mapper;

    EquipmentRepositoryAdapter(
            EquipmentJpaRepository jpaRepository,
            EquipmentJpaMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Equipment save(Equipment equipment) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Optional<Equipment> findById(Long id) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Optional<Equipment> findBySerialNumber(SerialNumber serialNumber) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Optional<Equipment> findByUid(Uid uid) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public List<Equipment> findAll(Optional<String> statusSlug, Optional<String> typeSlug) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public boolean existsBySerialNumber(SerialNumber serialNumber) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
