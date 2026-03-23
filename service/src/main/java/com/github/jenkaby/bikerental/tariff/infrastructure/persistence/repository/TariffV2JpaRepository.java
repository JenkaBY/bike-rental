package com.github.jenkaby.bikerental.tariff.infrastructure.persistence.repository;

import com.github.jenkaby.bikerental.tariff.infrastructure.persistence.entity.TariffV2JpaEntity;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface TariffV2JpaRepository extends JpaRepository<TariffV2JpaEntity, Long> {

    @Query("SELECT t FROM TariffV2JpaEntity t WHERE t.equipmentType = :typeSlug AND t.status = 'ACTIVE'")
    List<TariffV2JpaEntity> findActiveByEquipmentType(@Param("typeSlug") String typeSlug);

    @Query("SELECT t FROM TariffV2JpaEntity t WHERE t.equipmentType = :typeSlug AND t.status = 'ACTIVE' " +
            "AND t.validFrom <= :validTo AND (t.validTo IS NULL OR t.validTo >= :validTo)")
    List<TariffV2JpaEntity> findActiveByEquipmentTypeAndValidOn(
            @Param("typeSlug") String typeSlug,
            @Param("validTo") LocalDate validTo);

    @Override
    Page<TariffV2JpaEntity> findAll(@NonNull Pageable pageable);
}
