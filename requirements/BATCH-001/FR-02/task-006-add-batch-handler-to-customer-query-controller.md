# Task 006: Add Batch Handler to `CustomerQueryController`

> **Applied Skill:** N/A — standard Spring MVC handler addition with Bean Validation on query parameter and
> OpenAPI annotations following the existing controller patterns.

## 1. Objective

Inject `GetCustomersByIdsUseCase` into `CustomerQueryController` and add the
`GET /api/customers/batch?ids=…` handler. The handler validates that `ids` is present, contains at most 100
valid UUIDs (malformed UUIDs are rejected by Spring's type conversion), de-duplicates, delegates to the use
case, and maps the result with `CustomerWebMapper.toResponses()`.

## 2. File to Modify / Create

* **File Path:** `service/src/main/java/com/github/jenkaby/bikerental/customer/web/query/CustomerQueryController.java`
* **Action:** Modify Existing File

## 3. Code Implementation

**Imports Required:**

Add the following imports to the existing import block:

```java
import com.github.jenkaby.bikerental.customer.application.usecase.GetCustomersByIdsUseCase;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
```

(`java.util.List`, `java.util.UUID`, and `io.swagger.v3.oas.annotations.media.ArraySchema` are already imported.)

**Code to Add/Replace:**

### Step A — Add field and update constructor

* **Location:** Replace the existing field block and constructor of `CustomerQueryController`.

* **Current code:**

```java
    private final CustomerQueryUseCase customerQueryUseCase;
    private final CustomerWebMapper mapper;
    private final GetCustomerByIdUseCase getCustomerByIdUseCase;

    CustomerQueryController(CustomerQueryUseCase customerQueryUseCase,
                            GetCustomerByIdUseCase getCustomerByIdUseCase,
                            CustomerWebMapper customerMapper) {
        this.customerQueryUseCase = customerQueryUseCase;
        this.mapper = customerMapper;
        this.getCustomerByIdUseCase = getCustomerByIdUseCase;
    }
```

* **Snippet (replace with):**

```java
    private final CustomerQueryUseCase customerQueryUseCase;
    private final CustomerWebMapper mapper;
    private final GetCustomerByIdUseCase getCustomerByIdUseCase;
    private final GetCustomersByIdsUseCase getCustomersByIdsUseCase;

    CustomerQueryController(CustomerQueryUseCase customerQueryUseCase,
                            GetCustomerByIdUseCase getCustomerByIdUseCase,
                            GetCustomersByIdsUseCase getCustomersByIdsUseCase,
                            CustomerWebMapper customerMapper) {
        this.customerQueryUseCase = customerQueryUseCase;
        this.mapper = customerMapper;
        this.getCustomerByIdUseCase = getCustomerByIdUseCase;
        this.getCustomersByIdsUseCase = getCustomersByIdsUseCase;
    }
```

---

### Step B — Add the batch handler method

* **Location:** Inside the `CustomerQueryController` class body, immediately **before** the closing `}` of
  the class (i.e., after the existing `getById` method).

* **Snippet:**

```java
    @GetMapping("/batch")
    @Operation(
            summary = "Batch get customers by UUIDs",
            description = "Returns a flat list of customer records for the provided UUIDs. UUIDs that do not match any record are silently omitted.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Customer list returned",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = CustomerResponse.class)))),
            @ApiResponse(responseCode = "400", description = "Invalid ids parameter — missing, malformed UUID, or more than 100 elements",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<List<CustomerResponse>> getCustomersBatch(
            @Parameter(description = "Comma-separated list of customer UUIDs, 1–100 elements",
                    example = "3fa85f64-5717-4562-b3fc-2c963f66afa6,9cb12d11-0000-0000-0000-000000000002")
            @RequestParam(name = "ids")
            @NotEmpty(message = "ids must not be empty")
            @Size(max = 100, message = "ids must contain at most 100 elements")
            List<UUID> ids) {
        log.info("[GET] Batch fetch customers ids count={}", ids.size());
        var distinctIds = ids.stream().distinct().toList();
        var customers = getCustomersByIdsUseCase.execute(distinctIds);
        return ResponseEntity.ok(mapper.toResponses(customers));
    }
```

## 4. Validation Steps

skip