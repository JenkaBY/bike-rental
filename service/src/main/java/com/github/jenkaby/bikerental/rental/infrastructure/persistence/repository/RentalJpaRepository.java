package com.github.jenkaby.bikerental.rental.infrastructure.persistence.repository;

import com.github.jenkaby.bikerental.rental.infrastructure.persistence.entity.RentalJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface RentalJpaRepository extends JpaRepository<RentalJpaEntity, Long> {

    Page<RentalJpaEntity> findByStatus(String status, Pageable pageable);

    Page<RentalJpaEntity> findByStatusAndCustomerId(String status, UUID customerId, Pageable pageable);

    Page<RentalJpaEntity> findByCustomerId(UUID customerId, Pageable pageable);

    @Query(value =
            "SELECT DISTINCT r FROM RentalJpaEntity r LEFT JOIN r.rentalEquipments re " +
                    "WHERE r.status = :status AND re.equipmentUid = :equipmentUid",
            countQuery =
                    "SELECT count(DISTINCT r) FROM RentalJpaEntity r LEFT JOIN r.rentalEquipments re " +
                            "WHERE r.status = :status AND re.equipmentUid = :equipmentUid")
    Page<RentalJpaEntity> findByStatusAndEquipmentUid(@Param("status") String status, @Param("equipmentUid") String equipmentUid, Pageable pageable);
}
