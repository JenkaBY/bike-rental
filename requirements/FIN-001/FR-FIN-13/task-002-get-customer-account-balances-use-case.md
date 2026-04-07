# Task 002: Create `GetCustomerAccountBalancesUseCase`

> **Applied Skill:** `spring-boot-data-ddd` — use-case interface pattern; application-layer port declaring a
> read-only query following the established `*UseCase` naming convention.

## 1. Objective

Declare the use-case interface and its nested result record for retrieving customer account balances. This is the
contract between the controller (Task 005) and the service (Task 003). Using `BigDecimal` and `Instant` keeps the
result record free of domain-layer dependencies so it can be used directly as a web DTO projection.

## 2. File to Create

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/finance/application/usecase/GetCustomerAccountBalancesUseCase.java`
* **Action:** Create New File

## 3. Code Implementation

**Imports Required:**

```java
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
```

**Full file content:**

```java
package com.github.jenkaby.bikerental.finance.application.usecase;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public interface GetCustomerAccountBalancesUseCase {

    CustomerAccountBalances execute(UUID customerId);

    record CustomerAccountBalances(
            BigDecimal walletBalance,
            BigDecimal holdBalance,
            Instant lastUpdatedAt
    ) {
    }
}
```

## 4. Validation Steps

```bash
./gradlew :service:compileJava
```

Expected: compiles without errors; the interface is visible to other classes in the `finance` module.
