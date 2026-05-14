# Task 004: Update RentalJpaRepository — Add JpaSpecificationExecutor, Remove Per-Combo Methods

> **Applied Skill:** `spring-boot-data-ddd` — Spring Data JPA repositories, JpaSpecificationExecutor;
> `java.instructions.md` — interface design

## 1. Objective

Update `RentalJpaRepository` to extend `JpaSpecificationExecutor<RentalJpaEntity>`, which enables
the `findAll(Specification<RentalJpaEntity>, Pageable)` method required by the adapter. Remove the
four per-combination derived query methods that are replaced by the Specification-based approach.
The `findAllByCustomerIdAndStatusOrderByCreatedAtAsc` method used by `getCustomerDebtRentals`
is **not** modified.

## 2. File to Modify / Create

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/rental/infrastructure/persistence/repository/RentalJpaRepository.java`
* **Action:** Modify Existing File

## 3. Code Implementation

**Imports Required:**

Add:

```java
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
```

Remove these imports (no longer needed after removing the 4 query methods):

```java
// REMOVE: import org.springframework.data.jpa.repository.Query;
// REMOVE: import org.springframework.data.repository.query.Param;
```

> Verify that `@Query` and `@Param` are no longer referenced before removing their imports.

**Code to Add/Replace:**

* **Location:** Replace the entire file content.

```java
package com.github.jenkaby.bikerental.rental.infrastructure.persistence.repository;

import com.github.jenkaby.bikerental.rental.domain.model.RentalStatus;
import com.github.jenkaby.bikerental.rental.infrastructure.persistence.entity.RentalJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.UUID;

public interface RentalJpaRepository extends JpaRepository<RentalJpaEntity, Long>,
        JpaSpecificationExecutor<RentalJpaEntity> {

    List<RentalJpaEntity> findAllByCustomerIdAndStatusOrderByCreatedAtAsc(UUID customerId, RentalStatus status);
}
```

## 4. Validation Steps

skip
