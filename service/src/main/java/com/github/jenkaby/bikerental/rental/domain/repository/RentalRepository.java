package com.github.jenkaby.bikerental.rental.domain.repository;

import com.github.jenkaby.bikerental.rental.domain.model.Rental;
import com.github.jenkaby.bikerental.rental.domain.model.RentalSearchFilter;
import com.github.jenkaby.bikerental.shared.domain.CustomerRef;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Page;
import com.github.jenkaby.bikerental.shared.domain.model.vo.PageRequest;

import java.util.List;
import java.util.Optional;


public interface RentalRepository {
    
    Rental save(Rental rental);
    
    Optional<Rental> findById(Long id);
    
    boolean existsById(Long id);

    Page<Rental> findAll(RentalSearchFilter filter, PageRequest pageRequest);

    List<Rental> getCustomerDebtRentals(CustomerRef customerRef);
}
