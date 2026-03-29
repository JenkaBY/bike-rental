package com.github.jenkaby.bikerental.finance.infrastructure.persistence.mapper;

import com.github.jenkaby.bikerental.finance.domain.model.Transaction;
import com.github.jenkaby.bikerental.finance.infrastructure.persistence.entity.TransactionJpaEntity;
import com.github.jenkaby.bikerental.shared.mapper.IdempotencyKeyMapper;
import com.github.jenkaby.bikerental.shared.mapper.MoneyMapper;
import org.mapstruct.*;

@Mapper(uses = {MoneyMapper.class, IdempotencyKeyMapper.class, TransactionRecordMapper.class})
public interface TransactionJpaMapper {


    @Mapping(target = "records", source = "records")
    @Mapping(target = "type", source = "transactionType")
    Transaction toDomain(TransactionJpaEntity entity);

    @InheritInverseConfiguration
    TransactionJpaEntity toEntity(Transaction domain);

    @AfterMapping
    default void setTransactionRelationships(Transaction domain, @MappingTarget TransactionJpaEntity result) {
        if (result.getRecords() != null) {
            result.getRecords().forEach(rec -> rec.setTransaction(result));
        }
    }
}
