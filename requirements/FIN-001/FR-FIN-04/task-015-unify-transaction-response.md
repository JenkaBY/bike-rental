<task_file_template>

# Task 015: Unify Transaction Response for adjustments and deposits

> **Applied Skill:** mapstruct-hexagonal - MapStruct mapper implementation patterns; spring-mvc-controller-test -
> Controller test conventions and expectations

## 1. Objective

Make `/api/finance/adjustments` and `/api/finance/deposits` return the same response DTO
`TransactionResponse(UUID transactionId, BigDecimal walletBalance, Instant recordedAt)` and update mappers, controllers,
and tests accordingly.

## 2. File to Modify / Create

* **File Path:** `service/src/main/java/com/github/jenkaby/bikerental/finance/web/command/dto/TransactionResponse.java`
* **Action:** Create New File

Also modify the following existing files (replace response types and imports):

* `service/src/main/java/com/github/jenkaby/bikerental/finance/web/command/AdjustmentCommandController.java` — Modify
  Existing File
* `service/src/main/java/com/github/jenkaby/bikerental/finance/web/command/DepositCommandController.java` — Modify
  Existing File
* `service/src/main/java/com/github/jenkaby/bikerental/finance/web/command/mapper/AdjustmentCommandMapper.java` — Modify
  Existing File
* `service/src/main/java/com/github/jenkaby/bikerental/finance/web/command/mapper/DepositCommandMapper.java` — Modify
  Existing File
* Update tests:
    * `service/src/test/java/com/github/jenkaby/bikerental/finance/web/command/AdjustmentCommandControllerTest.java`
    * `service/src/test/java/com/github/jenkaby/bikerental/finance/web/command/DepositCommandControllerTest.java`

## 3. Code Implementation

**Imports Required for new DTO:**

```java
package com.github.jenkaby.bikerental.finance.web.command.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
```

**Code to Add (new DTO):**

* **Location:** New file
  `service/src/main/java/com/github/jenkaby/bikerental/finance/web/command/dto/TransactionResponse.java`
* **Snippet:**

```java
package com.github.jenkaby.bikerental.finance.web.command.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransactionResponse(UUID transactionId, BigDecimal walletBalance, Instant recordedAt) {
}
```

**Controller updates (example changes):**

- In `AdjustmentCommandController.java`:
    - Replace import of `AdjustmentResponse` with `TransactionResponse`.
    - Change method signature return type from `ResponseEntity<AdjustmentResponse>` to
      `ResponseEntity<TransactionResponse>`.
    - Ensure the controller uses the mapper that now returns `TransactionResponse`.

Snippet (replace the method signature and return):

```java
// imports: import com.github.jenkaby.bikerental.finance.web.command.dto.TransactionResponse;

public ResponseEntity<TransactionResponse> applyAdjustment(@Valid @RequestBody AdjustmentRequest request) {
    var result = applyAdjustmentUseCase.execute(mapper.toCommand(request));
    return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(result));
}
```

- In `DepositCommandController.java` do the same: replace `RecordDepositResponse` with `TransactionResponse` and return
  `mapper.toResponse(result)` (mapper updated below).

**Mapper updates (MapStruct interfaces):**

- In `AdjustmentCommandMapper.java` change the response mapping signature from
  `AdjustmentResponse toResponse(AdjustmentResult result);` to:

```java
import com.github.jenkaby.bikerental.finance.web.command.dto.TransactionResponse;

TransactionResponse toResponse(ApplyAdjustmentUseCase.AdjustmentResult result);
```

- In `DepositCommandMapper.java` change the response mapping signature from
  `RecordDepositResponse toResponse(DepositResult result);` to:

```java
import com.github.jenkaby.bikerental.finance.web.command.dto.TransactionResponse;

TransactionResponse toResponse(RecordDepositUseCase.DepositResult result);
```

MapStruct will regenerate the implementation; ensure the mapper maps `transactionId`, `recordedAt` and additionally
`walletBalance` (you may need to add a mapping expression if the `DepositResult`/`AdjustmentResult` expose
`walletBalance` as a Money value — map to `BigDecimal` via existing `MoneyMapper` or call `money.getAmount()` depending
on your codebase conventions).

If `DepositResult`/`AdjustmentResult` do not currently expose `walletBalance`, update the use-case result records to
include `BigDecimal walletBalance` (or `Money` and adapt mapping). Example `DepositResult` change (if needed):

```java
public record DepositResult(UUID transactionId, Instant recordedAt, BigDecimal walletBalance) {
}
```

And update the service implementations to return the wallet balance in the result.

**Test updates:**

- Update `AdjustmentCommandControllerTest` and `DepositCommandControllerTest` to expect `TransactionResponse` in mocked
  mapper responses and assertions. Example mocked response:

```java
var response = new TransactionResponse(TRANSACTION_ID, new BigDecimal("60.00"), now);

given(mapper.toResponse(any())).

willReturn(response);
```

Adjust JSON expectations accordingly.

## 4. Validation Steps

Run the following scoped commands to verify compilation and controller tests:

```bash
./gradlew :service:test "-Dspring.profiles.active=test" --tests "com.github.jenkaby.bikerental.finance.web.command.AdjustmentCommandControllerTest"
./gradlew :service:test "-Dspring.profiles.active=test" --tests "com.github.jenkaby.bikerental.finance.web.command.DepositCommandControllerTest"
```

If MapStruct-generated code fails after signature changes, run the full `:service:compileJava` to regenerate sources via
annotation processing:

```bash
./gradlew :service:compileJava
```

</task_file_template>