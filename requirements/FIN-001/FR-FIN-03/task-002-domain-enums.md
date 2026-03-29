# Task 002: Add Domain Enums — TransactionType, EntryDirection, TransactionSourceType

> **Applied Skill:** `java.instructions.md` — plain Java enums in the domain model package, no framework imports.

## 1. Objective

Create three new domain enums in `finance/domain/model/` that are required by the `Transaction` aggregate:
`TransactionType`, `EntryDirection`, and `TransactionSourceType`. These must exist before `Transaction` and
`TransactionRecord` domain classes can be authored.

## 2. Files to Create

| # | File Path | Action |
|---|-----------|--------|
| 1 | `service/src/main/java/com/github/jenkaby/bikerental/finance/domain/model/TransactionType.java` | Create New File |
| 2 | `service/src/main/java/com/github/jenkaby/bikerental/finance/domain/model/EntryDirection.java` | Create New File |
| 3 | `service/src/main/java/com/github/jenkaby/bikerental/finance/domain/model/TransactionSourceType.java` | Create New File |

## 3. Code Implementation

### File 1 — `TransactionType.java`

**Imports Required:** none

```java
package com.github.jenkaby.bikerental.finance.domain.model;

public enum TransactionType {
    DEPOSIT
}
```

### File 2 — `EntryDirection.java`

**Imports Required:** none

```java
package com.github.jenkaby.bikerental.finance.domain.model;

public enum EntryDirection {
    DEBIT,
    CREDIT
}
```

### File 3 — `TransactionSourceType.java`

**Imports Required:** none

```java
package com.github.jenkaby.bikerental.finance.domain.model;

public enum TransactionSourceType {
    RENTAL
}
```

## 4. Validation Steps

```bash
./gradlew :service:compileJava
```

All three files must compile without errors.
