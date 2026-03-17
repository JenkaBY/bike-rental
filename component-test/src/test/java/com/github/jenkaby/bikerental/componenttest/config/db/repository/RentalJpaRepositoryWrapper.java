package com.github.jenkaby.bikerental.componenttest.config.db.repository;

import com.github.jenkaby.bikerental.rental.infrastructure.persistence.entity.RentalEquipmentJpaEntity;
import com.github.jenkaby.bikerental.rental.infrastructure.persistence.entity.RentalJpaEntity;
import com.github.jenkaby.bikerental.rental.infrastructure.persistence.repository.RentalJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Repository
public class RentalJpaRepositoryWrapper {

    private final RentalJpaRepository rentalJpaRepository;

    @Transactional
    public List<RentalJpaEntity> findAll() {
        return rentalJpaRepository.findAll().stream()
                .peek(entity -> entity.getRentalEquipments().forEach(RentalEquipmentJpaEntity::getId))
                .toList();
    }
}
