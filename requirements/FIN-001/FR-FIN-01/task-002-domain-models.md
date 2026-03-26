# Task 002: Domain Models — SubLedger & Account

> **Applied Skill:** `java.instructions.md` — Immutable value-carrying objects with Lombok builders; no framework
> imports in domain layer.  
> **Applied Skill:** `spring-boot-data-ddd/SKILL.md` — Pure domain aggregates with no JPA or Spring annotations.

## 1. Objective

Create two immutable domain model classes in the `finance` module: `SubLedger` (a value object carrying
ledger-type and balance) and `Account` (an aggregate root carrying account-type, optional customer reference, and
a list of owned sub-ledgers). These are the domain representations used by all application-layer services.

## 2. Files to Create

### File 1

* **File Path:** `service/src/main/java/com/github/jenkaby/bikerental/finance/domain/model/SubLedger.java`
* **Action:** Create New File

### File 2

* **File Path:** `service/src/main/java/com/github/jenkaby/bikerental/finance/domain/model/Account.java`
* **Action:** Create New File

## 3. Code Implementation

### SubLedger.java

**Imports Required (SubLedger):**

```java
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;
```

**Code to Add/Replace:**

* **Location:** New file — full class body.

```java
package com.github.jenkaby.bikerental.finance.domain.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
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

---

### Account.java

**Imports Required (Account):**

```java
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.List;

import com.github.jenkaby.bikerental.shared.domain.CustomerRef;

import java.util.UUID; // id field stays UUID
```

**Code to Add/Replace:**

* **Location:** New file — full class body.

```java
package com.github.jenkaby.bikerental.finance.domain.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(of = "id")
public class Account {

    private final UUID id;
    private final AccountType accountType;
    private final CustomerRef customerRef; // typed wrapper for customer identity
    private final List<SubLedger> subLedgers;
}
```

## 4. Validation Steps

1. Compile the `service` module so MapStruct generates mapper implementations:

```bash
./gradlew :service:compileJava "-Dspring.profiles.active=test" --stacktrace
```

2. Run unit tests that cover `finance` mappers if present.

```bash
```

Notes:

- Audit timestamps (`createdAt`/`updatedAt`) were intentionally removed from domain classes. The persistence layer
  retains audit columns and is configured to populate them via JPA auditing. Update mappers to ignore audit fields when
  mapping domain->entity.
- `Account` now uses `CustomerRef` — update any tests and DTO mappers that previously used raw `UUID customerId.`
