# Task 004: Create Transaction and TransactionRecord Domain Models

> **Applied Skill:** `java.instructions.md` — pure domain aggregates with no framework imports; Lombok for
> immutability.

## 1. Objective

Create the two new domain classes that form the Transaction aggregate: `TransactionRecord` (value object — a
single debit/credit leg) and `Transaction` (aggregate root — the journal header owning exactly two
`TransactionRecord` children). Both classes depend on the enums created in Task 002.

## 2. Files to Create

| # | File Path | Action |
|---|-----------|--------|
| 1 | `service/src/main/java/com/github/jenkaby/bikerental/finance/domain/model/TransactionRecord.java` | Create New File |
| 2 | `service/src/main/java/com/github/jenkaby/bikerental/finance/domain/model/Transaction.java` | Create New File |

## 3. Code Implementation

### File 1 — `TransactionRecord.java`

**Imports Required:**
```java
import java.util.UUID;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;
```

```java
package com.github.jenkaby.bikerental.finance.domain.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(of = "id")
public class TransactionRecord {

    private final UUID id;
    private final SubLedgerRef subLedgerRef;
    private final LedgerType ledgerType;
    private final EntryDirection direction;
    private final Money amount;
}
```

### File 2 — `Transaction.java`

**Imports Required:**
```java
import com.github.jenkaby.bikerental.finance.PaymentMethod;
import org.jspecify.annotations.Nullable;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
```

```java
package com.github.jenkaby.bikerental.finance.domain.model;

import com.github.jenkaby.bikerental.finance.PaymentMethod;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(of = "id")
public class Transaction {

    private final UUID id;
    private final TransactionType type;
    private final PaymentMethod paymentMethod;
    private final Money amount;
    private final UUID customerId;
    private final String operatorId;
    @Nullable
    private final TransactionSourceType sourceType;
    @Nullable
    private final String sourceId;
    private final Instant recordedAt;
    private final com.github.jenkaby.bikerental.shared.domain.IdempotencyKey idempotencyKey;
    private final List<TransactionRecord> records;
}

Note: `TransactionRecord` should support a `@ToBuilder`-style API and domain code uses a
`TransactionRecordWithoutId` payload returned from `SubLedger.credit(Money)` / `debit(Money)`;
the service assigns UUIDs to create persistent `TransactionRecord` instances before persisting the
aggregate.
```

> **Note:** `records` is named `records` (not `lines` or `entries`) to match the `TransactionRecord` type
> name. The field is set at construction time via `@Builder` and must always contain exactly two elements;
> this invariant is enforced at the service layer (Task 007), not in the domain class itself, to keep the
> domain free of complex constructor logic.

## 4. Validation Steps

```bash
./gradlew :service:compileJava
```

Both new classes must compile without errors.
