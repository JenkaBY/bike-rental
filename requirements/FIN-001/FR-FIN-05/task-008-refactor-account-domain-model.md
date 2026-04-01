# Task 008: Refactor Account Domain Model into Abstract Class + CustomerAccount + SystemAccount

> **Applied Skill:** `java.instructions.md` — abstract class with `@SuperBuilder`; immutable domain objects;
> expressive naming via typed sub-classes instead of generic `getSubLedger(LedgerType)` calls at call sites.

## 1. Objective

Replace the single concrete `Account` class with an abstract base class plus two typed subclasses:

- `Account` (abstract) — holds `id` and `subLedgers`; declares abstract `getAccountType()`; exposes
  `getSubLedger(LedgerType)` for subclass convenience methods.
- `CustomerAccount` — adds `customerRef`, typed accessors `getWallet()` / `getOnHold()`, and the available-balance guard
  `isBalanceSufficient(Money)`.
- `SystemAccount` — adds typed accessors `getCash()`, `getCardTerminal()`, `getBankTransfer()`, `getRevenue()`,
  `getAdjustment()`.

No persistence schema changes are required — `AccountJpaEntity` is unchanged.

## 2. File to Modify / Create

* **File Path (1):** `service/src/main/java/com/github/jenkaby/bikerental/finance/domain/model/Account.java`
* **Action:** Modify Existing File — replace entire class body

* **File Path (2):** `service/src/main/java/com/github/jenkaby/bikerental/finance/domain/model/CustomerAccount.java`
* **Action:** Create New File

* **File Path (3):** `service/src/main/java/com/github/jenkaby/bikerental/finance/domain/model/SystemAccount.java`
* **Action:** Create New File

## 3. Code Implementation

### File 1 — `Account.java` (replace entirely)

**Code to Add/Replace:**

* **Location:** Replace the entire contents of the existing `Account.java`.

```java
package com.github.jenkaby.bikerental.finance.domain.model;

import com.github.jenkaby.bikerental.shared.exception.ResourceNotFoundException;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.UUID;

@Getter
@SuperBuilder
@EqualsAndHashCode(of = "id")
public abstract class Account {

    private final UUID id;
    private final List<SubLedger> subLedgers;

    public abstract AccountType getAccountType();

    public SubLedger getSubLedger(LedgerType type) {
        return subLedgers.stream()
                .filter(sl -> sl.getLedgerType() == type)
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(SubLedger.class, type.name()));
    }
}
```

> **Lombok note:** `@SuperBuilder` replaces both `@Builder` and `@AllArgsConstructor(access = AccessLevel.PRIVATE)`.
> Subclasses annotated with `@SuperBuilder` can access parent builder fields. MapStruct 1.6 has first-class
> `@SuperBuilder` support and will use the generated `builder()` factory method on each concrete subclass.

---

### File 2 — `CustomerAccount.java` (create new)

```java
package com.github.jenkaby.bikerental.finance.domain.model;

import com.github.jenkaby.bikerental.shared.domain.CustomerRef;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class CustomerAccount extends Account {

    private final CustomerRef customerRef;

    @Override
    public AccountType getAccountType() {
        return AccountType.CUSTOMER;
    }

    public SubLedger getWallet() {
        return getSubLedger(LedgerType.CUSTOMER_WALLET);
    }

    public SubLedger getOnHold() {
        return getSubLedger(LedgerType.CUSTOMER_HOLD);
    }

    public boolean isBalanceSufficient(Money amount) {
        var available = getWallet().getBalance().subtract(getOnHold().getBalance());
        return !available.isLessThan(amount);
    }
}
```

---

### File 3 — `SystemAccount.java` (create new)

```java
package com.github.jenkaby.bikerental.finance.domain.model;

import lombok.experimental.SuperBuilder;

@SuperBuilder
public class SystemAccount extends Account {

    @Override
    public AccountType getAccountType() {
        return AccountType.SYSTEM;
    }

    public SubLedger getCash() {
        return getSubLedger(LedgerType.CASH);
    }

    public SubLedger getCardTerminal() {
        return getSubLedger(LedgerType.CARD_TERMINAL);
    }

    public SubLedger getBankTransfer() {
        return getSubLedger(LedgerType.BANK_TRANSFER);
    }

    public SubLedger getRevenue() {
        return getSubLedger(LedgerType.REVENUE);
    }

    public SubLedger getAdjustment() {
        return getSubLedger(LedgerType.ADJUSTMENT);
    }
}
```

## 4. Validation Steps

```bash
./gradlew :service:compileJava
```
