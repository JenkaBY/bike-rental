package com.github.jenkaby.bikerental.finance.domain.model;

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
    private final long version;

    public TransactionRecordWithoutId credit(Money amount) {
        this.balance = this.balance.add(amount);
        return new TransactionRecordWithoutId(toRef(), this.ledgerType, EntryDirection.CREDIT, amount);
    }

    public TransactionRecordWithoutId debit(Money amount) {
        this.balance = this.balance.subtract(amount);
        return new TransactionRecordWithoutId(toRef(), this.ledgerType, EntryDirection.DEBIT, amount);
    }

    private SubLedgerRef toRef() {
        return new SubLedgerRef(this.id);
    }
}
