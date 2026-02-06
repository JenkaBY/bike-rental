package com.github.jenkaby.bikerental.rental.infrastructure.persistence.adapter;

import com.github.jenkaby.bikerental.rental.domain.model.Rental;
import com.github.jenkaby.bikerental.rental.domain.repository.RentalRepository;
import com.github.jenkaby.bikerental.rental.infrastructure.persistence.mapper.RentalJpaMapper;
import com.github.jenkaby.bikerental.rental.infrastructure.persistence.repository.RentalJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
class RentalRepositoryAdapter implements RentalRepository {

    private final RentalJpaRepository repository;
    private final RentalJpaMapper mapper;

    RentalRepositoryAdapter(RentalJpaRepository repository, RentalJpaMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Rental save(Rental rental) {
        var entity = mapper.toEntity(rental);
        var savedEntity = repository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Rental> findById(Long id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public boolean existsById(Long id) {
        return repository.existsById(id);
    }
}
