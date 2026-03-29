# Task 003: Extend Account and SubLedger Domain Models

> **Applied Skill:** `java.instructions.md` — extend existing domain classes with behaviour methods; keep domain
> pure (no framework imports).

## 1. Objective

Add two balance-mutation methods to `SubLedger` (`credit` and `debit`) and a sub-ledger lookup convenience method
to `Account` (`getSubLedger`). These are the only domain-layer changes required before the application service
can orchestrate a deposit.

## 2. Files to Modify

| # | File Path | Action |
|---|-----------|--------|
| 1 | `service/src/main/java/com/github/jenkaby/bikerental/finance/domain/model/SubLedger.java` | Modify Existing File |
| 2 | `service/src/main/java/com/github/jenkaby/bikerental/finance/domain/model/Account.java` | Modify Existing File |

## 3. Code Implementation

### Step 1 — Add mutation methods to `SubLedger`

**File:** `service/src/main/java/com/github/jenkaby/bikerental/finance/domain/model/SubLedger.java`

**Current file:**
```java
package com.github.jenkaby.bikerental.finance.domain.model;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(of = "id")
public class SubLedger {

    private final UUID id;
    private final LedgerType ledgerType;
    private final BigDecimal balance;
}
```

**Replace `private final BigDecimal balance;` and closing brace with:**

```java
package com.github.jenkaby.bikerental.finance.domain.model;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(of = "id")
public class SubLedger {

    private final UUID id;
    private final LedgerType ledgerType;
    @Setter(AccessLevel.PRIVATE)
    private BigDecimal balance;

    public void credit(BigDecimal amount) {
        this.balance = this.balance.add(amount);
    }

    public void debit(BigDecimal amount) {
        this.balance = this.balance.add(amount);
    }
}
```

> **Explanation:** Both `credit` and `debit` *add* to the balance. In double-entry bookkeeping the sign
> convention is determined by the account category and direction — the service layer controls direction via
> `EntryDirection`; `SubLedger` itself tracks a plain running total. The `@Setter(AccessLevel.PRIVATE)` on
> `balance` prevents direct field mutation from outside the class while still allowing Lombok `@Builder` to
> set the initial value.

### Step 2 — Add `getSubLedger` to `Account`

**File:** `service/src/main/java/com/github/jenkaby/bikerental/finance/domain/model/Account.java`

**Imports Required:**
```java
import com.github.jenkaby.bikerental.shared.exception.ResourceNotFoundException;
```

**Location:** Add the method at the end of the class body, before the closing brace.

```java
    public SubLedger getSubLedger(LedgerType type) {
        return subLedgers.stream()
                .filter(sl -> sl.getLedgerType() == type)
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(SubLedger.class, type.name()));
    }
```

## 4. Validation Steps

```bash
./gradlew :service:compileJava
```

Compilation must succeed. The `balance` field change from `final` to mutable (with private setter) must not
break `AccountJpaMapper` (which uses `@Builder`) — verify by checking that MapStruct-generated code still
compiles via the full compile goal.
