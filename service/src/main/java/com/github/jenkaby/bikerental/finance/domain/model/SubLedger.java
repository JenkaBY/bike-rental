package com.github.jenkaby.bikerental.finance.domain.model;

import com.github.jenkaby.bikerental.finance.domain.exception.InsufficientBalanceException;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;
import lombok.*;

import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(of = "id")
public class SubLedger {

    private final UUID id;
    private final LedgerType ledgerType;
    @Setter(AccessLevel.PRIVATE)
    private Money balance;
    private Long version;

    public TransactionRecordWithoutId credit(Money amount) {
        this.balance = this.balance.add(amount);
        return new TransactionRecordWithoutId(toRef(), this.ledgerType, EntryDirection.CREDIT, amount);
    }

    public boolean isSufficientBalance(Money amount) {
        return !this.balance.isLessThan(amount);
    }

    public TransactionRecordWithoutId debit(Money amount) {
        if (!this.ledgerType.isSystemLedger() && !isSufficientBalance(amount)) {
            throw new InsufficientBalanceException(this.balance, amount);
        }
        this.balance = this.balance.subtract(amount);
        return new TransactionRecordWithoutId(toRef(), this.ledgerType, EntryDirection.DEBIT, amount);
    }

    private SubLedgerRef toRef() {
        return new SubLedgerRef(this.id);
    }
}
