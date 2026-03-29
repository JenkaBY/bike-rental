# Task 011: Create `AdjustmentCommandMapper`

> **Applied Skill:** `mapstruct-hexagonal` — Pattern 1 (simple interface mapper), Pattern 3 (`@Mapper(uses={...})`
> composition with `MoneyMapper` for `BigDecimal ↔ Money` conversion). Web-command layer placement:
> `finance/web/command/mapper/`.

## 1. Objective

Create the MapStruct mapper that converts between the `AdjustmentRequest` HTTP DTO and the
`ApplyAdjustmentUseCase.ApplyAdjustmentCommand` use-case record, and converts the
`ApplyAdjustmentUseCase.AdjustmentResult` back to the `AdjustmentResponse` HTTP DTO.

`MoneyMapper` handles `BigDecimal → Money` (command direction) and `Money → BigDecimal` (response direction)
automatically via type-matching.

## 2. File to Modify / Create

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/finance/web/command/mapper/AdjustmentCommandMapper.java`
* **Action:** Create New File

## 3. Code Implementation

**Imports Required:** All listed below are included in the snippet.

**Code to Add/Replace:**

* **Location:** New file — paste the entire snippet as the file content.
* **Snippet:**

```java
package com.github.jenkaby.bikerental.finance.web.command.mapper;

import com.github.jenkaby.bikerental.finance.application.usecase.ApplyAdjustmentUseCase;
import com.github.jenkaby.bikerental.finance.application.usecase.ApplyAdjustmentUseCase.AdjustmentResult;
import com.github.jenkaby.bikerental.finance.application.usecase.ApplyAdjustmentUseCase.ApplyAdjustmentCommand;
import com.github.jenkaby.bikerental.finance.web.command.dto.AdjustmentRequest;
import com.github.jenkaby.bikerental.finance.web.command.dto.AdjustmentResponse;
import com.github.jenkaby.bikerental.shared.mapper.MoneyMapper;
import org.mapstruct.Mapper;

@Mapper(uses = {MoneyMapper.class})
public interface AdjustmentCommandMapper {

    ApplyAdjustmentCommand toCommand(AdjustmentRequest request);

    AdjustmentResponse toResponse(AdjustmentResult result);
}
```

> **MapStruct note:** `AdjustmentRequest.amount` is `BigDecimal`; `ApplyAdjustmentCommand.amount` is `Money`.
> `MoneyMapper.toMoney(BigDecimal)` is matched automatically. `AdjustmentRequest.idempotencyKey` is `UUID`;
> `ApplyAdjustmentCommand.idempotencyKey` is `IdempotencyKey`. Add an explicit `@Mapping` to convert it:
>
> ```java
> @Mapping(target = "idempotencyKey", expression = "java(com.github.jenkaby.bikerental.shared.domain.IdempotencyKey.of(request.idempotencyKey()))")
> ApplyAdjustmentCommand toCommand(AdjustmentRequest request);
> ```
>
> For the response, `AdjustmentResult.newWalletBalance` is `Money`; `AdjustmentResponse.newWalletBalance` is
> `BigDecimal`. `MoneyMapper.toBigDecimal(Money)` is matched automatically.

## 4. Validation Steps

```bash
./gradlew :service:compileJava
```
