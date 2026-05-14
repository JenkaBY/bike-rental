# Task 007: Rewrite FindRentalsService to Use Unified RentalSearchFilter

> **Applied Skill:** `springboot.instructions.md` — thin service, delegate to repository;
> `java.instructions.md` — no conditional branching that encodes query dispatch

## 1. Objective

Replace the four-branch dispatch logic in `FindRentalsService.execute()` with a single call to
`RentalRepository.findAll(RentalSearchFilter, PageRequest)`. The service constructs
`RentalSearchFilter` directly from the query fields — date-to-Instant conversion is delegated
entirely to `RentalSearchFilter.toMap()`.

## 2. File to Modify / Create

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/rental/application/service/FindRentalsService.java`
* **Action:** Modify Existing File

## 3. Code Implementation

**Imports Required:**

Add:

```java
import com.github.jenkaby.bikerental.rental.domain.model.RentalSearchFilter;
```

Remove (no longer needed):

```java
// REMOVE: import com.github.jenkaby.bikerental.rental.domain.model.RentalStatus;  (if unused)
```

**Code to Add/Replace:**

* **Location:** Replace the entire `execute` method body.

Replace:

```java
    @Override
    public Page<Rental> execute(FindRentalsQuery query) {
        // Priority: equipmentUid + status > customerId + status > customerId > status
        if (query.equipmentUid() != null && query.status() != null) {
            return repository.findByStatusAndEquipmentUid(query.status(), query.equipmentUid(), query.pageRequest());
        } else if (query.customerId() != null && query.status() != null) {
            return repository.findByStatusAndCustomerId(query.status(), query.customerId(), query.pageRequest());
        } else if (query.customerId() != null) {
            return repository.findByCustomerId(query.customerId(), query.pageRequest());
        } else {
            return repository.findByStatus(query.status(), query.pageRequest());
        }
    }
```

With:

```java
    @Override
    public Page<Rental> execute(FindRentalsQuery query) {
        var filter = new RentalSearchFilter(
                query.status(),
                query.customerId(),
                query.equipmentUid(),
                query.from(),
                query.to()
        );
        return repository.findAll(filter, query.pageRequest());
    }
```

## 4. Validation Steps

skip
