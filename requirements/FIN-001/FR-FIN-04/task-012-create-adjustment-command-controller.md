# Task 012: Create `AdjustmentCommandController`

> **Applied Skill:** N/A — mirrors `DepositCommandController` structure precisely (constructor injection,
> `@Validated`, `@Valid` on body, `201 Created` response, structured log lines).

## 1. Objective

Expose `POST /api/finance/adjustments` as the HTTP entry point for manual balance adjustments. The controller
validates the request DTO via Bean Validation, delegates to `ApplyAdjustmentUseCase`, and returns
`201 Created` with the `AdjustmentResponse` payload.

## 2. File to Modify / Create

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/finance/web/command/AdjustmentCommandController.java`
* **Action:** Create New File

## 3. Code Implementation

**Imports Required:** All listed below are included in the snippet.

**Code to Add/Replace:**

* **Location:** New file — paste the entire snippet as the file content.
* **Snippet:**

```java
package com.github.jenkaby.bikerental.finance.web.command;

import com.github.jenkaby.bikerental.finance.application.usecase.ApplyAdjustmentUseCase;
import com.github.jenkaby.bikerental.finance.web.command.dto.AdjustmentRequest;
import com.github.jenkaby.bikerental.finance.web.command.dto.AdjustmentResponse;
import com.github.jenkaby.bikerental.finance.web.command.mapper.AdjustmentCommandMapper;
import com.github.jenkaby.bikerental.shared.config.OpenApiConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
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
@RequestMapping("/api/finance/adjustments")
@Tag(name = OpenApiConfig.Tags.FINANCE)
@Slf4j
public class AdjustmentCommandController {

    private final ApplyAdjustmentUseCase applyAdjustmentUseCase;
    private final AdjustmentCommandMapper mapper;

    public AdjustmentCommandController(ApplyAdjustmentUseCase applyAdjustmentUseCase,
                                       AdjustmentCommandMapper mapper) {
        this.applyAdjustmentUseCase = applyAdjustmentUseCase;
        this.mapper = mapper;
    }

    @Operation(summary = "Apply a manual balance adjustment to a customer wallet")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Adjustment applied",
                    content = @Content(schema = @Schema(implementation = AdjustmentResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Customer finance account not found",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "422", description = "Insufficient wallet balance",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping
    public ResponseEntity<AdjustmentResponse> applyAdjustment(@Valid @RequestBody AdjustmentRequest request) {
        log.info("[POST] Apply adjustment request customerId={} amount={}", request.customerId(), request.amount());

        var command = mapper.toCommand(request);
        var result = applyAdjustmentUseCase.execute(command);

        log.info("[POST] Adjustment applied transactionId={} customerId={}", result.transactionId(), request.customerId());

        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(result));
    }
}
```

> **OpenAPI note:** `OpenApiConfig.Tags.FINANCE` must exist. If the constant is missing from `OpenApiConfig`,
> add `public static final String FINANCE = "Finance";` inside the nested `Tags` class following the same pattern
> as other existing tag constants.

## 4. Validation Steps

```bash
./gradlew :service:compileJava
```
