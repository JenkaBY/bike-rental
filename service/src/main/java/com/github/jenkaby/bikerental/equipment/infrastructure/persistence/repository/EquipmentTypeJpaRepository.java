package com.github.jenkaby.bikerental.equipment.infrastructure.persistence.repository;

import com.github.jenkaby.bikerental.equipment.infrastructure.persistence.entity.EquipmentTypeJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EquipmentTypeJpaRepository extends JpaRepository<EquipmentTypeJpaEntity, Long> {
    Optional<EquipmentTypeJpaEntity> findBySlug(String slug);

    boolean existsBySlug(String slug);
}
