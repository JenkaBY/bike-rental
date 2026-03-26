# Task 001: Extend LedgerType Enum — CUSTOMER_WALLET & CUSTOMER_HOLD

> **Applied Skill:** `java.instructions.md` — Enum constants in `UPPER_SNAKE_CASE`; no framework imports in domain
> enums.

## 1. Objective

Add two new `LedgerType` constants that represent customer-owned sub-ledger categories. `CUSTOMER_WALLET` is the
customer's available spendable balance (Liability). `CUSTOMER_HOLD` is the reserved, pre-authorized funds locked
during an active rental (Liability). Both were absent from the initial FR-FIN-01 implementation which only covered
system-account sub-ledgers.

## 2. File to Modify

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/finance/domain/model/LedgerType.java`
* **Action:** Modify Existing File

## 3. Code Implementation

**Imports Required:**

```java
// none — enum only
```

**Code to Add/Replace:**

* **Location:** Inside the `LedgerType` enum body, after the existing `ADJUSTMENT` constant.

**Current file content (for reference):**

```java
package com.github.jenkaby.bikerental.finance.domain.model;

public enum LedgerType {
    CASH,
    CARD_TERMINAL,
    BANK_TRANSFER,
    REVENUE,
    ADJUSTMENT
}
```

**Replace the entire file with:**

```java
package com.github.jenkaby.bikerental.finance.domain.model;

public enum LedgerType {
    CASH,
    CARD_TERMINAL,
    BANK_TRANSFER,
    REVENUE,
    ADJUSTMENT,
    CUSTOMER_WALLET,
    CUSTOMER_HOLD
}
```

## 4. Validation Steps

```bash
./gradlew :service:compileJava
```
