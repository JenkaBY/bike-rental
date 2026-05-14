# Task 008: Add from/to Query Params to RentalQueryController

> **Applied Skill:** `springboot.instructions.md` — thin controller, no business logic;
> `java.instructions.md` — `@RequestParam` with `required = false`

## 1. Objective

Add two optional `@RequestParam LocalDate from` and `to` parameters to the `getRentals` handler
in `RentalQueryController`. Pass both values into the extended `FindRentalsQuery` record.
Spring MVC's default `LocalDate` deserialization rejects any value that is not `yyyy-MM-dd` with
a `400 Bad Request` before the method body is reached.

## 2. File to Modify / Create

* **File Path:** `service/src/main/java/com/github/jenkaby/bikerental/rental/web/query/RentalQueryController.java`
* **Action:** Modify Existing File

## 3. Code Implementation

**Imports Required:**

Add:

```java
import java.time.LocalDate;
```

**Code to Add/Replace:**

* **Location:** Replace the `getRentals` method signature and body.

Replace:

```java
    public ResponseEntity<Page<RentalSummaryResponse>> getRentals(
            @Parameter(description = "Rental status filter", example = "ACTIVE") @RequestParam(name = "status", required = false) RentalStatus status,
            @Parameter(description = "Customer UUID filter") @RequestParam(name = "customerId", required = false) UUID customerId,
            @Parameter(description = "Equipment UID filter", example = "BIKE-001") @RequestParam(name = "equipmentUid", required = false) String equipmentUid,
            @PageableDefault(size = 20, sort = "expectedReturnAt", direction = Sort.Direction.ASC) Pageable pageable) {
        log.info("[GET] Get rentals with filters status={}, customerId={}, equipmentUid={}", status, customerId, equipmentUid);

        PageRequest pageRequest = pageMapper.toPageRequest(pageable);
        var query = new FindRentalsUseCase.FindRentalsQuery(status, customerId, equipmentUid, pageRequest);

        Page<Rental> rentals = findRentalsUseCase.execute(query);

        Page<RentalSummaryResponse> response = rentals.map(mapper::toRentalSummaryResponse);
        return ResponseEntity.ok(response);
    }
```

With:

```java
    public ResponseEntity<Page<RentalSummaryResponse>> getRentals(
            @Parameter(description = "Rental status filter", example = "ACTIVE") @RequestParam(name = "status", required = false) RentalStatus status,
            @Parameter(description = "Customer UUID filter") @RequestParam(name = "customerId", required = false) UUID customerId,
            @Parameter(description = "Equipment UID filter", example = "BIKE-001") @RequestParam(name = "equipmentUid", required = false) String equipmentUid,
            @Parameter(description = "Created-at range start (inclusive), format yyyy-MM-dd", example = "2026-02-15") @RequestParam(name = "from", required = false) LocalDate from,
            @Parameter(description = "Created-at range end (inclusive), format yyyy-MM-dd", example = "2026-02-20") @RequestParam(name = "to", required = false) LocalDate to,
            @PageableDefault(size = 20, sort = "expectedReturnAt", direction = Sort.Direction.ASC) Pageable pageable) {
        log.info("[GET] Get rentals with filters status={}, customerId={}, equipmentUid={}, from={}, to={}", status, customerId, equipmentUid, from, to);

        PageRequest pageRequest = pageMapper.toPageRequest(pageable);
        var query = new FindRentalsUseCase.FindRentalsQuery(status, customerId, equipmentUid, pageRequest, from, to);

        Page<Rental> rentals = findRentalsUseCase.execute(query);

        Page<RentalSummaryResponse> response = rentals.map(mapper::toRentalSummaryResponse);
        return ResponseEntity.ok(response);
    }
```

## 4. Validation Steps

skip