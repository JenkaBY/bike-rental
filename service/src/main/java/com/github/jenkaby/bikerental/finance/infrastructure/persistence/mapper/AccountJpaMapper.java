package com.github.jenkaby.bikerental.finance.infrastructure.persistence.mapper;

import com.github.jenkaby.bikerental.finance.domain.model.Account;
import com.github.jenkaby.bikerental.finance.domain.model.SubLedger;
import com.github.jenkaby.bikerental.finance.infrastructure.persistence.entity.AccountJpaEntity;
import com.github.jenkaby.bikerental.finance.infrastructure.persistence.entity.SubLedgerJpaEntity;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(uses = {com.github.jenkaby.bikerental.shared.mapper.CustomerRefMapper.class})
public interface AccountJpaMapper {

    @Mapping(target = "customerRef", source = "customerId")
    Account toDomain(AccountJpaEntity entity);

    @Mapping(target = "customerId", source = "customerRef")
    @Mapping(target = "createdAt", ignore = true)
    AccountJpaEntity toEntity(Account domain);

    @AfterMapping
    default void setSubLedgerRelationships(Account domain, @MappingTarget AccountJpaEntity result) {
        if (result.getSubLedgers() != null) {
            result.getSubLedgers().forEach(sl -> sl.setAccount(result));
        }
    }

    SubLedger toDomain(SubLedgerJpaEntity entity);

    @Mapping(target = "account", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    SubLedgerJpaEntity toEntity(SubLedger domain);
}
