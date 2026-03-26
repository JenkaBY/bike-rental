# Task 009: Create FinanceCustomerEventListener

> **Applied Skill:** `spring-boot-modulith/SKILL.md` — Cross-module event consumer; use
> `@TransactionalEventListener(phase = BEFORE_COMMIT)` instead of `@ApplicationModuleListener` to bind the
> finance-side write to the same database transaction as the customer registration, guaranteeing atomicity
> without a distributed transaction.
> `java.instructions.md` — Constructor injection only; no field injection; package-private class.

## 1. Objective

Create `FinanceCustomerEventListener` — the Spring event listener inside the `finance` module that reacts to the
`CustomerRegistered` event published by the customer module. It must run **within** the same database transaction as
the customer registration (`BEFORE_COMMIT` phase) so that:

- If account creation fails, the entire transaction (including the customer row) is rolled back.
- A customer can never exist without an accompanying finance account.

This listener is the integration seam between the `customer` and `finance` bounded contexts.

**IMPORTANT — `BEFORE_COMMIT` vs `@ApplicationModuleListener`:**
`@ApplicationModuleListener` combines
`@Async + @Transactional(REQUIRES_NEW) + @TransactionalEventListener(AFTER_COMMIT)`.
That is the wrong semantic here: `AFTER_COMMIT` runs after the customer transaction commits, in a separate
transaction, which breaks atomicity. Use `@TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)`
directly, without `@Async` and without `@Transactional(REQUIRES_NEW)`, so the listener shares the caller's
transaction.

## 2. File to Create

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/finance/infrastructure/eventlistener/FinanceCustomerEventListener.java`
* **Action:** Create New File

## 3. Code Implementation

**Imports Required:**

```java
import com.github.jenkaby.bikerental.finance.application.usecase.CreateCustomerAccountUseCase;
import com.github.jenkaby.bikerental.shared.domain.event.CustomerRegistered;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
```

**Code to Add/Replace:**

* **Location:** New file — full class body.

```java
package com.github.jenkaby.bikerental.finance.infrastructure.eventlistener;

import com.github.jenkaby.bikerental.finance.application.usecase.CreateCustomerAccountUseCase;
import com.github.jenkaby.bikerental.shared.domain.event.CustomerRegistered;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@RequiredArgsConstructor
@Component
class FinanceCustomerEventListener {

    private final CreateCustomerAccountUseCase createCustomerAccountUseCase;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void onCustomerRegistered(CustomerRegistered event) {
        log.info("Received CustomerRegistered event for customerId={}", event.customerRef().id());
        createCustomerAccountUseCase.execute(event.customerRef().id());
    }
}
```

**Key points:**

- The class is **package-private** (`class`, not `public class`) — it is not part of the finance module's public API.
- `@TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)` ensures the listener runs in the same
  transaction as `CreateCustomerService.execute()`, before the commit.
- Any exception thrown by `createCustomerAccountUseCase.execute(...)` propagates out of the listener and causes the
  enclosing transaction to roll back — preventing a customer row without a finance account.
- `@Slf4j` + `@RequiredArgsConstructor` from Lombok reduce boilerplate.

## 4. Validation Steps

```bash
./gradlew :service:compileJava
```
