package com.github.jenkaby.bikerental.equipment.infrastructure.persistence.adapter;

import com.github.jenkaby.bikerental.equipment.domain.model.EquipmentType;
import com.github.jenkaby.bikerental.equipment.domain.repository.EquipmentTypeRepository;
import com.github.jenkaby.bikerental.equipment.infrastructure.persistence.mapper.EquipmentTypeJpaMapper;
import com.github.jenkaby.bikerental.equipment.infrastructure.persistence.repository.EquipmentTypeJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
class EquipmentTypeRepositoryAdapter implements EquipmentTypeRepository {

    private final EquipmentTypeJpaRepository jpaRepository;
    private final EquipmentTypeJpaMapper mapper;

    EquipmentTypeRepositoryAdapter(
            EquipmentTypeJpaRepository jpaRepository,
            EquipmentTypeJpaMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public EquipmentType save(EquipmentType equipmentType) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Optional<EquipmentType> findBySlug(String slug) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public List<EquipmentType> findAll() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public boolean existsBySlug(String slug) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
