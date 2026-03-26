# Task 002: Create CustomerRegistered Domain Event

> **Applied Skill:** `spring-boot-modulith/SKILL.md` — Events are immutable records implementing the marker interface;
> placed in the canonical `shared.domain.event` package so all modules can reference them without a cyclic dependency.
> `java.instructions.md` — Records for immutable value types; no framework imports in domain events.

## 1. Objective

Create the `CustomerRegistered` domain event record. It is published by `CreateCustomerService` (customer module)
immediately after a new customer is persisted, and consumed by `FinanceCustomerEventListener` (finance module) to
trigger customer finance account creation. Placing the event in `shared.domain.event` follows the exact pattern
established by `RentalCreated`, `RentalStarted`, `RentalCompleted`, and `RentalUpdated` — all of which live in the
same package and are accessible to all modules without introducing a module-dependency cycle.

## 2. File to Create

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/shared/domain/event/CustomerRegistered.java`
* **Action:** Create New File

## 3. Code Implementation

**Imports Required:**

```java
import com.github.jenkaby.bikerental.shared.domain.CustomerRef;
```

**Code to Add/Replace:**

* **Location:** New file — full class body.

```java
package com.github.jenkaby.bikerental.shared.domain.event;

import com.github.jenkaby.bikerental.shared.domain.CustomerRef;

public record CustomerRegistered(CustomerRef customerRef) implements BikeRentalEvent {
}
```

**Key points:**

- `customerRef` wraps the UUID identity of the newly registered customer as a typed value object, consistent with
  how `Account.customerRef` and `AccountJpaMapper` already model customer identity in the finance module.
- No `Instant occurredAt` field is required — the event is consumed synchronously within the same transaction and
  temporal ordering is implicit.
- The record is intentionally minimal: the consumer only needs the `customerRef` to create the finance account.

## 4. Validation Steps

```bash
./gradlew :service:compileJava
```
