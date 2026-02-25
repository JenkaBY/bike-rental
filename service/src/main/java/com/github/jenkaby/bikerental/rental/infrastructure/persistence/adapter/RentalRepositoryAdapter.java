package com.github.jenkaby.bikerental.rental.infrastructure.persistence.adapter;

import com.github.jenkaby.bikerental.rental.domain.model.Rental;
import com.github.jenkaby.bikerental.rental.domain.model.RentalStatus;
import com.github.jenkaby.bikerental.rental.domain.repository.RentalRepository;
import com.github.jenkaby.bikerental.rental.infrastructure.persistence.mapper.RentalJpaMapper;
import com.github.jenkaby.bikerental.rental.infrastructure.persistence.repository.RentalJpaRepository;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Page;
import com.github.jenkaby.bikerental.shared.domain.model.vo.PageRequest;
import com.github.jenkaby.bikerental.shared.mapper.PageMapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
class RentalRepositoryAdapter implements RentalRepository {

    private final RentalJpaRepository repository;
    private final RentalJpaMapper mapper;
    private final PageMapper pageMapper;

    RentalRepositoryAdapter(RentalJpaRepository repository, RentalJpaMapper mapper, PageMapper pageMapper) {
        this.repository = repository;
        this.mapper = mapper;
        this.pageMapper = pageMapper;
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

    @Override
    public Page<Rental> findByStatus(RentalStatus status, PageRequest pageRequest) {
        var springPageRequest = pageMapper.toSpring(pageRequest);
        var page = repository.findByStatus(status.name(), springPageRequest);
        return pageMapper.toDomain(page)
                .map(mapper::toDomain);
    }

    @Override
    public Page<Rental> findByStatusAndCustomerId(RentalStatus status, UUID customerId, PageRequest pageRequest) {
        var springPageRequest = pageMapper.toSpring(pageRequest);
        var page = repository.findByStatusAndCustomerId(status.name(), customerId, springPageRequest);
        return pageMapper.toDomain(page)
                .map(mapper::toDomain);
    }

    @Override
    public Page<Rental> findByCustomerId(UUID customerId, PageRequest pageRequest) {
        var springPageRequest = pageMapper.toSpring(pageRequest);
        var page = repository.findByCustomerId(customerId, springPageRequest);
        return pageMapper.toDomain(page)
                .map(mapper::toDomain);
    }

    @Override
    public Page<Rental> findByStatusAndEquipmentUid(RentalStatus status, String equipmentUid, PageRequest pageRequest) {
        var springPageRequest = pageMapper.toSpring(pageRequest);
        var page = repository.findByStatusAndEquipmentUid(status.name(), equipmentUid, springPageRequest);
        return pageMapper.toDomain(page)
                .map(mapper::toDomain);
    }
}
