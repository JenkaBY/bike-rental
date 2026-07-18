package com.github.jenkaby.bikerental.finance.web.query.mapper;

import com.github.jenkaby.bikerental.finance.domain.model.LedgerType;
import com.github.jenkaby.bikerental.finance.domain.model.Transaction;
import com.github.jenkaby.bikerental.finance.domain.model.TransactionFilter;
import com.github.jenkaby.bikerental.finance.domain.model.TransactionRecord;
import com.github.jenkaby.bikerental.finance.web.query.dto.TransactionFilterParams;
import com.github.jenkaby.bikerental.finance.web.query.dto.TransactionSummaryResponse;
import com.github.jenkaby.bikerental.shared.mapper.MoneyMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Set;
import java.util.UUID;

@Mapper(uses = {MoneyMapper.class})
public interface TransactionQueryMapper {

    @Mapping(target = "entries", source = "records")
    TransactionSummaryResponse toResponse(Transaction transaction);

    TransactionSummaryResponse.TransactionEntryResponse toEntryResponse(TransactionRecord record);

    default TransactionFilter toFilter(TransactionFilterParams params) {
        Set<UUID> customerIds = params.customerIds() == null ? Set.of() : params.customerIds();
        Set<LedgerType> ledgerTypes = params.ledgerTypes() == null ? Set.of() : params.ledgerTypes();
        return new TransactionFilter(
                customerIds,
                params.fromDate(),
                params.toDate(),
                params.sourceId(),
                params.sourceType(),
                ledgerTypes);
    }
}
