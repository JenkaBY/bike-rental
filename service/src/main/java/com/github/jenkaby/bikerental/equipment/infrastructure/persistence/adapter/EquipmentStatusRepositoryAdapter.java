package com.github.jenkaby.bikerental.equipment.infrastructure.persistence.adapter;

import com.github.jenkaby.bikerental.equipment.domain.model.EquipmentStatus;
import com.github.jenkaby.bikerental.equipment.domain.repository.EquipmentStatusRepository;
import com.github.jenkaby.bikerental.equipment.infrastructure.persistence.mapper.EquipmentStatusJpaMapper;
import com.github.jenkaby.bikerental.equipment.infrastructure.persistence.repository.EquipmentStatusJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

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
        var entity = mapper.toEntity(equipmentStatus);
        var savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<EquipmentStatus> findBySlug(String slug) {
        return jpaRepository.findBySlug(slug)
                .map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EquipmentStatus> findAll() {
        return jpaRepository.findAll().stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsBySlug(String slug) {
        return jpaRepository.existsBySlug(slug);
    }
}
