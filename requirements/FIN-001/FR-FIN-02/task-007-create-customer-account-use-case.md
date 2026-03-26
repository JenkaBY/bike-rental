# Task 007: Create CreateCustomerAccountUseCase Interface

> **Applied Skill:** `java.instructions.md` — Interfaces as use-case ports; single-responsibility; placed in
> `application/usecase` following the existing `RecordPaymentUseCase` pattern.

## 1. Objective

Create the `CreateCustomerAccountUseCase` interface in the `finance` module's application layer. It declares a single
`execute(UUID customerId)` method that `FinanceCustomerEventListener` (Task 009) will invoke. The interface acts as
the inversion-of-control boundary between the event listener and the service implementation.

## 2. File to Create

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/finance/application/usecase/CreateCustomerAccountUseCase.java`
* **Action:** Create New File

## 3. Code Implementation

**Imports Required:**

```java
import java.util.UUID;
```

**Code to Add/Replace:**

* **Location:** New file — full class body.

```java
package com.github.jenkaby.bikerental.finance.application.usecase;

import java.util.UUID;

public interface CreateCustomerAccountUseCase {

    void execute(UUID customerId);
}
```

## 4. Validation Steps

```bash
./gradlew :service:compileJava
```
