# Task 001: Add `GET /api/customers/{id}` endpoint to `CustomerQueryController`

> **Applied Skill:** spring-boot-modulith (architecture boundaries) - keep controller inside `customer` module;
> spring-mvc-controller-test (controller conventions) - follow validation & ProblemDetail conventions

## 1. Objective

Add a read endpoint `GET /api/customers/{id}` to return a full `CustomerResponse` by UUID using the existing
`CustomerQueryUseCase.findById(UUID)` use-case.

## 2. File to Modify / Create

* **File Path:** `service/src/main/java/com/github/jenkaby/bikerental/customer/web/query/CustomerQueryController.java`
* **Action:** Modify Existing File

## 3. Code Implementation

**Imports Required:**

```java
import java.util.UUID;
import org.springframework.web.bind.annotation.PathVariable;
import com.github.jenkaby.bikerental.customer.web.query.dto.CustomerResponse;
import com.github.jenkaby.bikerental.shared.exception.ResourceNotFoundException;
import com.github.jenkaby.bikerental.customer.domain.model.Customer;
```

**Code to Add/Replace:**

* **Location:** Inside `CustomerQueryController` class, add the new handler method after the existing
  `searchByPhone(...)` method.
* **Snippet:**

```java
    @GetMapping("/{id}")
    @Operation(summary = "Get a customer by UUID", description = "Returns full customer profile by UUID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Customer returned",
                    content = @Content(schema = @Schema(implementation = CustomerResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid UUID",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Customer not found",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<CustomerResponse> getById(
            @Parameter(description = "Customer UUID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
            @PathVariable("id") UUID id) {
        log.info("[GET] Fetching customer by id: {}", id);
        var opt = customerQueryUseCase.findById(id);
        var info = opt.orElseThrow(() -> new ResourceNotFoundException(Customer.class, id));
        return ResponseEntity.ok(mapper.toResponse(info));
    }
```

Notes:

- Use `ResourceNotFoundException(Customer.class, id)` so the global advice produces `shared.resource.not_found`
  errorCode.
- Accept `UUID` directly as `@PathVariable` — invalid UUID format will be handled by existing controller advice as 400.

## 4. Validation Steps

Run the controller slice test for `CustomerQueryController` after implementing the corresponding tests (Task 004):

```bash
./gradlew :service:test "-Dspring.profiles.active=test" --tests com.github.jenkaby.bikerental.customer.web.query.CustomerQueryControllerTest
```
