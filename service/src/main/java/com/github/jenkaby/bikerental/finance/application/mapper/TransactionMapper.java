package com.github.jenkaby.bikerental.finance.application.mapper;

import com.github.jenkaby.bikerental.finance.application.usecase.GetTransactionHistoryUseCase.TransactionDto;
import com.github.jenkaby.bikerental.finance.domain.model.Transaction;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;
import com.github.jenkaby.bikerental.shared.mapper.MoneyMapper;
import org.jspecify.annotations.Nullable;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(uses = {MoneyMapper.class})
public interface TransactionMapper {

    String CREDIT = "CREDIT";
    String DEBIT = "DEBIT";

    @Mapping(target = "amount", source = "tx.amount")
    @Mapping(target = "direction", expression = "java(direction(tx))")
    @Mapping(target = "deltas", expression = "java(deltas(tx))")
    @Mapping(target = "balances", expression = "java(balances(walletBalanceAfter, holdBalanceAfter))")
    TransactionDto toEntry(Transaction tx, @Nullable Money walletBalanceAfter, @Nullable Money holdBalanceAfter);

    default String direction(Transaction tx) {
        Money walletDelta = tx.walletBalanceDelta();
        Money decisive = walletDelta.isZero() ? walletDelta.add(tx.holdBalanceDelta()) : walletDelta;
        return decisive.isNegative() ? DEBIT : CREDIT;
    }

    default TransactionDto.Deltas deltas(Transaction tx) {
        Money wallet = tx.walletBalanceDelta();
        Money hold = tx.holdBalanceDelta();
        return new TransactionDto.Deltas(wallet.amount(), hold.amount(), wallet.add(hold).amount());
    }

    default TransactionDto.Balances balances(@Nullable Money walletBalanceAfter, @Nullable Money holdBalanceAfter) {
        return new TransactionDto.Balances(
                walletBalanceAfter != null ? walletBalanceAfter.amount() : null,
                holdBalanceAfter != null ? holdBalanceAfter.amount() : null);
    }
}
