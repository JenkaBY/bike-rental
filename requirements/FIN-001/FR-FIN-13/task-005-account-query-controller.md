# Task 005: Create `AccountQueryController`

> **Applied Skill:** `springboot` — REST controller following the CQRS query-controller pattern; path variable
> UUID binding; SpringDoc OpenAPI annotations consistent with existing finance controllers.

## 1. Objective

Expose `GET /api/finance/customers/{customerId}/balances`. The controller validates that `customerId` is a
well-formed `UUID` (Spring MVC type binding rejects malformed values with `400`), delegates to
`GetCustomerAccountBalancesUseCase`, maps the result with `AccountQueryMapper`, and returns `200 OK`. All error
handling is delegated to `FinanceRestControllerAdvice` (module-scoped) and `CoreExceptionHandlerAdvice` (global).

## 2. File to Create

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/finance/web/query/AccountQueryController.java`
* **Action:** Create New File

## 3. Code Implementation

**Imports Required:**

```java
import com.github.jenkaby.bikerental.finance.application.usecase.GetCustomerAccountBalancesUseCase;
import com.github.jenkaby.bikerental.finance.web.query.dto.CustomerAccountBalancesResponse;
import com.github.jenkaby.bikerental.finance.web.query.mapper.AccountQueryMapper;
import com.github.jenkaby.bikerental.shared.config.OpenApiConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;
```

**Full file content:**

```java
package com.github.jenkaby.bikerental.finance.web.query;

import com.github.jenkaby.bikerental.finance.application.usecase.GetCustomerAccountBalancesUseCase;
import com.github.jenkaby.bikerental.finance.web.query.dto.CustomerAccountBalancesResponse;
import com.github.jenkaby.bikerental.finance.web.query.mapper.AccountQueryMapper;
import com.github.jenkaby.bikerental.shared.config.OpenApiConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Validated
@RestController
@RequestMapping("/api/finance/customers")
@Tag(name = OpenApiConfig.Tags.FINANCE)
@Slf4j
public class AccountQueryController {

    private final GetCustomerAccountBalancesUseCase getCustomerAccountBalancesUseCase;
    private final AccountQueryMapper mapper;

    public AccountQueryController(GetCustomerAccountBalancesUseCase getCustomerAccountBalancesUseCase,
                                  AccountQueryMapper mapper) {
        this.getCustomerAccountBalancesUseCase = getCustomerAccountBalancesUseCase;
        this.mapper = mapper;
    }

    @GetMapping("/{customerId}/balances")
    @Operation(summary = "Retrieve customer account balances")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Account balances retrieved",
                    content = @Content(schema = @Schema(implementation = CustomerAccountBalancesResponse.class))),
            @ApiResponse(responseCode = "404", description = "Customer finance account not found",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "400", description = "Invalid customer UUID format",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<CustomerAccountBalancesResponse> getBalances(
            @Parameter(description = "Customer UUID") @PathVariable("customerId") UUID customerId) {
        log.info("[GET] Retrieve balances for customerId={}", customerId);
        var result = getCustomerAccountBalancesUseCase.execute(customerId);
        return ResponseEntity.ok(mapper.toResponse(result));
    }
}
```

**Notes:**

- `@RequestMapping("/api/finance/customers")` follows REST resource naming: the sub-resource path is
  `/{customerId}/balances` added at method level with `@GetMapping`.
- The `UUID` path variable type means Spring MVC rejects malformed UUIDs with `400` before the method is
  invoked — no explicit `@Pattern` annotation is required.
- Constructor injection without Lombok `@RequiredArgsConstructor` is used intentionally to match the style of
  existing finance controllers (`DepositCommandController`).

## 4. Validation Steps

```bash
./gradlew :service:compileJava
```

Expected: compiles without errors; Spring discovers the `@RestController` bean on startup.
