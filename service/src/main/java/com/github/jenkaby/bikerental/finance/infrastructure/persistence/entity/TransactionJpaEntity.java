package com.github.jenkaby.bikerental.finance.infrastructure.persistence.entity;

import com.github.jenkaby.bikerental.finance.PaymentMethod;
import com.github.jenkaby.bikerental.finance.domain.model.TransactionSourceType;
import com.github.jenkaby.bikerental.finance.domain.model.TransactionType;
import jakarta.persistence.*;
import lombok.*;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "finance_transactions",
        uniqueConstraints = @UniqueConstraint(name = "uq_finance_transactions_idempotency_key_customer_id",
                columnNames = {"idempotency_key", "customer_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class TransactionJpaEntity {

    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 30)
    private TransactionType transactionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 30)
    private PaymentMethod paymentMethod;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "operator_id", nullable = false)
    private String operatorId;

    @Nullable
    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", length = 30)
    private TransactionSourceType sourceType;

    @Nullable
    @Column(name = "source_id", length = 255)
    private String sourceId;

    @Column(name = "recorded_at", nullable = false)
    private Instant recordedAt;

    @Column(name = "idempotency_key", nullable = false)
    private UUID idempotencyKey;

    @OneToMany(mappedBy = "transaction", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TransactionRecordJpaEntity> records = new ArrayList<>();
}
