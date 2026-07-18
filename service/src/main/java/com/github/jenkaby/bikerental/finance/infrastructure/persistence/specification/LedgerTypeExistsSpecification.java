package com.github.jenkaby.bikerental.finance.infrastructure.persistence.specification;

import com.github.jenkaby.bikerental.finance.domain.model.LedgerType;
import com.github.jenkaby.bikerental.finance.infrastructure.persistence.entity.TransactionJpaEntity;
import com.github.jenkaby.bikerental.finance.infrastructure.persistence.entity.TransactionRecordJpaEntity;
import org.springframework.data.jpa.domain.Specification;

import java.util.Collection;
import java.util.UUID;

public final class LedgerTypeExistsSpecification {

    private LedgerTypeExistsSpecification() {
    }

    public static Specification<TransactionJpaEntity> forLedgerTypes(Collection<LedgerType> ledgerTypes) {
        return (root, query, criteriaBuilder) -> {
            var subquery = query.subquery(UUID.class);
            var recordRoot = subquery.from(TransactionRecordJpaEntity.class);
            subquery.select(recordRoot.get("id"));
            subquery.where(
                    criteriaBuilder.equal(recordRoot.get("transaction"), root),
                    recordRoot.get(SpecConstant.TransactionField.LEDGER_TYPE).in(ledgerTypes));
            return criteriaBuilder.exists(subquery);
        };
    }
}
