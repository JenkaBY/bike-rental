# Task 001: Add `TransactionRef` and `RentalRef` Shared Value Objects

> **Applied Skill:** `java.instructions.md` — Java Records for immutable data; null guard; `static of()` factory
> following the `CustomerRef` / `IdempotencyKey` pattern already in the codebase.

## 1. Objective

Create two new shared domain value-object records in `shared/domain/` so they are visible across module
boundaries. `TransactionRef` wraps a `UUID` transaction identifier; `RentalRef` wraps a `Long` rental identifier.

## 2. File to Modify / Create

### File 1

* **File Path:** `service/src/main/java/com/github/jenkaby/bikerental/shared/domain/TransactionRef.java`
* **Action:** Create New File

### File 2

* **File Path:** `service/src/main/java/com/github/jenkaby/bikerental/shared/domain/RentalRef.java`
* **Action:** Create New File

---

## 3. Code Implementation

### File 1 — `TransactionRef.java`

**Imports Required:** `java.util.UUID`

```java
package com.github.jenkaby.bikerental.shared.domain;

import java.util.UUID;

public record TransactionRef(UUID id) {

    public TransactionRef {
        if (id == null) {
            throw new IllegalArgumentException("Transaction id must not be null");
        }
    }

    public static TransactionRef of(UUID id) {
        return new TransactionRef(id);
    }
}
```

---

### File 2 — `RentalRef.java`

**Imports Required:** none

```java
package com.github.jenkaby.bikerental.shared.domain;

public record RentalRef(Long id) {

    public RentalRef {
        if (id == null) {
            throw new IllegalArgumentException("Rental id must not be null");
        }
    }

    public static RentalRef of(Long id) {
        return new RentalRef(id);
    }
}
```

---

## 4. Validation Steps

```bash
./gradlew :service:compileJava
```
