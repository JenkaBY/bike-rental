package com.github.jenkaby.bikerental.equipment.infrastructure.persistence.adapter;

import com.github.jenkaby.bikerental.equipment.domain.model.Equipment;
import com.github.jenkaby.bikerental.equipment.domain.repository.EquipmentRepository;
import com.github.jenkaby.bikerental.equipment.infrastructure.persistence.entity.EquipmentJpaEntity;
import com.github.jenkaby.bikerental.equipment.infrastructure.persistence.mapper.EquipmentJpaMapper;
import com.github.jenkaby.bikerental.equipment.infrastructure.persistence.repository.EquipmentJpaRepository;
import com.github.jenkaby.bikerental.equipment.shared.domain.model.vo.SerialNumber;
import com.github.jenkaby.bikerental.equipment.shared.domain.model.vo.Uid;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Page;
import com.github.jenkaby.bikerental.shared.domain.model.vo.PageRequest;
import com.github.jenkaby.bikerental.shared.mapper.PageMapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
class EquipmentRepositoryAdapter implements EquipmentRepository {

    private final EquipmentJpaRepository jpaRepository;
    private final EquipmentJpaMapper mapper;
    private final PageMapper pageMapper;

    EquipmentRepositoryAdapter(
            EquipmentJpaRepository jpaRepository,
            EquipmentJpaMapper mapper, PageMapper pageMapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
        this.pageMapper = pageMapper;
    }

    @Override
    public Equipment save(Equipment equipment) {
        var jpaEntity = mapper.toEntity(equipment);
        var savedEntity = jpaRepository.save(jpaEntity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Equipment> findById(Long id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public Page<Equipment> findAll(Optional<String> statusSlug, Optional<String> typeSlug, PageRequest request) {
        var pageRequest = pageMapper.toSpring(request);

        org.springframework.data.domain.Page<EquipmentJpaEntity> page = jpaRepository.findAllByFilters(statusSlug.orElse(null), typeSlug.orElse(null), pageRequest);
        return pageMapper.toDomain(page)
                .map(mapper::toDomain);
    }

    @Override
    public boolean existsBySerialNumber(SerialNumber serialNumber) {
        return jpaRepository.existsBySerialNumber(serialNumber.value());
    }

    @Override
    public boolean existsByUid(Uid uid) {
        return jpaRepository.existsByUid(uid.value());
    }
}
