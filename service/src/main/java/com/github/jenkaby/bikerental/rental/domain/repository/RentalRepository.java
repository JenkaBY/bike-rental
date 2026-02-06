package com.github.jenkaby.bikerental.rental.domain.repository;

import com.github.jenkaby.bikerental.rental.domain.model.Rental;

import java.util.Optional;


public interface RentalRepository {
    
    Rental save(Rental rental);
    
    Optional<Rental> findById(Long id);
    
    boolean existsById(Long id);
}
