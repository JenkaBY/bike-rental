package com.github.jenkaby.bikerental.finance.infrastructure.persistence.mapper;

import com.github.jenkaby.bikerental.finance.domain.model.SubLedgerRef;
import com.github.jenkaby.bikerental.finance.domain.model.TransactionRecord;
import com.github.jenkaby.bikerental.finance.infrastructure.persistence.entity.TransactionRecordJpaEntity;
import com.github.jenkaby.bikerental.shared.mapper.MoneyMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.UUID;

@Mapper(uses = {MoneyMapper.class})
public interface TransactionRecordMapper {

    @Mapping(target = "subLedgerRef", source = "subLedgerId")
    TransactionRecord toDomain(TransactionRecordJpaEntity entity);

    @Mapping(target = "transaction", ignore = true)
    @Mapping(target = "subLedgerId", source = "subLedgerRef.id")
    TransactionRecordJpaEntity toEntity(TransactionRecord domain);

    default SubLedgerRef toRef(UUID id) {
        return new SubLedgerRef(id);
    }
}
