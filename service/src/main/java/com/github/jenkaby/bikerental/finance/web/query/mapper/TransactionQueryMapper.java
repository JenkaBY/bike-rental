package com.github.jenkaby.bikerental.finance.web.query.mapper;

import com.github.jenkaby.bikerental.finance.application.usecase.FindTransactionsUseCase.TransactionListItemDto;
import com.github.jenkaby.bikerental.finance.domain.model.LedgerType;
import com.github.jenkaby.bikerental.finance.domain.model.TransactionFilter;
import com.github.jenkaby.bikerental.finance.web.query.dto.TransactionFilterParams;
import com.github.jenkaby.bikerental.finance.web.query.dto.TransactionSummaryResponse;
import org.mapstruct.Mapper;

import java.util.Set;
import java.util.UUID;

@Mapper
public interface TransactionQueryMapper {

    TransactionSummaryResponse toResponse(TransactionListItemDto dto);

    TransactionSummaryResponse.TransactionEntryResponse toEntryResponse(TransactionListItemDto.Entry entry);

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
