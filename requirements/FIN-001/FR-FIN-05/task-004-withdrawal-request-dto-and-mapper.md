# Task 004: Create RecordWithdrawalRequest DTO and WithdrawalCommandMapper

> **Applied Skill:** `mapstruct-hexagonal` — web/command layer mapper composing `MoneyMapper` and
> `IdempotencyKeyMapper`; mirrors `DepositCommandMapper` / `RecordDepositRequest` pattern exactly.

## 1. Objective

Create the `RecordWithdrawalRequest` record (HTTP request DTO with Bean Validation annotations) and the
`WithdrawalCommandMapper` MapStruct interface that converts it to `RecordWithdrawalCommand` and maps
`WithdrawalResult` back to `TransactionResponse`.

## 2. File to Modify / Create

* **File Path (1):**
  `service/src/main/java/com/github/jenkaby/bikerental/finance/web/command/dto/RecordWithdrawalRequest.java`
* **Action:** Create New File

* **File Path (2):**
  `service/src/main/java/com/github/jenkaby/bikerental/finance/web/command/mapper/WithdrawalCommandMapper.java`
* **Action:** Create New File

## 3. Code Implementation

### File 1 — `RecordWithdrawalRequest.java`

**Imports Required:**

```java
import com.github.jenkaby.bikerental.finance.PaymentMethod;
import com.github.jenkaby.bikerental.shared.web.support.MoneyAmount;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;
```

**Code to Add/Replace:**

* **Location:** New file — full contents below.

```java
package com.github.jenkaby.bikerental.finance.web.command.dto;

import com.github.jenkaby.bikerental.finance.PaymentMethod;
import com.github.jenkaby.bikerental.shared.web.support.MoneyAmount;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Request body for recording a fund withdrawal")
public record RecordWithdrawalRequest(
        @Schema(description = "Client-generated UUID sent with every request to ensure exactly-once submission",
                example = "018e2cc3-0000-7000-8000-000000000099")
        @NotNull UUID idempotencyKey,

        @Schema(description = "Customer UUID", example = "018e2cc3-0000-7000-8000-000000000001")
        @NotNull UUID customerId,

        @Schema(description = "Withdrawal amount", example = "30.00")
        @NotNull @DecimalMin(value = "0.01") @MoneyAmount BigDecimal amount,

        @Schema(description = "Payout method (CASH, CARD_TERMINAL, BANK_TRANSFER)")
        @NotNull PaymentMethod payoutMethod,

        @Schema(description = "Operator identifier", example = "operator-1")
        @NotBlank String operatorId
) {
}
```

---

### File 2 — `WithdrawalCommandMapper.java`

**Imports Required:**

```java
import com.github.jenkaby.bikerental.finance.application.usecase.RecordWithdrawalUseCase.RecordWithdrawalCommand;
import com.github.jenkaby.bikerental.finance.application.usecase.RecordWithdrawalUseCase.WithdrawalResult;
import com.github.jenkaby.bikerental.finance.web.command.dto.RecordWithdrawalRequest;
import com.github.jenkaby.bikerental.finance.web.command.dto.TransactionResponse;
import com.github.jenkaby.bikerental.shared.mapper.IdempotencyKeyMapper;
import com.github.jenkaby.bikerental.shared.mapper.MoneyMapper;
import org.mapstruct.Mapper;
```

**Code to Add/Replace:**

* **Location:** New file — full contents below.

```java
package com.github.jenkaby.bikerental.finance.web.command.mapper;

import com.github.jenkaby.bikerental.finance.application.usecase.RecordWithdrawalUseCase.RecordWithdrawalCommand;
import com.github.jenkaby.bikerental.finance.application.usecase.RecordWithdrawalUseCase.WithdrawalResult;
import com.github.jenkaby.bikerental.finance.web.command.dto.RecordWithdrawalRequest;
import com.github.jenkaby.bikerental.finance.web.command.dto.TransactionResponse;
import com.github.jenkaby.bikerental.shared.mapper.IdempotencyKeyMapper;
import com.github.jenkaby.bikerental.shared.mapper.MoneyMapper;
import org.mapstruct.Mapper;

@Mapper(uses = {MoneyMapper.class, IdempotencyKeyMapper.class})
public interface WithdrawalCommandMapper {

    RecordWithdrawalCommand toCommand(RecordWithdrawalRequest request);

    TransactionResponse toResponse(WithdrawalResult result);
}
```

> **MapStruct note:** `MoneyMapper` converts `BigDecimal amount` → `Money amount`. `IdempotencyKeyMapper`
> converts `UUID idempotencyKey` → `IdempotencyKey idempotencyKey`. Both source (`RecordWithdrawalRequest`)
> and target (`RecordWithdrawalCommand`) use the same field names (`customerId`, `amount`, `payoutMethod`,
> `operatorId`, `idempotencyKey`), so no explicit `@Mapping` is required.

## 4. Validation Steps

```bash
./gradlew :service:compileJava
```
