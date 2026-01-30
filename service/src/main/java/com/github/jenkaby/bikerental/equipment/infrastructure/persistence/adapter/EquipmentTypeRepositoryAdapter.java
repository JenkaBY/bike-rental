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
        var jpaEntity = mapper.toEntity(equipmentType);
        var savedEntity = jpaRepository.save(jpaEntity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<EquipmentType> findBySlug(String slug) {
        return jpaRepository.findBySlug(slug)
                .map(mapper::toDomain);
    }

    @Override
    public List<EquipmentType> findAll() {
        return jpaRepository.findAll().stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsBySlug(String slug) {
        return jpaRepository.existsBySlug(slug);
    }
}
