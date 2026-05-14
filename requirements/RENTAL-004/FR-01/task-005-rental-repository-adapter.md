# Task 005: Update RentalRepositoryAdapter — Unified findAll via SpecificationBuilder

> **Applied Skill:** `spring-boot-data-ddd` — adapter pattern, repository implementation;
> `java.instructions.md` — consistent with TransactionRepositoryAdapter pattern

## 1. Objective

Replace the four per-combination delegating methods in `RentalRepositoryAdapter` with a single
`findAll(RentalSearchFilter, PageRequest)` implementation that uses `SpecificationBuilder` to build
a `RentalSpec` dynamically, then delegates to `jpaRepository.findAll(spec, pageable)`. The
`getCustomerDebtRentals` method is **not** modified.

## 2. File to Modify / Create

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/rental/infrastructure/persistence/adapter/RentalRepositoryAdapter.java`
* **Action:** Modify Existing File

## 3. Code Implementation

**Imports Required:**

Add:

```java
import com.github.jenkaby.bikerental.rental.domain.model.RentalSearchFilter;
import com.github.jenkaby.bikerental.rental.infrastructure.persistence.specification.RentalSpec;
import net.kaczmarzyk.spring.data.jpa.utils.SpecificationBuilder;
```

Remove (no longer referenced):

```java
// REMOVE: import com.github.jenkaby.bikerental.rental.domain.model.RentalStatus;
// REMOVE: import java.util.UUID;
```

> Verify these are not referenced elsewhere in the file before removing.

**Code to Add/Replace:**

* **Location:** Remove the four existing `findByStatus`, `findByStatusAndCustomerId`,
  `findByCustomerId`, and `findByStatusAndEquipmentUid` methods. Replace them with the single
  unified `findAll` method shown below.

Remove these four methods entirely:

```java
    @Override
    public Page<Rental> findByStatus(RentalStatus status, PageRequest pageRequest) {
        var springPageRequest = pageMapper.toSpring(pageRequest);
        var page = repository.findByStatus(status, springPageRequest);
        return pageMapper.toDomain(page)
                .map(mapper::toDomain);
    }

    @Override
    public Page<Rental> findByStatusAndCustomerId(RentalStatus status, UUID customerId, PageRequest pageRequest) {
        var springPageRequest = pageMapper.toSpring(pageRequest);
        var page = repository.findByStatusAndCustomerId(status, customerId, springPageRequest);
        return pageMapper.toDomain(page)
                .map(mapper::toDomain);
    }

    @Override
    public Page<Rental> findByCustomerId(UUID customerId, PageRequest pageRequest) {
        var springPageRequest = pageMapper.toSpring(pageRequest);
        var page = repository.findByCustomerId(customerId, springPageRequest);
        return pageMapper.toDomain(page)
                .map(mapper::toDomain);
    }

    @Override
    public Page<Rental> findByStatusAndEquipmentUid(RentalStatus status, String equipmentUid, PageRequest pageRequest) {
        var springPageRequest = pageMapper.toSpring(pageRequest);
        var page = repository.findByStatusAndEquipmentUid(status, equipmentUid, springPageRequest);
        return pageMapper.toDomain(page)
                .map(mapper::toDomain);
    }
```

Add the following method in their place (insert before the `getCustomerDebtRentals` method):

```java
    @Override
    public Page<Rental> findAll(RentalSearchFilter filter, PageRequest pageRequest) {
        var pageable = pageMapper.toSpring(pageRequest);
        var specBuilder = SpecificationBuilder.specification(RentalSpec.class);
        filter.toMap().forEach(specBuilder::withParam);
        var spec = specBuilder.build();
        var page = repository.findAll(spec, pageable);
        return pageMapper.toDomain(page)
                .map(mapper::toDomain);
    }
```

## 4. Validation Steps

skip
