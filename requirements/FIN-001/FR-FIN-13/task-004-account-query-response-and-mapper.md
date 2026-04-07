# Task 004: Create Response DTO and `AccountQueryMapper`

> **Applied Skill:** `mapstruct-hexagonal` — web-layer DTO + MapStruct interface that maps the application
> use-case result record to the HTTP response record; field names and types match so no explicit `@Mapping`
> annotations are required.

## 1. Objective

Create the `CustomerAccountBalancesResponse` record (the JSON response body) and the `AccountQueryMapper`
MapStruct interface that converts `GetCustomerAccountBalancesUseCase.CustomerAccountBalances` →
`CustomerAccountBalancesResponse`. Both files live in the `finance/web/query/` layer.

## 2. Files to Create

### File 1 — Response DTO

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/finance/web/query/dto/CustomerAccountBalancesResponse.java`
* **Action:** Create New File

**Full file content:**

```java
package com.github.jenkaby.bikerental.finance.web.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;

@Schema(description = "Customer account balance breakdown")
public record CustomerAccountBalancesResponse(
        @Schema(description = "Available (spendable) wallet balance", example = "120.00")
        BigDecimal walletBalance,
        @Schema(description = "Reserved (held) balance for active rentals", example = "30.00")
        BigDecimal holdBalance,
        @Schema(description = "Timestamp of the most recent ledger mutation (UTC ISO-8601)")
        Instant lastUpdatedAt
) {
}
```

---

### File 2 — MapStruct Mapper

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/finance/web/query/mapper/AccountQueryMapper.java`
* **Action:** Create New File

**Full file content:**

```java
package com.github.jenkaby.bikerental.finance.web.query.mapper;

import com.github.jenkaby.bikerental.finance.application.usecase.GetCustomerAccountBalancesUseCase.CustomerAccountBalances;
import com.github.jenkaby.bikerental.finance.web.query.dto.CustomerAccountBalancesResponse;
import org.mapstruct.Mapper;

@Mapper
public interface AccountQueryMapper {

    CustomerAccountBalancesResponse toResponse(CustomerAccountBalances domain);
}
```

**Notes:**

- All three fields (`walletBalance: BigDecimal`, `holdBalance: BigDecimal`, `lastUpdatedAt: Instant`) have
  identical names and types in both source and target — MapStruct auto-maps without any `@Mapping` annotations.
- No `uses` clause is needed because no custom type conversion is required.
- The `@Mapper` annotation (with no `componentModel`) inherits the global compiler flag
  `-Amapstruct.defaultComponentModel=spring`, so the generated implementation is registered as a Spring bean.

## 4. Validation Steps

```bash
./gradlew :service:compileJava
```

Expected: MapStruct generates `AccountQueryMapperImpl` with no unmapped-field errors.
