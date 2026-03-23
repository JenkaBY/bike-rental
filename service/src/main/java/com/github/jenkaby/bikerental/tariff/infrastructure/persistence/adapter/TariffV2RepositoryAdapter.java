package com.github.jenkaby.bikerental.tariff.infrastructure.persistence.adapter;

import com.github.jenkaby.bikerental.shared.domain.model.vo.Page;
import com.github.jenkaby.bikerental.shared.domain.model.vo.PageRequest;
import com.github.jenkaby.bikerental.shared.exception.ResourceNotFoundException;
import com.github.jenkaby.bikerental.shared.mapper.PageMapper;
import com.github.jenkaby.bikerental.tariff.domain.model.TariffV2;
import com.github.jenkaby.bikerental.tariff.domain.repository.TariffV2Repository;
import com.github.jenkaby.bikerental.tariff.infrastructure.persistence.entity.TariffV2JpaEntity;
import com.github.jenkaby.bikerental.tariff.infrastructure.persistence.mapper.TariffV2JpaMapper;
import com.github.jenkaby.bikerental.tariff.infrastructure.persistence.repository.TariffV2JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
class TariffV2RepositoryAdapter implements TariffV2Repository {

    private final TariffV2JpaRepository jpaRepository;
    private final TariffV2JpaMapper mapper;
    private final PageMapper pageMapper;

    TariffV2RepositoryAdapter(TariffV2JpaRepository jpaRepository, TariffV2JpaMapper mapper, PageMapper pageMapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
        this.pageMapper = pageMapper;
    }

    @Override
    public TariffV2 save(TariffV2 tariff) {
        TariffV2JpaEntity entity = mapper.toEntity(tariff);
        TariffV2JpaEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<TariffV2> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Page<TariffV2> findAll(PageRequest pageRequest) {
        var pageable = pageMapper.toSpring(pageRequest);
        var page = jpaRepository.findAll(pageable);
        return pageMapper.toDomain(page).map(mapper::toDomain);
    }

    @Override
    public List<TariffV2> findActiveForEquipmentType(String equipmentTypeSlug) {
        return jpaRepository.findActiveByEquipmentType(equipmentTypeSlug).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<TariffV2> findActiveByEquipmentTypeAndValidOn(String equipmentTypeSlug, LocalDate date) {
        return jpaRepository.findActiveByEquipmentTypeAndValidOn(equipmentTypeSlug, date).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public TariffV2 get(Long id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain)
                .orElseThrow(() -> new ResourceNotFoundException(TariffV2.class, id.toString()));
    }
}
