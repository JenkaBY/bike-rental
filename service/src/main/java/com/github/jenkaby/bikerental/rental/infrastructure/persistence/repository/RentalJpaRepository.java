package com.github.jenkaby.bikerental.rental.infrastructure.persistence.repository;

import com.github.jenkaby.bikerental.rental.infrastructure.persistence.entity.RentalJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RentalJpaRepository extends JpaRepository<RentalJpaEntity, Long> {
}
