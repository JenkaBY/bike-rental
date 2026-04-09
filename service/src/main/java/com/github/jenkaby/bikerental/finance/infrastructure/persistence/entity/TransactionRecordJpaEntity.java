package com.github.jenkaby.bikerental.finance.infrastructure.persistence.entity;

import com.github.jenkaby.bikerental.finance.domain.model.EntryDirection;
import com.github.jenkaby.bikerental.finance.domain.model.LedgerType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "finance_transaction_records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "transaction")
public class TransactionRecordJpaEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id", nullable = false)
    private TransactionJpaEntity transaction;

    @Column(name = "sub_ledger_id", nullable = false)
    private UUID subLedgerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "ledger_type", nullable = false, length = 30)
    private LedgerType ledgerType;

    @Enumerated(EnumType.STRING)
    @Column(name = "direction", nullable = false, length = 10)
    private EntryDirection direction;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;
}
