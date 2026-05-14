# Task 003: Update RentalRepository Domain Port

> **Applied Skill:** `spring-boot-data-ddd` — domain port interfaces, one repository per aggregate;
> `java.instructions.md` — interface contracts, no framework leakage into domain

## 1. Objective

Replace the four per-combination search methods in `RentalRepository` with a single unified method
`findAll(RentalSearchFilter filter, PageRequest pageRequest)`. The `getCustomerDebtRentals` method
is **not** modified.

## 2. File to Modify / Create

* **File Path:** `service/src/main/java/com/github/jenkaby/bikerental/rental/domain/repository/RentalRepository.java`
* **Action:** Modify Existing File

## 3. Code Implementation

**Imports Required:**

Replace existing search-related imports with:

```java
import com.github.jenkaby.bikerental.rental.domain.model.RentalSearchFilter;
```

Remove these imports (no longer needed):

```java
// REMOVE: import com.github.jenkaby.bikerental.rental.domain.model.RentalStatus;
// REMOVE: import java.util.UUID;
```

> `RentalStatus` and `UUID` are still used by `getCustomerDebtRentals` via `CustomerRef` —
> check whether each import is still referenced; remove only those no longer used.

**Code to Add/Replace:**

* **Location:** Replace the four search method declarations with the single unified method.

Replace:

```java
    Page<Rental> findByStatus(RentalStatus status, PageRequest pageRequest);

    Page<Rental> findByStatusAndCustomerId(RentalStatus status, UUID customerId, PageRequest pageRequest);

    Page<Rental> findByCustomerId(UUID customerId, PageRequest pageRequest);

    Page<Rental> findByStatusAndEquipmentUid(RentalStatus status, String equipmentUid, PageRequest pageRequest);
```

With:

```java
    Page<Rental> findAll(RentalSearchFilter filter, PageRequest pageRequest);
```

The complete updated file should look like:

```java
package com.github.jenkaby.bikerental.rental.domain.repository;

import com.github.jenkaby.bikerental.rental.domain.model.Rental;
import com.github.jenkaby.bikerental.rental.domain.model.RentalSearchFilter;
import com.github.jenkaby.bikerental.rental.domain.model.RentalStatus;
import com.github.jenkaby.bikerental.shared.domain.CustomerRef;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Page;
import com.github.jenkaby.bikerental.shared.domain.model.vo.PageRequest;

import java.util.List;
import java.util.Optional;

public interface RentalRepository {

    Rental save(Rental rental);

    Optional<Rental> findById(Long id);

    boolean existsById(Long id);

    Page<Rental> findAll(RentalSearchFilter filter, PageRequest pageRequest);

    List<Rental> getCustomerDebtRentals(CustomerRef customerRef);
}
```

## 4. Validation Steps

skip