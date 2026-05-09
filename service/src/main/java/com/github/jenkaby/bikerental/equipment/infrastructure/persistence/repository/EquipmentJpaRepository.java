package com.github.jenkaby.bikerental.equipment.infrastructure.persistence.repository;

import com.github.jenkaby.bikerental.equipment.infrastructure.persistence.entity.EquipmentJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface EquipmentJpaRepository extends JpaRepository<EquipmentJpaEntity, Long>,
        JpaSpecificationExecutor<EquipmentJpaEntity> {

        Optional<EquipmentJpaEntity> findBySerialNumber(String serialNumber);

        Optional<EquipmentJpaEntity> findByUid(String uid);

        boolean existsBySerialNumber(String serialNumber);

        boolean existsByUid(String uid);

        List<EquipmentJpaEntity> findAllByIdIn(Collection<Long> ids);
}
