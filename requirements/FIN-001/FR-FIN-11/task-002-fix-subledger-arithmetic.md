# Task 002: Fix `SubLedger` Balance Arithmetic

> **Applied Skill:** `java.instructions.md` — pattern matching; immutability; no inline comments.
> **Depends on:** `task-001` (`LedgerType.isAssetLedger()` must exist).

## 1. Objective

Make `SubLedger.debit()` and `SubLedger.credit()` account-type-aware. For asset sub-ledgers (`CASH`,
`CARD_TERMINAL`, `BANK_TRANSFER`), `debit()` must add to the balance and `credit()` must subtract from it,
matching standard T-account rules. Liability and income sub-ledgers retain their current arithmetic.

## 2. File to Modify / Create

* **File Path:** `service/src/main/java/com/github/jenkaby/bikerental/finance/domain/model/SubLedger.java`
* **Action:** Modify Existing File

---

## 3. Code Implementation

**Imports Required:** None — all referenced types are already in scope.

**Replace the entire `credit()` method:**

* **Location:** Replace the existing `credit(Money amount)` method body (lines 21–23 in the current file).

* **Old code:**

```java
    public TransactionRecordWithoutId credit(Money amount) {
        this.balance = this.balance.add(amount);
        return new TransactionRecordWithoutId(toRef(), this.ledgerType, EntryDirection.CREDIT, amount);
    }
```

* **New code:**

```java
    public TransactionRecordWithoutId credit(Money amount) {
        this.balance = this.ledgerType.isAssetLedger()
                ? this.balance.subtract(amount)
                : this.balance.add(amount);
        return new TransactionRecordWithoutId(toRef(), this.ledgerType, EntryDirection.CREDIT, amount);
    }
```

**Replace the entire `debit()` method:**

* **Location:** Replace the existing `debit(Money amount)` method body (lines 30–35 in the current file).

* **Old code:**

```java
    public TransactionRecordWithoutId debit(Money amount) {
        if (!this.ledgerType.isSystemLedger() && !isSufficientBalance(amount)) {
            throw new InsufficientBalanceException(this.balance, amount);
        }
        this.balance = this.balance.subtract(amount);
        return new TransactionRecordWithoutId(toRef(), this.ledgerType, EntryDirection.DEBIT, amount);
    }
```

* **New code:**

```java
    public TransactionRecordWithoutId debit(Money amount) {
        if (!this.ledgerType.isSystemLedger() && !isSufficientBalance(amount)) {
            throw new InsufficientBalanceException(this.balance, amount);
        }
        this.balance = this.ledgerType.isAssetLedger()
                ? this.balance.add(amount)
                : this.balance.subtract(amount);
        return new TransactionRecordWithoutId(toRef(), this.ledgerType, EntryDirection.DEBIT, amount);
    }
```

**Resulting full class after changes:**

```java
package com.github.jenkaby.bikerental.finance.domain.model;

import com.github.jenkaby.bikerental.shared.exception.InsufficientBalanceException;
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
        this.balance = this.ledgerType.isAssetLedger()
                ? this.balance.subtract(amount)
                : this.balance.add(amount);
        return new TransactionRecordWithoutId(toRef(), this.ledgerType, EntryDirection.CREDIT, amount);
    }

    public boolean isSufficientBalance(Money amount) {
        return !this.balance.isLessThan(amount);
    }

    public TransactionRecordWithoutId debit(Money amount) {
        if (!this.ledgerType.isSystemLedger() && !isSufficientBalance(amount)) {
            throw new InsufficientBalanceException(this.balance, amount);
        }
        this.balance = this.ledgerType.isAssetLedger()
                ? this.balance.add(amount)
                : this.balance.subtract(amount);
        return new TransactionRecordWithoutId(toRef(), this.ledgerType, EntryDirection.DEBIT, amount);
    }

    private SubLedgerRef toRef() {
        return new SubLedgerRef(this.id);
    }
}
```

## 4. Validation Steps

```bash
./gradlew :service:compileJava
```
