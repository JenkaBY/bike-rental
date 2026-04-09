# Task 003: Add GetTransactionHistoryUseCase Interface

> **Applied Skill:** `spring-boot-data-ddd` — Application Use-Case Interface (contract definition)

## 1. Objective

Create the `GetTransactionHistoryUseCase` interface in the `finance` application use-case layer. It defines the
contract accepted by the service and consumed by the controller: a customer ID, optional filter parameters, and
pagination — returning a `Page<TransactionEntryDto>` from the shared value object.

## 2. File to Modify / Create

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/finance/application/usecase/GetTransactionHistoryUseCase.java`
* **Action:** Create New File

## 3. Code Implementation

**Imports Required:**

```java
import com.github.jenkaby.bikerental.finance.domain.model.EntryDirection;
import com.github.jenkaby.bikerental.finance.domain.model.LedgerType;
import com.github.jenkaby.bikerental.finance.domain.model.TransactionHistoryFilter;
import com.github.jenkaby.bikerental.finance.domain.model.TransactionSourceType;
import com.github.jenkaby.bikerental.finance.domain.model.TransactionType;
import com.github.jenkaby.bikerental.finance.PaymentMethod;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Page;
import com.github.jenkaby.bikerental.shared.domain.model.vo.PageRequest;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
```

**Snippet:**

```java
package com.github.jenkaby.bikerental.finance.application.usecase;

import com.github.jenkaby.bikerental.finance.PaymentMethod;
import com.github.jenkaby.bikerental.finance.domain.model.EntryDirection;
import com.github.jenkaby.bikerental.finance.domain.model.LedgerType;
import com.github.jenkaby.bikerental.finance.domain.model.TransactionHistoryFilter;
import com.github.jenkaby.bikerental.finance.domain.model.TransactionSourceType;
import com.github.jenkaby.bikerental.finance.domain.model.TransactionType;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Page;
import com.github.jenkaby.bikerental.shared.domain.model.vo.PageRequest;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public interface GetTransactionHistoryUseCase {

    Page<TransactionEntryDto> execute(UUID customerId, TransactionHistoryFilter filter, PageRequest pageRequest);

    record TransactionEntryDto(
          LedgerType subLedger,
          UUID customerId,
            BigDecimal amount,
            EntryDirection direction,
            TransactionType type,
            Instant recordedAt,
            @Nullable PaymentMethod paymentMethod,
            @Nullable String reason,
          @Nullable TransactionSourceType sourceType,
          @Nullable String sourceId
    ) {
    }
}
```

> **Note:** `TransactionHistoryFilter` is a domain record created in Task 004 at
> `finance/domain/model/TransactionHistoryFilter.java`.

## 4. Validation Steps

skip validation
