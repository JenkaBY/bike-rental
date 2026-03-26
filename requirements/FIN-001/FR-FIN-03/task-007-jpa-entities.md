# Task 007: Create JPA Entities — TransactionJpaEntity and TransactionRecordJpaEntity

> **Applied Skill:** `spring-boot-data-ddd` — JPA entity conventions; `@EntityListeners(AuditingEntityListener)`;
> Lombok builder pattern; JPA `AttributeConverter` for value-object columns; follow exact same style as
> `AccountJpaEntity` / `SubLedgerJpaEntity`.

## 1. Objective

Create `SubLedgerRef` (finance-scoped domain value object), its JPA `AttributeConverter`, and the two JPA
entities that map to the tables introduced in Task 006. `TransactionJpaEntity` owns a one-to-many
cascade-all relationship to `TransactionRecordJpaEntity`. `TransactionRecordJpaEntity` stores the
sub-ledger reference as the typed `SubLedgerRef` value object rather than a raw UUID.

## 2. Files to Create

| # | File Path | Action |
|---|-----------|--------|
| 1 | `service/src/main/java/com/github/jenkaby/bikerental/finance/domain/model/SubLedgerRef.java` | Create New File |
| 2 | `service/src/main/java/com/github/jenkaby/bikerental/finance/infrastructure/persistence/converter/SubLedgerRefConverter.java` | Create New File |
| 3 | `service/src/main/java/com/github/jenkaby/bikerental/finance/infrastructure/persistence/entity/TransactionJpaEntity.java` | Create New File |
| 4 | `service/src/main/java/com/github/jenkaby/bikerental/finance/infrastructure/persistence/entity/TransactionRecordJpaEntity.java` | Create New File |

## 3. Code Implementation

### File 1 — `SubLedgerRef.java`

```java
package com.github.jenkaby.bikerental.finance.domain.model;

import java.util.UUID;

public record SubLedgerRef(UUID id) {

    public SubLedgerRef {
        if (id == null) {
            throw new IllegalArgumentException("SubLedger id must not be null");
        }
    }
}
```

### File 2 — `SubLedgerRefConverter.java`

```java
package com.github.jenkaby.bikerental.finance.infrastructure.persistence.converter;

import com.github.jenkaby.bikerental.finance.domain.model.SubLedgerRef;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

@Converter
public class SubLedgerRefConverter implements AttributeConverter<SubLedgerRef, UUID> {

    @Override
    public @Nullable UUID convertToDatabaseColumn(@Nullable SubLedgerRef ref) {
        return ref == null ? null : ref.id();
    }

    @Override
    public @Nullable SubLedgerRef convertToEntityAttribute(@Nullable UUID uuid) {
        return new SubLedgerRef(uuid);
    }
}
```

### File 3 — `TransactionJpaEntity.java`

```java
package com.github.jenkaby.bikerental.finance.infrastructure.persistence.entity;

import com.github.jenkaby.bikerental.finance.domain.model.TransactionSourceType;
import com.github.jenkaby.bikerental.finance.domain.model.TransactionType;
import com.github.jenkaby.bikerental.finance.PaymentMethod;
import jakarta.persistence.*;
import lombok.*;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "finance_transactions")
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

    @Column(name = "idempotency_key", nullable = false, unique = true)
    private UUID idempotencyKey;

    @OneToMany(mappedBy = "transaction", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TransactionRecordJpaEntity> records = new ArrayList<>();
}
```

### File 4 — `TransactionRecordJpaEntity.java`

```java
package com.github.jenkaby.bikerental.finance.infrastructure.persistence.entity;

import com.github.jenkaby.bikerental.finance.domain.model.EntryDirection;
import com.github.jenkaby.bikerental.finance.domain.model.LedgerType;
import com.github.jenkaby.bikerental.finance.domain.model.SubLedgerRef;
import com.github.jenkaby.bikerental.finance.infrastructure.persistence.converter.SubLedgerRefConverter;
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

    @Convert(converter = SubLedgerRefConverter.class)
    @Column(name = "sub_ledger_id", nullable = false)
    private SubLedgerRef subLedgerRef;

    @Enumerated(EnumType.STRING)
    @Column(name = "ledger_type", nullable = false, length = 30)
    private LedgerType ledgerType;

    @Enumerated(EnumType.STRING)
    @Column(name = "direction", nullable = false, length = 10)
    private EntryDirection direction;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;
}
```

## 4. Validation Steps

```bash
./gradlew :service:compileJava
```

All four files must compile without errors.


