# Task 002: Create `RentalEquipmentJpaRepository` Spring Data Repository

> **Applied Skill:** `d:\Projects\private\bikerent\.github\skills\spring-boot-data-ddd\SKILL.md` — Spring Data JPA
> repository with `@Query` annotation; placed in `infrastructure/persistence/repository/` consistent with
> `RentalJpaRepository` in the same package.

## 1. Objective

Create a Spring Data JPA repository `RentalEquipmentJpaRepository` that queries the `rental_equipments` table for
equipment IDs whose `status` column contains `'ACTIVE'` or `'ASSIGNED'` and whose `equipment_id` is within the
provided candidate set. Note: `RentalEquipmentJpaEntity.status` is a `String` column — status values are passed as
strings, not enum values.

## 2. File to Modify / Create

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/rental/infrastructure/persistence/repository/RentalEquipmentJpaRepository.java`
* **Action:** Create New File

## 3. Code Implementation

**Imports Required:** See snippet below.

**Code to Add/Replace:**

* **Location:** New file — entire file content below.
* **Snippet:**

```java
package com.github.jenkaby.bikerental.rental.infrastructure.persistence.repository;

import com.github.jenkaby.bikerental.rental.infrastructure.persistence.entity.RentalEquipmentJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Set;

public interface RentalEquipmentJpaRepository extends JpaRepository<RentalEquipmentJpaEntity, Long> {

    @Query("SELECT re.equipmentId FROM RentalEquipmentJpaEntity re " +
            "WHERE re.equipmentId IN :candidateIds AND re.status IN :statuses")
    Set<Long> findEquipmentIdsByEquipmentIdInAndStatusIn(
            @Param("candidateIds") Set<Long> candidateIds,
            @Param("statuses") Set<String> statuses);
}
```

> **Key rule:** `RentalEquipmentJpaEntity.status` is stored as a `String` (not an `@Enumerated` column), so the query
> uses `Set<String>` for statuses, not the `RentalEquipmentStatus` enum. The adapter (Task 003) provides the correct
> string values by calling `RentalEquipmentStatus.ACTIVE.name()` and `RentalEquipmentStatus.ASSIGNED.name()`.

## 4. Validation Steps

Execute the following commands to ensure this task was successful. Do NOT run the full application server.

```bash
./gradlew :service:compileJava
```
