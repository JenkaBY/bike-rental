# Task 003: Create `RentalEquipmentRepositoryAdapter` Infrastructure Adapter

> **Applied Skill:** `d:\Projects\private\bikerent\.github\skills\spring-boot-data-ddd\SKILL.md` — Infrastructure
> adapter implementing the domain port; package-private class, consistent with `RentalRepositoryAdapter` pattern in the
> same `adapter/` package.

## 1. Objective

Create the infrastructure adapter `RentalEquipmentRepositoryAdapter` that implements the `RentalEquipmentRepository`
domain port (Task 001) by delegating to `RentalEquipmentJpaRepository` (Task 002). The adapter applies the empty-set
short-circuit: if `candidateIds` is empty it returns `Set.of()` immediately without issuing a database query.

## 2. File to Modify / Create

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/rental/infrastructure/persistence/adapter/RentalEquipmentRepositoryAdapter.java`
* **Action:** Create New File

## 3. Code Implementation

**Imports Required:** See snippet below.

**Code to Add/Replace:**

* **Location:** New file — entire file content below.
* **Snippet:**

```java
package com.github.jenkaby.bikerental.rental.infrastructure.persistence.adapter;

import com.github.jenkaby.bikerental.rental.domain.model.RentalEquipmentStatus;
import com.github.jenkaby.bikerental.rental.domain.repository.RentalEquipmentRepository;
import com.github.jenkaby.bikerental.rental.infrastructure.persistence.repository.RentalEquipmentJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Repository
@Transactional(readOnly = true)
class RentalEquipmentRepositoryAdapter implements RentalEquipmentRepository {

    private static final Set<String> OCCUPIED_STATUSES = Set.of(
            RentalEquipmentStatus.ACTIVE.name(),
            RentalEquipmentStatus.ASSIGNED.name()
    );

    private final RentalEquipmentJpaRepository repository;

    RentalEquipmentRepositoryAdapter(RentalEquipmentJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Set<Long> findOccupiedEquipmentIds(Set<Long> candidateIds) {
        if (candidateIds.isEmpty()) {
            return Set.of();
        }
        return repository.findEquipmentIdsByEquipmentIdInAndStatusIn(candidateIds, OCCUPIED_STATUSES);
    }
}
```

> **Key rules:**
> - The class is **package-private** (no `public` modifier), consistent with `RentalRepositoryAdapter`.
> - `OCCUPIED_STATUSES` is a static constant to avoid re-allocating the set on every call.
> - The empty-set guard eliminates DB round-trips when no candidates are provided (FR-02 Scenario 4).

## 4. Validation Steps

skip
