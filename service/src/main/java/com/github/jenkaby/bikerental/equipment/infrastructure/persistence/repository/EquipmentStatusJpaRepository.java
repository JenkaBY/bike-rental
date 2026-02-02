package com.github.jenkaby.bikerental.equipment.infrastructure.persistence.repository;

import com.github.jenkaby.bikerental.equipment.infrastructure.persistence.entity.EquipmentStatusJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EquipmentStatusJpaRepository extends JpaRepository<EquipmentStatusJpaEntity, Long> {
    Optional<EquipmentStatusJpaEntity> findBySlug(String slug);

    boolean existsBySlug(String slug);
}
