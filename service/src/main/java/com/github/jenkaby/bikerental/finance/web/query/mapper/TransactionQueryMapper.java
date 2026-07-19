package com.github.jenkaby.bikerental.finance.web.query.mapper;

import com.github.jenkaby.bikerental.finance.domain.model.LedgerType;
import com.github.jenkaby.bikerental.finance.domain.model.Transaction;
import com.github.jenkaby.bikerental.finance.domain.model.TransactionDetails;
import com.github.jenkaby.bikerental.finance.domain.model.TransactionFilter;
import com.github.jenkaby.bikerental.finance.domain.model.TransactionRecord;
import com.github.jenkaby.bikerental.finance.web.query.dto.CustomerTransactionResponse;
import com.github.jenkaby.bikerental.finance.web.query.dto.TransactionBalancesResponse;
import com.github.jenkaby.bikerental.finance.web.query.dto.TransactionDeltasResponse;
import com.github.jenkaby.bikerental.finance.web.query.dto.TransactionDetailsResponse;
import com.github.jenkaby.bikerental.finance.web.query.dto.TransactionFilterParams;
import com.github.jenkaby.bikerental.finance.web.query.dto.TransactionSummaryResponse;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;
import com.github.jenkaby.bikerental.shared.mapper.MoneyMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Set;
import java.util.UUID;

@Mapper(uses = {MoneyMapper.class})
public interface TransactionQueryMapper {

    String CREDIT = "CREDIT";
    String DEBIT = "DEBIT";

    @Mapping(target = "entries", source = "records")
    TransactionSummaryResponse toResponse(Transaction transaction);

    TransactionSummaryResponse.TransactionEntryResponse toEntryResponse(TransactionRecord record);

    @Mapping(target = "id", source = "transaction.id")
    @Mapping(target = "customerId", source = "transaction.customerId")
    @Mapping(target = "amount", source = "transaction.amount")
    @Mapping(target = "type", source = "transaction.type")
    @Mapping(target = "recordedAt", source = "transaction.recordedAt")
    @Mapping(target = "paymentMethod", source = "transaction.paymentMethod")
    @Mapping(target = "reason", source = "transaction.reason")
    @Mapping(target = "sourceType", source = "transaction.sourceType")
    @Mapping(target = "sourceId", source = "transaction.sourceId")
    @Mapping(target = "operatorId", source = "transaction.operatorId")
    @Mapping(target = "entries", source = "transaction.records")
    @Mapping(target = "deltas", expression = "java(deltas(details.transaction()))")
    @Mapping(target = "balances", expression = "java(balances(details))")
    TransactionDetailsResponse toDetailsResponse(TransactionDetails details);

    @Mapping(target = "customerId", source = "transaction.customerId")
    @Mapping(target = "amount", source = "transaction.amount")
    @Mapping(target = "type", source = "transaction.type")
    @Mapping(target = "recordedAt", source = "transaction.recordedAt")
    @Mapping(target = "paymentMethod", source = "transaction.paymentMethod")
    @Mapping(target = "reason", source = "transaction.reason")
    @Mapping(target = "sourceType", source = "transaction.sourceType")
    @Mapping(target = "sourceId", source = "transaction.sourceId")
    @Mapping(target = "direction", expression = "java(direction(details.transaction()))")
    @Mapping(target = "deltas", expression = "java(deltas(details.transaction()))")
    @Mapping(target = "balances", expression = "java(balances(details))")
    CustomerTransactionResponse toCustomerTransactionResponse(TransactionDetails details);

    @Mapping(target = "balanceAfter", source = "entry.runningBalance")
    @Mapping(target = "signedDelta", expression = "java(entry.signedBalanceDelta().amount())")
    @Mapping(target = "systemLedger", expression = "java(entry.getLedgerType().isSystemLedger())")
    TransactionDetailsResponse.TransactionDetailEntryResponse toDetailEntryResponse(TransactionRecord entry);

    default TransactionDeltasResponse deltas(Transaction transaction) {
        Money wallet = transaction.walletBalanceDelta();
        Money hold = transaction.holdBalanceDelta();
        return new TransactionDeltasResponse(wallet.amount(), hold.amount(), wallet.add(hold).amount());
    }

    default TransactionBalancesResponse balances(TransactionDetails details) {
        return new TransactionBalancesResponse(
                details.walletBalanceAfter().amount(), details.holdBalanceAfter().amount());
    }

    default String direction(Transaction transaction) {
        Money walletDelta = transaction.walletBalanceDelta();
        Money decisive = walletDelta.isZero() ? walletDelta.add(transaction.holdBalanceDelta()) : walletDelta;
        return decisive.isNegative() ? DEBIT : CREDIT;
    }

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
