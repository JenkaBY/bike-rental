package com.github.jenkaby.bikerental.rental.infrastructure.persistence.repository;

import com.github.jenkaby.bikerental.rental.infrastructure.persistence.entity.RentalJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RentalJpaRepository extends JpaRepository<RentalJpaEntity, Long> {

    Page<RentalJpaEntity> findByStatus(String status, Pageable pageable);

    Page<RentalJpaEntity> findByStatusAndCustomerId(String status, UUID customerId, Pageable pageable);

    Page<RentalJpaEntity> findByCustomerId(UUID customerId, Pageable pageable);

    Page<RentalJpaEntity> findByStatusAndEquipmentUid(String status, String equipmentUid, Pageable pageable);
}
