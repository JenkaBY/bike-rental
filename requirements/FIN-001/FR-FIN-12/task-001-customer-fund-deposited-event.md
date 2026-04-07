# Task 001: Create CustomerFundDeposited Domain Event

> **Applied Skill:** `spring-boot-modulith` — Event records must live in the module's public API package and implement
`BikeRentalEvent`. Follow the pattern of existing `PaymentReceived` in the same package.

## 1. Objective

Create the `CustomerFundDeposited` domain event record in the Finance module's public API package so it can be published
by `RecordDepositService` and consumed by any `@ApplicationModuleListener` in another module.

## 2. File to Modify / Create

* **File Path:** `service/src/main/java/com/github/jenkaby/bikerental/finance/CustomerFundDeposited.java`
* **Action:** Create New File

## 3. Code Implementation

**Imports Required:**

```java
package com.github.jenkaby.bikerental.finance;

import com.github.jenkaby.bikerental.shared.domain.event.BikeRentalEvent;
import java.time.Instant;
import java.util.UUID;
```

**Code to Add/Replace:**

* **Location:** New file — entire content below

```java
package com.github.jenkaby.bikerental.finance;

import com.github.jenkaby.bikerental.shared.domain.event.BikeRentalEvent;

import java.time.Instant;
import java.util.UUID;

public record CustomerFundDeposited(
        UUID customerId,
        UUID transactionId,
        String operatorId,
        Instant depositedAt
) implements BikeRentalEvent {
}
```

## 4. Validation Steps

```bash
./gradlew :service:compileJava
```
