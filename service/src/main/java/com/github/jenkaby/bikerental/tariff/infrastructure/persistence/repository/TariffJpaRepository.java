package com.github.jenkaby.bikerental.tariff.infrastructure.persistence.repository;

import com.github.jenkaby.bikerental.tariff.infrastructure.persistence.entity.TariffJpaEntity;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TariffJpaRepository extends JpaRepository<TariffJpaEntity, Long> {

    @Query("SELECT t FROM TariffJpaEntity t WHERE t.equipmentTypeSlug = :typeSlug AND t.status = 'ACTIVE'")
    List<TariffJpaEntity> findActiveByEquipmentType(@Param("typeSlug") String typeSlug);

    @Override
    Page<TariffJpaEntity> findAll(@NonNull Pageable pageable);
}
