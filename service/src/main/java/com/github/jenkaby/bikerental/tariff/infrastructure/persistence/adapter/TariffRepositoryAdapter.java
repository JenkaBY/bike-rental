package com.github.jenkaby.bikerental.tariff.infrastructure.persistence.adapter;

import com.github.jenkaby.bikerental.shared.domain.model.vo.Page;
import com.github.jenkaby.bikerental.shared.domain.model.vo.PageRequest;
import com.github.jenkaby.bikerental.shared.exception.ResourceNotFoundException;
import com.github.jenkaby.bikerental.shared.mapper.PageMapper;
import com.github.jenkaby.bikerental.tariff.domain.model.Tariff;
import com.github.jenkaby.bikerental.tariff.domain.repository.TariffRepository;
import com.github.jenkaby.bikerental.tariff.infrastructure.persistence.entity.TariffJpaEntity;
import com.github.jenkaby.bikerental.tariff.infrastructure.persistence.mapper.TariffJpaMapper;
import com.github.jenkaby.bikerental.tariff.infrastructure.persistence.repository.TariffJpaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
class TariffRepositoryAdapter implements TariffRepository {

    private final TariffJpaRepository jpaRepository;
    private final TariffJpaMapper mapper;
    private final PageMapper pageMapper;

    TariffRepositoryAdapter(TariffJpaRepository jpaRepository, TariffJpaMapper mapper, PageMapper pageMapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
        this.pageMapper = pageMapper;
    }

    @Override
    public Tariff save(Tariff tariff) {
        TariffJpaEntity entity = mapper.toEntity(tariff);
        var saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Tariff> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Page<Tariff> findAll(PageRequest pageRequest) {
        Pageable pageable = pageMapper.toSpring(pageRequest);
        var page = jpaRepository.findAll(pageable);
        return pageMapper.toDomain(page).map(mapper::toDomain);
    }

    @Override
    public List<Tariff> findActiveForEquipmentType(String equipmentTypeSlug) {
        return jpaRepository.findActiveByEquipmentType(equipmentTypeSlug).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Tariff get(Long id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain)
                .orElseThrow(() -> new ResourceNotFoundException(Tariff.class, id.toString()));
    }
}
