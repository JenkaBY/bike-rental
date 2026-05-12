package com.github.jenkaby.bikerental.rental.infrastructure.persistence.repository;

import com.github.jenkaby.bikerental.rental.infrastructure.persistence.entity.RentalEquipmentJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Set;

public interface RentalEquipmentJpaRepository extends JpaRepository<RentalEquipmentJpaEntity, Long> {

    @Query("SELECT re.equipmentId FROM RentalEquipmentJpaEntity re " +
            "WHERE re.equipmentId IN :candidateIds AND re.status IN :statuses")
    Set<Long> findEquipmentIdsByEquipmentIdInAndStatusIn(
            @Param("candidateIds") Set<Long> candidateIds,
            @Param("statuses") Set<String> statuses);
}