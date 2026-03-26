# Task 012: Create DepositCommandController with Request/Response DTOs and Mapper

> **Applied Skill:** `mapstruct-hexagonal` — web/command mapper; `spring-mvc-controller-test` — validation
> annotations on DTO; follows exact same style as `PaymentCommandController` / `RecordPaymentRequest`.

## 1. Objective

Create the HTTP entry point for the deposit feature: a `RecordDepositRequest` DTO, a `RecordDepositResponse`
DTO, a `DepositCommandMapper` MapStruct interface, and the `DepositCommandController` REST controller.

## 2. Files to Create

| # | File Path | Action |
|---|-----------|--------|
| 1 | `service/src/main/java/com/github/jenkaby/bikerental/finance/web/command/dto/RecordDepositRequest.java` | Create New File |
| 2 | `service/src/main/java/com/github/jenkaby/bikerental/finance/web/command/dto/RecordDepositResponse.java` | Create New File |
| 3 | `service/src/main/java/com/github/jenkaby/bikerental/finance/web/command/mapper/DepositCommandMapper.java` | Create New File |
| 4 | `service/src/main/java/com/github/jenkaby/bikerental/finance/web/command/DepositCommandController.java` | Create New File |

## 3. Code Implementation

### File 1 — `RecordDepositRequest.java`

```java
package com.github.jenkaby.bikerental.finance.web.command.dto;

import com.github.jenkaby.bikerental.finance.PaymentMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Request body for recording a fund deposit")
public record RecordDepositRequest(
        @Schema(description = "Client-generated UUID sent with every request to ensure exactly-once submission",
                example = "018e2cc3-0000-7000-8000-000000000099")
        @NotNull UUID idempotencyKey,

        @Schema(description = "Customer UUID", example = "018e2cc3-0000-7000-8000-000000000001")
        @NotNull UUID customerId,

        @Schema(description = "Deposit amount", example = "50.00")
        @NotNull @DecimalMin(value = "0.01") BigDecimal amount,

        @Schema(description = "Payment method (CASH, CARD_TERMINAL, BANK_TRANSFER)")
        @NotNull PaymentMethod paymentMethod,

        @Schema(description = "Operator identifier", example = "operator-1")
        @NotBlank String operatorId
) {
}
```

### File 2 — `RecordDepositResponse.java`

```java
package com.github.jenkaby.bikerental.finance.web.command.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Result of recording a fund deposit")
public record RecordDepositResponse(
        @Schema(description = "Transaction UUID") UUID transactionId,
        @Schema(description = "Timestamp when the deposit was recorded") Instant recordedAt
) {
}
```

### File 3 — `DepositCommandMapper.java`

```java
package com.github.jenkaby.bikerental.finance.web.command.mapper;

import com.github.jenkaby.bikerental.finance.application.usecase.RecordDepositUseCase.DepositResult;
import com.github.jenkaby.bikerental.finance.application.usecase.RecordDepositUseCase.RecordDepositCommand;
import com.github.jenkaby.bikerental.finance.web.command.dto.RecordDepositRequest;
import com.github.jenkaby.bikerental.finance.web.command.dto.RecordDepositResponse;
import org.mapstruct.Mapper;

@Mapper
public interface DepositCommandMapper {

    RecordDepositCommand toCommand(RecordDepositRequest request);

    RecordDepositResponse toResponse(DepositResult result);
}
```

### File 4 — `DepositCommandController.java`

```java
package com.github.jenkaby.bikerental.finance.web.command;

import com.github.jenkaby.bikerental.finance.application.usecase.RecordDepositUseCase;
import com.github.jenkaby.bikerental.finance.web.command.dto.RecordDepositRequest;
import com.github.jenkaby.bikerental.finance.web.command.dto.RecordDepositResponse;
import com.github.jenkaby.bikerental.finance.web.command.mapper.DepositCommandMapper;
import com.github.jenkaby.bikerental.shared.config.OpenApiConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/finance/deposits")
@Tag(name = OpenApiConfig.Tags.FINANCE)
public class DepositCommandController {

    private final RecordDepositUseCase recordDepositUseCase;
    private final DepositCommandMapper mapper;

    public DepositCommandController(RecordDepositUseCase recordDepositUseCase,
                                    DepositCommandMapper mapper) {
        this.recordDepositUseCase = recordDepositUseCase;
        this.mapper = mapper;
    }

    @Operation(summary = "Record a fund deposit for a customer")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Deposit recorded",
                    content = @Content(schema = @Schema(implementation = RecordDepositResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Customer finance account not found",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping
    public ResponseEntity<RecordDepositResponse> recordDeposit(@Valid @RequestBody RecordDepositRequest request) {
        var command = mapper.toCommand(request);
        var result = recordDepositUseCase.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(result));
    }
}
```

## 4. Validation Steps

```bash
./gradlew :service:compileJava
```

MapStruct will validate `DepositCommandMapper` at compile time — all fields in `RecordDepositCommand` and
`DepositResult` must map without errors. Build must succeed with zero unmapped target field warnings treated
as errors.
