package com.github.jenkaby.bikerental.finance.infrastructure.persistence.adapter;

import com.github.jenkaby.bikerental.finance.domain.model.*;
import com.github.jenkaby.bikerental.finance.domain.repository.TransactionRepository;
import com.github.jenkaby.bikerental.finance.infrastructure.persistence.mapper.TransactionJpaMapper;
import com.github.jenkaby.bikerental.finance.domain.model.TransactionSortField;
import com.github.jenkaby.bikerental.finance.infrastructure.persistence.entity.TransactionJpaEntity;
import com.github.jenkaby.bikerental.finance.infrastructure.persistence.repository.TransactionJpaRepository;
import com.github.jenkaby.bikerental.finance.infrastructure.persistence.specification.CustomerTransactionsSpec;
import com.github.jenkaby.bikerental.finance.infrastructure.persistence.specification.CustomerTransactionsSpecParamsMapper;
import com.github.jenkaby.bikerental.finance.infrastructure.persistence.specification.LedgerTypeExistsSpecification;
import com.github.jenkaby.bikerental.finance.infrastructure.persistence.specification.SpecConstant;
import com.github.jenkaby.bikerental.finance.infrastructure.persistence.specification.TransactionsSpec;
import com.github.jenkaby.bikerental.finance.infrastructure.persistence.specification.TransactionsSpecParamsMapper;
import com.github.jenkaby.bikerental.shared.domain.CustomerRef;
import com.github.jenkaby.bikerental.shared.domain.IdempotencyKey;
import com.github.jenkaby.bikerental.shared.domain.RentalId;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Page;
import com.github.jenkaby.bikerental.shared.domain.model.vo.PageRequest;
import lombok.extern.slf4j.Slf4j;
import net.kaczmarzyk.spring.data.jpa.utils.SpecificationBuilder;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Repository
class TransactionRepositoryAdapter implements TransactionRepository {

    private static final Map<String, String> ENTITY_PROPERTY_BY_SORT_FIELD = Map.of(
            TransactionSortField.RECORDED_AT.apiName(), SpecConstant.TransactionField.RECORDED_AT,
            TransactionSortField.AMOUNT.apiName(), SpecConstant.TransactionField.AMOUNT,
            TransactionSortField.TYPE.apiName(), SpecConstant.TransactionField.TRANSACTION_TYPE);

    private final TransactionJpaRepository jpaRepository;
    private final TransactionJpaMapper mapper;
    private final CustomerTransactionsSpecParamsMapper specParamsMapper;
    private final TransactionsSpecParamsMapper transactionsSpecParamsMapper;

    TransactionRepositoryAdapter(TransactionJpaRepository jpaRepository, TransactionJpaMapper mapper,
                                 CustomerTransactionsSpecParamsMapper specParamsMapper,
                                 TransactionsSpecParamsMapper transactionsSpecParamsMapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
        this.specParamsMapper = specParamsMapper;
        this.transactionsSpecParamsMapper = transactionsSpecParamsMapper;
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public Transaction save(Transaction transaction) {
        var entity = mapper.toEntity(transaction);
        var saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Transaction> findByIdempotencyKeyAndCustomerId(IdempotencyKey idempotencyKey, CustomerRef customerId) {
        return jpaRepository.findByIdempotencyKeyAndCustomerId(idempotencyKey.id(), customerId.id())
                .map(mapper::toDomain);
    }

    @Override
    public Optional<Transaction> findByIdempotencyKey(IdempotencyKey idempotencyKey) {
        return jpaRepository.findByIdempotencyKey(idempotencyKey.id())
                .map(mapper::toDomain);
    }

    @Override
    public Optional<Transaction> findByRentalRefAndType(RentalId rentalRef, TransactionType type) {
        return findAllByRentalRefAndTypes(rentalRef, Set.of(type)).stream()
                .findFirst();
    }

    @Override
    public List<Transaction> findAllByRentalRefAndTypes(RentalId rentalRef, Set<TransactionType> types) {
        return jpaRepository.findAllBySourceTypeAndSourceIdAndTransactionTypeIn(
                        TransactionSourceType.RENTAL,
                        String.valueOf(rentalRef.id()),
                        types)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Transaction> findTransactionHistory(CustomerRef customerId, TransactionHistoryFilter filter, PageRequest pageRequest) {
        var pageable = org.springframework.data.domain.PageRequest.of(pageRequest.page(), pageRequest.size(),
                Sort.by(Sort.Direction.DESC, SpecConstant.TransactionField.RECORDED_AT));

        var txnSpec = SpecificationBuilder.specification(CustomerTransactionsSpec.class)
                .withParam(SpecConstant.TransactionField.CUSTOMER_ID, customerId.id().toString());
        specParamsMapper.toParams(filter).forEach(txnSpec::withParam);
        var spec = txnSpec.build();

        var springPage = jpaRepository.findAll(spec, pageable);
        var items = springPage.getContent().stream()
                .map(mapper::toDomain)
                .toList();
        return new Page<>(items, springPage.getTotalElements(), pageRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Transaction> findTransactions(TransactionFilter filter, PageRequest pageRequest) {
        var pageable = org.springframework.data.domain.PageRequest.of(
                pageRequest.page(), pageRequest.size(), toEntitySort(pageRequest.sort()));

        var specBuilder = SpecificationBuilder.specification(TransactionsSpec.class);
        transactionsSpecParamsMapper.toParams(filter).forEach(specBuilder::withParam);
        if (!filter.customerIds().isEmpty()) {
            specBuilder.withParam(SpecConstant.TransactionField.CUSTOMER_IDS, toStringArray(filter.customerIds()));
        }
        Specification<TransactionJpaEntity> spec = specBuilder.build();
        if (!filter.ledgerTypes().isEmpty()) {
            spec = spec.and(LedgerTypeExistsSpecification.forLedgerTypes(filter.ledgerTypes()));
        }

        var springPage = jpaRepository.findAll(spec, pageable);
        var items = springPage.getContent().stream()
                .map(mapper::toDomain)
                .toList();
        return new Page<>(items, springPage.getTotalElements(), pageRequest);
    }

    private Sort toEntitySort(com.github.jenkaby.bikerental.shared.domain.model.vo.Sort sort) {
        if (!sort.isSorted()) {
            return Sort.by(Sort.Direction.DESC, SpecConstant.TransactionField.RECORDED_AT);
        }
        var orders = sort.orders().stream()
                .map(order -> new Sort.Order(
                        order.direction().isDescending() ? Sort.Direction.DESC : Sort.Direction.ASC,
                        ENTITY_PROPERTY_BY_SORT_FIELD.getOrDefault(order.property(), order.property())))
                .toList();
        return Sort.by(orders);
    }

    private String[] toStringArray(Collection<UUID> ids) {
        return ids.stream().map(UUID::toString).toArray(String[]::new);
    }

    @Override
    public Map<LedgerType, Money> findLatestLedgerBalancesBefore(CustomerRef customerId, Instant before) {
        return jpaRepository.findLatestCustomerBucketBalancesBefore(customerId.id(), before).stream()
                .collect(Collectors.toMap(
                        projection -> LedgerType.valueOf(projection.getLedgerType()),
                        projection -> Money.of(projection.getRunningBalance())));
    }
}
