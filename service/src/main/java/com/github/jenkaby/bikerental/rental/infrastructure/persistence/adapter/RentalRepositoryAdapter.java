package com.github.jenkaby.bikerental.rental.infrastructure.persistence.adapter;

import com.github.jenkaby.bikerental.rental.domain.model.Rental;
import com.github.jenkaby.bikerental.rental.domain.model.RentalSearchFilter;
import com.github.jenkaby.bikerental.rental.domain.model.RentalStatus;
import com.github.jenkaby.bikerental.rental.domain.repository.RentalRepository;
import com.github.jenkaby.bikerental.rental.infrastructure.persistence.mapper.RentalJpaMapper;
import com.github.jenkaby.bikerental.rental.infrastructure.persistence.repository.RentalJpaRepository;
import com.github.jenkaby.bikerental.rental.infrastructure.persistence.specification.RentalSpec;
import com.github.jenkaby.bikerental.shared.domain.CustomerRef;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Page;
import com.github.jenkaby.bikerental.shared.domain.model.vo.PageRequest;
import com.github.jenkaby.bikerental.shared.mapper.PageMapper;
import net.kaczmarzyk.spring.data.jpa.utils.SpecificationBuilder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional(readOnly = true)
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
    @Transactional
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
    public Page<Rental> findAll(RentalSearchFilter filter, PageRequest pageRequest) {
        var pageable = pageMapper.toSpring(pageRequest);
        var specBuilder = SpecificationBuilder.specification(RentalSpec.class);
        filter.toMap().forEach(specBuilder::withParam);
        var spec = specBuilder.build();
        var page = repository.findAll(spec, pageable);
        return pageMapper.toDomain(page)
                .map(mapper::toDomain);
    }

    @Override
    public List<Rental> getCustomerDebtRentals(CustomerRef customerRef) {
        return repository
                .findAllByCustomerIdAndStatusOrderByCreatedAtAsc(customerRef.id(), RentalStatus.DEBT)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
}
