package com.github.jenkaby.bikerental.finance.infrastructure.persistence.adapter;

import com.github.jenkaby.bikerental.finance.domain.model.*;
import com.github.jenkaby.bikerental.finance.domain.repository.TransactionRepository;
import com.github.jenkaby.bikerental.finance.infrastructure.persistence.entity.TransactionJpaEntity;
import com.github.jenkaby.bikerental.finance.infrastructure.persistence.mapper.TransactionJpaMapper;
import com.github.jenkaby.bikerental.finance.infrastructure.persistence.repository.TransactionJpaRepository;
import com.github.jenkaby.bikerental.shared.domain.CustomerRef;
import com.github.jenkaby.bikerental.shared.domain.IdempotencyKey;
import com.github.jenkaby.bikerental.shared.domain.RentalRef;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

@Repository
class TransactionRepositoryAdapter implements TransactionRepository {

    private final TransactionJpaRepository jpaRepository;
    private final TransactionJpaMapper mapper;

    TransactionRepositoryAdapter(TransactionJpaRepository jpaRepository, TransactionJpaMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
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
    public Optional<Transaction> findByRentalRefAndType(RentalRef rentalRef, TransactionType type) {
        return findAllByRentalRefAndType(rentalRef, type).stream()
                .findFirst();
    }

    @Override
    public List<Transaction> findAllByRentalRefAndType(RentalRef rentalRef, TransactionType type) {
        return jpaRepository.findAllBySourceTypeAndSourceIdAndTransactionType(
                        TransactionSourceType.RENTAL,
                        String.valueOf(rentalRef.id()),
                        type)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public com.github.jenkaby.bikerental.shared.domain.model.vo.Page<Transaction> findTransactionHistory(
            CustomerRef customerId,
            TransactionHistoryFilter filter,
            com.github.jenkaby.bikerental.shared.domain.model.vo.PageRequest pageRequest) {
        var pageable = org.springframework.data.domain.PageRequest.of(
                pageRequest.page(),
                pageRequest.size(),
                Sort.by(Sort.Direction.DESC, "recordedAt"));

        Specification<TransactionJpaEntity> spec =
                customerSubLedgerScope(customerId).and(filterSpec(filter));

        var springPage = jpaRepository.findAll(spec, pageable);
        var items = springPage.getContent().stream().map(mapper::toDomain).toList();
        return new com.github.jenkaby.bikerental.shared.domain.model.vo.Page<>(items, springPage.getTotalElements(), pageRequest);
    }

    private static Specification<TransactionJpaEntity> customerSubLedgerScope(CustomerRef customerId) {
        return (root, query, cb) -> {
            var customerPredicate = cb.equal(root.get("customerId"), customerId.id());

            var recordsJoin = root.join("records", JoinType.INNER);
            var customerLedgerTypes = List.of(LedgerType.CUSTOMER_WALLET, LedgerType.CUSTOMER_HOLD);
            var ledgerPredicate = recordsJoin.get("ledgerType").in(customerLedgerTypes);
            return cb.and(customerPredicate, ledgerPredicate);
        };
    }

    private static Specification<TransactionJpaEntity> filterSpec(TransactionHistoryFilter filter) {
        Specification<TransactionJpaEntity> spec = (root, query, cb) -> null;
        if (filter.fromDate() != null) {
            var from = filter.fromDate().atStartOfDay().toInstant(ZoneOffset.UTC);
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("recordedAt"), from));
        }
        if (filter.toDate() != null) {
            var to = filter.toDate().plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);
            spec = spec.and((root, query, cb) -> cb.lessThan(root.get("recordedAt"), to));
        }
        if (filter.sourceId() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("sourceId"), filter.sourceId()));
        }
        if (filter.sourceType() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("sourceType"), filter.sourceType()));
        }
        return spec;
    }
}
