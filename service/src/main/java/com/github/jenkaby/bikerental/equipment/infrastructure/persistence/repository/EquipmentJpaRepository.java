package com.github.jenkaby.bikerental.equipment.infrastructure.persistence.repository;

import com.github.jenkaby.bikerental.equipment.infrastructure.persistence.entity.EquipmentJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EquipmentJpaRepository extends JpaRepository<EquipmentJpaEntity, Long> {
    Optional<EquipmentJpaEntity> findBySerialNumber(String serialNumber);

    Optional<EquipmentJpaEntity> findByUid(String uid);

    boolean existsBySerialNumber(String serialNumber);
}
