package com.github.jenkaby.bikerental.equipment.infrastructure.persistence.adapter;

import com.github.jenkaby.bikerental.equipment.domain.model.Equipment;
import com.github.jenkaby.bikerental.equipment.domain.repository.EquipmentRepository;
import com.github.jenkaby.bikerental.equipment.infrastructure.persistence.entity.EquipmentJpaEntity;
import com.github.jenkaby.bikerental.equipment.infrastructure.persistence.mapper.EquipmentJpaMapper;
import com.github.jenkaby.bikerental.equipment.infrastructure.persistence.repository.EquipmentJpaRepository;
import com.github.jenkaby.bikerental.equipment.infrastructure.persistence.specification.EquipmentSpec;
import com.github.jenkaby.bikerental.equipment.infrastructure.persistence.specification.EquipmentSpecConstant;
import com.github.jenkaby.bikerental.equipment.shared.domain.model.vo.SerialNumber;
import com.github.jenkaby.bikerental.equipment.shared.domain.model.vo.Uid;
import com.github.jenkaby.bikerental.shared.domain.model.Condition;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Page;
import com.github.jenkaby.bikerental.shared.domain.model.vo.PageRequest;
import com.github.jenkaby.bikerental.shared.mapper.PageMapper;
import net.kaczmarzyk.spring.data.jpa.utils.SpecificationBuilder;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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
    @Transactional
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
    public List<Equipment> findByIds(Collection<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return List.of();
        }
        return jpaRepository.findAllByIdIn(ids).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Page<Equipment> findAll(String statusSlug, String typeSlug, String searchText, PageRequest request) {
        var pageRequest = pageMapper.toSpring(request);

        var spec = SpecificationBuilder.specification(EquipmentSpec.class)
                .withParam(EquipmentSpecConstant.STATUS, statusSlug)
                .withParam(EquipmentSpecConstant.TYPE, typeSlug)
                .withParam(EquipmentSpecConstant.SEARCH, searchText)
                .build();

        org.springframework.data.domain.Page<EquipmentJpaEntity> page = jpaRepository.findAll(spec, pageRequest);
        return pageMapper.toDomain(page)
                .map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Equipment> findByConditions(Set<Condition> conditions, String searchText) {
        if (conditions.isEmpty()) {
            throw new IllegalArgumentException("conditions must not be empty");
        }
        Specification<EquipmentJpaEntity> spec =
                (root, query, cb) -> root.get(EquipmentSpecConstant.CONDITION_SLUG).in(conditions);
        if (searchText != null) {
            var textSpec = SpecificationBuilder.specification(EquipmentSpec.class)
                    .withParam(EquipmentSpecConstant.SEARCH, searchText)
                    .build();
            spec = spec.and(textSpec);
        }
        return jpaRepository.findAll(spec).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsBySerialNumber(SerialNumber serialNumber) {
        return jpaRepository.existsBySerialNumber(serialNumber.value());
    }

    @Override
    public boolean existsByUid(Uid uid) {
        return jpaRepository.existsByUid(uid.value());
    }

    @Override
    public Optional<Equipment> findBySerialNumber(SerialNumber serialNumber) {
        return jpaRepository.findBySerialNumber(serialNumber.value()).map(mapper::toDomain);
    }

    @Override
    public Optional<Equipment> findByUid(Uid uid) {
        return jpaRepository.findByUid(uid.value()).map(mapper::toDomain);
    }
}
