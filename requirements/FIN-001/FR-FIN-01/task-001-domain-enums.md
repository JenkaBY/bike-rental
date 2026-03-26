# Task 001: Domain Enums — AccountType & LedgerType

> **Applied Skill:** `java.instructions.md` — Enums for discriminated values; immutable constants in `UPPER_SNAKE_CASE`.

## 1. Objective

Create two new domain enums inside the `finance` module's domain model package. `AccountType` discriminates between
system-owned and customer-owned accounts. `LedgerType` enumerates all five sub-ledger classifications for the System
Account as specified in FR-FIN-01.

## 2. Files to Create

### File 1

* **File Path:** `service/src/main/java/com/github/jenkaby/bikerental/finance/domain/model/AccountType.java`
* **Action:** Create New File

### File 2

* **File Path:** `service/src/main/java/com/github/jenkaby/bikerental/finance/domain/model/LedgerType.java`
* **Action:** Create New File

## 3. Code Implementation

### AccountType.java

**Imports Required:**

```java
// none
```

**Code to Add/Replace:**

* **Location:** New file — full class body.

```java
package com.github.jenkaby.bikerental.finance.domain.model;

public enum AccountType {
    SYSTEM,
    CUSTOMER
}
```

---

### LedgerType.java

**Imports Required:**

```java
// none
```

**Code to Add/Replace:**

* **Location:** New file — full class body.

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

## 4. Validation Steps

```bash
./gradlew :service:compileJava
```
