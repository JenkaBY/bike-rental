package com.github.jenkaby.bikerental.rental.infrastructure.persistence.repository;

import com.github.jenkaby.bikerental.rental.domain.model.RentalStatus;
import com.github.jenkaby.bikerental.rental.infrastructure.persistence.entity.RentalJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.UUID;

public interface RentalJpaRepository extends JpaRepository<RentalJpaEntity, Long>,
        JpaSpecificationExecutor<RentalJpaEntity> {

    List<RentalJpaEntity> findAllByCustomerIdAndStatusOrderByCreatedAtAsc(UUID customerId, RentalStatus status);
}
