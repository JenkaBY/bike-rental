# Task 006: Implement GetTransactionHistoryService

> **Applied Skill:** `spring-boot-data-ddd` — Transactional Application Service; `mapstruct-hexagonal` — Application
> mapper

## 1. Objective

Create `TransactionEntryMapper` (application-layer MapStruct mapper) and `GetTransactionHistoryService` which
implements `GetTransactionHistoryUseCase`. The service verifies the customer finance account exists, delegates the
paginated query to `TransactionRepository`, then uses `TransactionEntryMapper` to convert each
`Transaction + TransactionRecord` pair into a `TransactionEntryDto`. All Specification and Pageable conversion remains
the adapter's responsibility.

## 2. File to Modify / Create

### 2a.

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/finance/application/mapper/TransactionEntryMapper.java`
* **Action:** Create New File

### 2b.

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/finance/application/service/GetTransactionHistoryService.java`
* **Action:** Create New File

## 3. Code Implementation

### 2a — `TransactionEntryMapper.java`

Multi-source MapStruct mapper. When a method has two source parameters MapStruct requires every `@Mapping` to
qualify its source with the parameter name prefix (`tx.*` or `record.*`). The path `record.amount.amount` traverses
the `Money` value object to reach the `BigDecimal` field — MapStruct handles this automatically.

```java
package com.github.jenkaby.bikerental.finance.application.mapper;

import com.github.jenkaby.bikerental.finance.application.usecase.GetTransactionHistoryUseCase.TransactionEntryDto;
import com.github.jenkaby.bikerental.finance.domain.model.Transaction;
import com.github.jenkaby.bikerental.finance.domain.model.TransactionRecord;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface TransactionEntryMapper {

    @Mapping(source = "record.ledgerType", target = "subLedger")
    @Mapping(source = "record.amount.amount", target = "amount")
    @Mapping(source = "record.direction", target = "direction")
    @Mapping(source = "tx.type", target = "type")
    @Mapping(source = "tx.recordedAt", target = "recordedAt")
    @Mapping(source = "tx.paymentMethod", target = "paymentMethod")
    @Mapping(source = "tx.reason", target = "reason")
    @Mapping(source = "tx.customerId", target = "customerId")
    @Mapping(source = "tx.sourceType", target = "sourceType")
    @Mapping(source = "tx.sourceId", target = "sourceId")
    TransactionEntryDto toEntry(Transaction tx, TransactionRecord record);
}
```

---

### 2b — `GetTransactionHistoryService.java`

```java
package com.github.jenkaby.bikerental.finance.application.service;

import com.github.jenkaby.bikerental.finance.application.mapper.TransactionEntryMapper;
import com.github.jenkaby.bikerental.finance.application.usecase.GetTransactionHistoryUseCase;
import com.github.jenkaby.bikerental.finance.domain.model.CustomerAccount;
import com.github.jenkaby.bikerental.finance.domain.model.Transaction;
import com.github.jenkaby.bikerental.finance.domain.model.TransactionHistoryFilter;
import com.github.jenkaby.bikerental.finance.domain.repository.AccountRepository;
import com.github.jenkaby.bikerental.finance.domain.repository.TransactionRepository;
import com.github.jenkaby.bikerental.shared.domain.CustomerRef;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Page;
import com.github.jenkaby.bikerental.shared.domain.model.vo.PageRequest;
import com.github.jenkaby.bikerental.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
class GetTransactionHistoryService implements GetTransactionHistoryUseCase {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final TransactionEntryMapper transactionEntryMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<TransactionEntryDto> execute(UUID customerId, TransactionHistoryFilter filter, PageRequest pageRequest) {
        var customerRef = CustomerRef.of(customerId);
        accountRepository.findByCustomerId(customerRef)
                .orElseThrow(() -> new ResourceNotFoundException(CustomerAccount.class, customerId));

        Page<Transaction> page = transactionRepository.findTransactionHistory(customerRef, filter, pageRequest);

        List<TransactionEntryDto> entries = page.items().stream()
                .flatMap(tx -> tx.getRecords().stream()
                        .map(record -> transactionEntryMapper.toEntry(tx, record)))
                .toList();

        return new Page<>(entries, page.totalItems(), pageRequest);
    }
}
```

## 4. Validation Steps

```bash
./gradlew :service:compileJava
```

Build must complete with zero errors.
