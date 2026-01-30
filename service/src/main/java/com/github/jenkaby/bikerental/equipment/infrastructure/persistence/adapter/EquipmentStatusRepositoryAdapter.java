package com.github.jenkaby.bikerental.equipment.infrastructure.persistence.adapter;

import com.github.jenkaby.bikerental.equipment.domain.model.EquipmentStatus;
import com.github.jenkaby.bikerental.equipment.domain.repository.EquipmentStatusRepository;
import com.github.jenkaby.bikerental.equipment.infrastructure.persistence.mapper.EquipmentStatusJpaMapper;
import com.github.jenkaby.bikerental.equipment.infrastructure.persistence.repository.EquipmentStatusJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
class EquipmentStatusRepositoryAdapter implements EquipmentStatusRepository {

    private final EquipmentStatusJpaRepository jpaRepository;
    private final EquipmentStatusJpaMapper mapper;

    EquipmentStatusRepositoryAdapter(
            EquipmentStatusJpaRepository jpaRepository,
            EquipmentStatusJpaMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public EquipmentStatus save(EquipmentStatus equipmentStatus) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Optional<EquipmentStatus> findBySlug(String slug) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public List<EquipmentStatus> findAll() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public boolean existsBySlug(String slug) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
