package com.github.jenkaby.bikerental.finance.infrastructure.persistence.adapter;

import com.github.jenkaby.bikerental.finance.domain.model.Transaction;
import com.github.jenkaby.bikerental.finance.domain.model.TransactionHistoryFilter;
import com.github.jenkaby.bikerental.finance.domain.model.TransactionSourceType;
import com.github.jenkaby.bikerental.finance.domain.model.TransactionType;
import com.github.jenkaby.bikerental.finance.domain.repository.TransactionRepository;
import com.github.jenkaby.bikerental.finance.infrastructure.persistence.mapper.TransactionJpaMapper;
import com.github.jenkaby.bikerental.finance.infrastructure.persistence.repository.TransactionJpaRepository;
import com.github.jenkaby.bikerental.finance.infrastructure.persistence.specification.CustomerTransactionsSpec;
import com.github.jenkaby.bikerental.finance.infrastructure.persistence.specification.CustomerTransactionsSpecParamsMapper;
import com.github.jenkaby.bikerental.finance.infrastructure.persistence.specification.SpecConstant;
import com.github.jenkaby.bikerental.shared.domain.CustomerRef;
import com.github.jenkaby.bikerental.shared.domain.IdempotencyKey;
import com.github.jenkaby.bikerental.shared.domain.RentalId;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Page;
import com.github.jenkaby.bikerental.shared.domain.model.vo.PageRequest;
import lombok.extern.slf4j.Slf4j;
import net.kaczmarzyk.spring.data.jpa.utils.SpecificationBuilder;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Repository
class TransactionRepositoryAdapter implements TransactionRepository {

    private final TransactionJpaRepository jpaRepository;
    private final TransactionJpaMapper mapper;
    private final CustomerTransactionsSpecParamsMapper specParamsMapper;

    TransactionRepositoryAdapter(TransactionJpaRepository jpaRepository, TransactionJpaMapper mapper, CustomerTransactionsSpecParamsMapper specParamsMapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
        this.specParamsMapper = specParamsMapper;
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
}
