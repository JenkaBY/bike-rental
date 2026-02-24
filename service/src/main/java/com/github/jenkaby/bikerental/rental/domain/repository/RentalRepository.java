package com.github.jenkaby.bikerental.rental.domain.repository;

import com.github.jenkaby.bikerental.rental.domain.model.Rental;
import com.github.jenkaby.bikerental.rental.domain.model.RentalStatus;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Page;
import com.github.jenkaby.bikerental.shared.domain.model.vo.PageRequest;

import java.util.Optional;
import java.util.UUID;


public interface RentalRepository {
    
    Rental save(Rental rental);
    
    Optional<Rental> findById(Long id);
    
    boolean existsById(Long id);

    Page<Rental> findByStatus(RentalStatus status, PageRequest pageRequest);

    Page<Rental> findByStatusAndCustomerId(RentalStatus status, UUID customerId, PageRequest pageRequest);

    Page<Rental> findByCustomerId(UUID customerId, PageRequest pageRequest);
}
