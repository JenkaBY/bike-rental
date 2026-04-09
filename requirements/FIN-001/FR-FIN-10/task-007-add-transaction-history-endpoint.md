# Task 007: Add Transaction History Endpoint to AccountQueryController

> **Applied Skill:** `spring-boot-data-ddd` — Controller integration; `spring-mvc-controller-test` — @ApiTest pattern

## 1. Objective

Add a `GET /api/finance/customers/{customerId}/transactions` endpoint to the existing `AccountQueryController`. The
endpoint accepts `customerId` as a UUID path variable, a `Pageable` resolved by Spring Data Web (default size 20),
and four optional filter query parameters combined into a `TransactionHistoryFilterParams` web DTO bound via
`@ModelAttribute`. It delegates to `GetTransactionHistoryUseCase` and returns `Page<TransactionEntryResponse>`
(shared VO) so no custom wrapper record is needed.

## 2. File to Modify / Create

### 2a.

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/finance/web/query/dto/TransactionHistoryFilterParams.java`
* **Action:** Create New File

### 2b.

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/finance/web/query/dto/TransactionEntryResponse.java`
* **Action:** Create New File

### 2c.

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/finance/web/query/mapper/TransactionHistoryQueryMapper.java`
* **Action:** Create New File

### 2d.

* **File Path:** `service/src/main/java/com/github/jenkaby/bikerental/finance/web/query/AccountQueryController.java`
* **Action:** Modify Existing File

## 3. Code Implementation

### 2a — `TransactionHistoryFilterParams.java`

A thin web-layer record that groups the four optional filter query parameters. Spring MVC binds it via
`@ModelAttribute` — no `@RequestParam` needed on the controller method. The `@DateTimeFormat` annotation on
`LocalDate` fields is required for Spring to parse `yyyy-MM-dd` strings.

```java
package com.github.jenkaby.bikerental.finance.web.query.dto;

import com.github.jenkaby.bikerental.finance.domain.model.TransactionSourceType;
import org.jspecify.annotations.Nullable;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public record TransactionHistoryFilterParams(
        @Nullable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
        @Nullable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
        @Nullable String sourceId,
        @Nullable TransactionSourceType sourceType) {
}
```

---

### 2b — `TransactionEntryResponse.java`

```java
package com.github.jenkaby.bikerental.finance.web.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.time.Instant;

@Schema(description = "A single journal entry line in the transaction history")
public record TransactionEntryResponse(
        @Schema(description = "Sub-ledger this entry belongs to") String subLedger,
        @Schema(description = "Customer UUID as string") String customerId,
        @Schema(description = "Entry amount") BigDecimal amount,
        @Schema(description = "DEBIT or CREDIT") String direction,
        @Schema(description = "Business transaction type") String type,
        @Schema(description = "When the entry was recorded (UTC ISO-8601)") Instant recordedAt,
        @Nullable @Schema(description = "Payment method, present for deposits and withdrawals") String paymentMethod,
        @Nullable @Schema(description = "Free-text reason, present for adjustments") String reason,
        @Nullable @Schema(description = "Source type, e.g. RENTAL") String sourceType,
        @Nullable @Schema(description = "Source identifier") String sourceId
) {
}
```

---

### 2c — `TransactionHistoryQueryMapper.java`

```java
package com.github.jenkaby.bikerental.finance.web.query.mapper;

import com.github.jenkaby.bikerental.finance.application.usecase.GetTransactionHistoryUseCase.TransactionEntryDto;
import com.github.jenkaby.bikerental.finance.web.query.dto.TransactionResponse;
import org.mapstruct.Mapper;

@Mapper
public interface TransactionHistoryQueryMapper {

    TransactionEntryResponse toEntry(TransactionEntryDto dto);
}
```

---

### 2d — `AccountQueryController.java`

**Imports Required** (add to the existing import block):

```java
import com.github.jenkaby.bikerental.finance.application.usecase.GetTransactionHistoryUseCase;
import com.github.jenkaby.bikerental.finance.domain.model.TransactionHistoryFilter;
import com.github.jenkaby.bikerental.finance.web.query.dto.TransactionResponse;
import com.github.jenkaby.bikerental.finance.web.query.dto.TransactionHistoryFilterParams;
import com.github.jenkaby.bikerental.finance.web.query.mapper.TransactionHistoryQueryMapper;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Page;
import com.github.jenkaby.bikerental.shared.domain.model.vo.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.ModelAttribute;
```

**Location:** In the constructor of `AccountQueryController`, add two new injected fields, and add the new endpoint
method after `getBalances(...)`.

Replace the current constructor and class fields block:

```java
    private final GetCustomerAccountBalancesUseCase getCustomerAccountBalancesUseCase;
    private final AccountQueryMapper mapper;

    public AccountQueryController(GetCustomerAccountBalancesUseCase getCustomerAccountBalancesUseCase,
                                  AccountQueryMapper mapper) {
        this.getCustomerAccountBalancesUseCase = getCustomerAccountBalancesUseCase;
        this.mapper = mapper;
    }
```

With:

```java
    private final GetCustomerAccountBalancesUseCase getCustomerAccountBalancesUseCase;
    private final AccountQueryMapper mapper;
    private final GetTransactionHistoryUseCase getTransactionHistoryUseCase;
    private final TransactionHistoryQueryMapper transactionHistoryQueryMapper;

    public AccountQueryController(GetCustomerAccountBalancesUseCase getCustomerAccountBalancesUseCase,
                                  AccountQueryMapper mapper,
                                  GetTransactionHistoryUseCase getTransactionHistoryUseCase,
                                  TransactionHistoryQueryMapper transactionHistoryQueryMapper) {
        this.getCustomerAccountBalancesUseCase = getCustomerAccountBalancesUseCase;
        this.mapper = mapper;
        this.getTransactionHistoryUseCase = getTransactionHistoryUseCase;
        this.transactionHistoryQueryMapper = transactionHistoryQueryMapper;
    }
```

**Location:** After the closing `}` of `getBalances(...)`, before the class closing `}`.

**New endpoint snippet:**

```java
    @GetMapping("/{customerId}/transactions")
    @Operation(summary = "Retrieve paginated transaction history for a customer")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Transaction history retrieved",
                    content = @Content(schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "404", description = "Customer finance account not found",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<Page<TransactionEntryResponse>> getTransactionHistory(
            @Parameter(description = "Customer UUID") @PathVariable("customerId") UUID customerId,
            @ModelAttribute TransactionHistoryFilterParams filterParams,
            @PageableDefault(size = 20) Pageable pageable) {

        log.info("[GET] Transaction history for customerId={} page={} size={}",
                customerId, pageable.getPageNumber(), pageable.getPageSize());
        var filter = new TransactionHistoryFilter(
                filterParams.fromDate(), filterParams.toDate(),
                filterParams.sourceId(), filterParams.sourceType());
        var pageRequest = new PageRequest(pageable.getPageSize(), pageable.getPageNumber(), null);
        var result = getTransactionHistoryUseCase.execute(customerId, filter, pageRequest);
        return ResponseEntity.ok(result.map(transactionHistoryQueryMapper::toEntry));
    }
```

## 4. Validation Steps

```bash
./gradlew :service:compileJava
```

Build must complete with zero errors.
