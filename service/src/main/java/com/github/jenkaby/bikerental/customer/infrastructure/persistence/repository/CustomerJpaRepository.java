package com.github.jenkaby.bikerental.customer.infrastructure.persistence.repository;

import com.github.jenkaby.bikerental.customer.infrastructure.persistence.entity.CustomerJpaEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CustomerJpaRepository extends JpaRepository<CustomerJpaEntity, UUID> {
    Optional<CustomerJpaEntity> findByPhone(String phone);

    boolean existsByPhone(String phone);

    List<CustomerJpaEntity> findByPhoneContaining(String phone, Pageable pageable);
}
