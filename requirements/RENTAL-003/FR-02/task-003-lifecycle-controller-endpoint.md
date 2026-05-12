# Task 003: Add Lifecycle Endpoint to RentalCommandController

> **Applied Skill:** `springboot.instructions.md` — thin controller, constructor injection;
> `java.instructions.md` — no inline branching in controllers

## 1. Objective

Add the `PATCH /api/rentals/{rentalId}/lifecycles` handler method to `RentalCommandController`.
The controller validates the request, converts the web-layer `LifecycleStatus` to `RentalStatus`,
and delegates unconditionally to `RentalLifecycleUseCase`.

## 2. File to Modify / Create

* **File Path:** `service/src/main/java/com/github/jenkaby/bikerental/rental/web/command/RentalCommandController.java`
* **Action:** Modify Existing File

## 3. Code Implementation

**Imports Required:**

Add the following imports to the existing import block:

```java
import com.github.jenkaby.bikerental.rental.application.usecase.RentalLifecycleUseCase;
import com.github.jenkaby.bikerental.rental.web.command.dto.RentalLifecycleRequest;
import jakarta.validation.constraints.Positive;
```

> `@Positive`, `@Valid`, `@NotNull`, `ResponseEntity`, `ProblemDetail` are already imported.

**Step 1 — Add field to the controller.**

* **Location:** After the existing `private final RentalQueryMapper queryMapper;` field declaration.

```java
    private final RentalLifecycleUseCase rentalLifecycleUseCase;
```

**Step 2 — Add constructor parameter.**

* **Location:** In the constructor, after the last existing parameter `RentalQueryMapper queryMapper`.
  Add `RentalLifecycleUseCase rentalLifecycleUseCase` as the last parameter and assign it.

Replace the existing constructor with:

```java
    RentalCommandController(
            CreateRentalUseCase createRentalUseCase,
            UpdateRentalUseCase updateRentalUseCase,
            ReturnEquipmentUseCase returnEquipmentUseCase,
            RentalCommandMapper commandMapper,
            RentalQueryMapper queryMapper,
            RentalLifecycleUseCase rentalLifecycleUseCase) {
        this.createRentalUseCase = createRentalUseCase;
        this.updateRentalUseCase = updateRentalUseCase;
        this.returnEquipmentUseCase = returnEquipmentUseCase;
        this.commandMapper = commandMapper;
        this.queryMapper = queryMapper;
        this.rentalLifecycleUseCase = rentalLifecycleUseCase;
    }
```

**Step 3 — Add the new handler method.**

* **Location:** Add as the last method in the class, before the closing `}`.

```java
    @PatchMapping("/{rentalId}/lifecycles")
    @Operation(summary = "Transition rental lifecycle status",
            description = "Transitions a rental to ACTIVE or CANCELLED status")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Rental status updated",
                    content = @Content(schema = @Schema(implementation = RentalResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Rental not found",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "422", description = "Invalid status transition",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<RentalResponse> updateLifecycle(
            @Positive @PathVariable Long rentalId,
            @Valid @RequestBody RentalLifecycleRequest request) {
        log.info("[PATCH] Lifecycle transition for rentalId={}, targetStatus={}", rentalId, request.status());
        var command = new RentalLifecycleUseCase.RentalLifecycleCommand(
                rentalId,
                RentalStatus.valueOf(request.status().name()),
                request.operatorId());
        var rental = rentalLifecycleUseCase.execute(command);
        var response = queryMapper.toResponse(rental);
        log.info("[PATCH] Rental {} lifecycle updated to {}", rentalId, rental.getStatus());
        return ResponseEntity.ok(response);
    }
```

## 4. Validation Steps

```bash
./gradlew :service:compileJava
```
