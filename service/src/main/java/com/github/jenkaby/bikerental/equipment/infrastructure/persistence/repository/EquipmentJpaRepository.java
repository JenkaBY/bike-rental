package com.github.jenkaby.bikerental.equipment.infrastructure.persistence.repository;

import com.github.jenkaby.bikerental.equipment.infrastructure.persistence.entity.EquipmentJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface EquipmentJpaRepository extends JpaRepository<EquipmentJpaEntity, Long> {
    Optional<EquipmentJpaEntity> findBySerialNumber(String serialNumber);

    Optional<EquipmentJpaEntity> findByUid(String uid);

    boolean existsBySerialNumber(String serialNumber);

    @Query("SELECT e FROM EquipmentJpaEntity e WHERE " +
            "(:statusSlug IS NULL OR e.statusSlug = :statusSlug) AND " +
            "(:typeSlug IS NULL OR e.equipmentTypeSlug = :typeSlug)")
    Page<EquipmentJpaEntity> findAllByFilters(@Param("statusSlug") String statusSlug, @Param("typeSlug") String typeSlug,
                                              Pageable pageRequest);
}
