package com.github.jenkaby.bikerental.customer.infrastructure.persistence.adapter;

import com.github.jenkaby.bikerental.customer.domain.model.Customer;
import com.github.jenkaby.bikerental.customer.domain.repository.CustomerRepository;
import com.github.jenkaby.bikerental.customer.infrastructure.persistence.mapper.CustomerJpaMapper;
import com.github.jenkaby.bikerental.customer.infrastructure.persistence.repository.CustomerJpaRepository;
import com.github.jenkaby.bikerental.customer.infrastructure.persistence.specification.CustomerSpec;
import com.github.jenkaby.bikerental.customer.infrastructure.persistence.specification.SpecConstant;
import net.kaczmarzyk.spring.data.jpa.utils.SpecificationBuilder;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
class CustomerRepositoryAdapter implements CustomerRepository {

    private static final int FIRST_PAGE = 0;
    private static final Sort SORT_BY_LAST_NAME_AND_FIRST_NAME_ASC = Sort.by(Sort.Direction.ASC, SpecConstant.LAST_NAME)
            .and(Sort.by(Sort.Direction.ASC, SpecConstant.FIRST_NAME));
    private final CustomerJpaRepository repository;
    private final CustomerJpaMapper mapper;

    CustomerRepositoryAdapter(CustomerJpaRepository repository, CustomerJpaMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Customer save(Customer customer) {
        var entity = mapper.toEntity(customer);
        var savedEntity = repository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Customer> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Customer> findByIds(Collection<UUID> ids) {
        return repository.findAllById(ids).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Customer> findByPhone(String phone) {
        return repository.findByPhone(phone).map(mapper::toDomain);
    }

    @Override
    public boolean existsByPhone(String phone) {
        return repository.existsByPhone(phone);
    }

    @Override
    public List<Customer> searchByPhone(String phone, int limit) {
        var pageable = org.springframework.data.domain.PageRequest.of(FIRST_PAGE, limit,
                SORT_BY_LAST_NAME_AND_FIRST_NAME_ASC);

        var customerSpec = SpecificationBuilder.specification(CustomerSpec.class)
                .withParam(SpecConstant.PHONE, phone);
        var spec = customerSpec.build();

        var springPage = repository.findAll(spec, pageable);
        return springPage.getContent().stream()
                .map(mapper::toDomain)
                .toList();
    }
}
