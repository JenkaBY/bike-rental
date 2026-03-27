package com.github.jenkaby.bikerental.finance.infrastructure.persistence.mapper;

import com.github.jenkaby.bikerental.finance.domain.model.TransactionRecord;
import com.github.jenkaby.bikerental.finance.infrastructure.persistence.entity.TransactionRecordJpaEntity;
import com.github.jenkaby.bikerental.shared.mapper.MoneyMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(uses = {MoneyMapper.class})
public interface TransactionRecordMapper {

    TransactionRecord toDomain(TransactionRecordJpaEntity entity);

    @Mapping(target = "transaction", ignore = true)
    TransactionRecordJpaEntity toEntity(TransactionRecord domain);
}
