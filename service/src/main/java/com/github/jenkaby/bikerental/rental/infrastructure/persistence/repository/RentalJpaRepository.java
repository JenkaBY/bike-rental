package com.github.jenkaby.bikerental.rental.infrastructure.persistence.repository;

import com.github.jenkaby.bikerental.rental.infrastructure.persistence.entity.RentalJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface RentalJpaRepository extends JpaRepository<RentalJpaEntity, Long> {

    Page<RentalJpaEntity> findByStatus(String status, Pageable pageable);

    Page<RentalJpaEntity> findByStatusAndCustomerId(String status, UUID customerId, Pageable pageable);

    Page<RentalJpaEntity> findByCustomerId(UUID customerId, Pageable pageable);

    @Query(value =
            "SELECT r.* FROM rentals r LEFT JOIN rental_equipments re ON r.id = re.rental_id " +
                    "WHERE r.status = :status AND re.equipment_uid = :equipmentUid",
            countQuery =
                    "SELECT count(DISTINCT r.id) FROM rentals r " +
                            "LEFT JOIN rental_equipments re ON r.id = re.rental_id " +
                            "WHERE r.status = :status AND re.equipment_uid = :equipmentUid",
            nativeQuery = true)
    Page<RentalJpaEntity> findByStatusAndEquipmentUid(String status, String equipmentUid, Pageable pageable);
}
