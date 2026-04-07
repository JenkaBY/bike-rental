# Task 003: Migrate RentalJpaEntity.status to Enum and Add getCustomerDebtRentals

> **Applied Skill:** `spring-boot-data-ddd` — JPA entities should use `@Enumerated(EnumType.STRING)` for enum fields to
> avoid brittle ordinal mappings and to eliminate the `String`↔`RentalStatus` conversion layer in the JPA mapper. Domain
> repository port is a pure interface; the JPA adapter delegates to a Spring Data repository.

## 1. Objective

Migrate `RentalJpaEntity.status` from `String` to `RentalStatus` enum with `@Enumerated(EnumType.STRING)`. Update all
affected classes: `RentalJpaRepository`, `RentalRepositoryAdapter`, `RentalJpaMapper`, and the component-test
transformer. Then add the new `getCustomerDebtRentals` query using the enum type throughout.

---

## Step A — Migrate RentalJpaEntity.status to RentalStatus

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/rental/infrastructure/persistence/entity/RentalJpaEntity.java`
* **Action:** Modify Existing File

**Imports Required:**

```java
import com.github.jenkaby.bikerental.rental.domain.model.RentalStatus;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
```

**Code to Add/Replace:**

* **Location:** Replace the `status` field declaration:

Replace:

```java
    @Column(nullable = false, length = 20)
    private String status;
```

With:

```java
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RentalStatus status;
```

---

## Step B — Update RentalJpaMapper (remove RentalStatusMapper)

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/rental/infrastructure/persistence/mapper/RentalJpaMapper.java`
* **Action:** Modify Existing File

**Code to Add/Replace:**

* **Location:** Remove `RentalStatusMapper.class` from the `@Mapper(uses = {...})` annotation. The entity and domain
  model now both carry `RentalStatus` directly; no conversion is needed.

Replace:

```java
@Mapper(uses = {MoneyMapper.class, InstantMapper.class, RentalStatusMapper.class, DurationMapper.class, RentalEquipmentJpaMapper.class})
```

With:

```java
@Mapper(uses = {MoneyMapper.class, InstantMapper.class, DurationMapper.class, RentalEquipmentJpaMapper.class})
```

Also remove the now-unused import:

```java
import com.github.jenkaby.bikerental.rental.shared.mapper.RentalStatusMapper;
```

---

## Step C — Update RentalJpaRepository (all status parameters → RentalStatus)

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/rental/infrastructure/persistence/repository/RentalJpaRepository.java`
* **Action:** Modify Existing File

**Imports Required:**

```java
import com.github.jenkaby.bikerental.rental.domain.model.RentalStatus;
import java.util.List;
```

**Code to Add/Replace:**

* **Location:** Replace the entire interface body with the updated version below, converting all `String status`
  parameters to `RentalStatus status` and adding the new derived query:

Replace:

```java
    Page<RentalJpaEntity> findByStatus(String status, Pageable pageable);

    Page<RentalJpaEntity> findByStatusAndCustomerId(String status, UUID customerId, Pageable pageable);

    Page<RentalJpaEntity> findByCustomerId(UUID customerId, Pageable pageable);

    @Query(value =
            "SELECT DISTINCT r FROM RentalJpaEntity r LEFT JOIN r.rentalEquipments re " +
                    "WHERE r.status = :status AND re.equipmentUid = :equipmentUid",
            countQuery =
                    "SELECT count(DISTINCT r) FROM RentalJpaEntity r LEFT JOIN r.rentalEquipments re " +
                            "WHERE r.status = :status AND re.equipmentUid = :equipmentUid")
    Page<RentalJpaEntity> findByStatusAndEquipmentUid(@Param("status") String status, @Param("equipmentUid") String equipmentUid, Pageable pageable);
```

With:

```java
    Page<RentalJpaEntity> findByStatus(RentalStatus status, Pageable pageable);

    Page<RentalJpaEntity> findByStatusAndCustomerId(RentalStatus status, UUID customerId, Pageable pageable);

    Page<RentalJpaEntity> findByCustomerId(UUID customerId, Pageable pageable);

    @Query(value =
            "SELECT DISTINCT r FROM RentalJpaEntity r LEFT JOIN r.rentalEquipments re " +
                    "WHERE r.status = :status AND re.equipmentUid = :equipmentUid",
            countQuery =
                    "SELECT count(DISTINCT r) FROM RentalJpaEntity r LEFT JOIN r.rentalEquipments re " +
                            "WHERE r.status = :status AND re.equipmentUid = :equipmentUid")
    Page<RentalJpaEntity> findByStatusAndEquipmentUid(@Param("status") RentalStatus status, @Param("equipmentUid") String equipmentUid, Pageable pageable);

    List<RentalJpaEntity> findAllByCustomerIdAndStatusOrderByCreatedAtAsc(UUID customerId, RentalStatus status);
```

---

## Step D — Update RentalRepositoryAdapter (remove .name() calls, add new override)

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/rental/infrastructure/persistence/adapter/RentalRepositoryAdapter.java`
* **Action:** Modify Existing File

**Imports Required:**

```java
import com.github.jenkaby.bikerental.shared.domain.CustomerRef;
import java.util.List;
```

**Code to Add/Replace:**

* **Location 1:** `findByStatus` — remove `.name()`:

Replace:

```java
        var page = repository.findByStatus(status.name(), springPageRequest);
```

With:

```java
        var page = repository.findByStatus(status, springPageRequest);
```

* **Location 2:** `findByStatusAndCustomerId` — remove `.name()`:

Replace:

```java
        var page = repository.findByStatusAndCustomerId(status.name(), customerId, springPageRequest);
```

With:

```java
        var page = repository.findByStatusAndCustomerId(status, customerId, springPageRequest);
```

* **Location 3:** `findByStatusAndEquipmentUid` — remove `.name()`:

Replace:

```java
        var page = repository.findByStatusAndEquipmentUid(status.name(), equipmentUid, springPageRequest);
```

With:

```java
        var page = repository.findByStatusAndEquipmentUid(status, equipmentUid, springPageRequest);
```

* **Location 4:** Add new override after `findByStatusAndEquipmentUid`, before the closing `}`:

```java
    @Override
    public List<Rental> getCustomerDebtRentals(CustomerRef customerRef) {
        return repository
                .findAllByCustomerIdAndStatusOrderByCreatedAtAsc(customerRef.id(), RentalStatus.DEBT)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
```

---

## Step E — Add getCustomerDebtRentals to RentalRepository Port

* **File Path:** `service/src/main/java/com/github/jenkaby/bikerental/rental/domain/repository/RentalRepository.java`
* **Action:** Modify Existing File

**Imports Required:**

```java
import com.github.jenkaby.bikerental.shared.domain.CustomerRef;
import java.util.List;
```

**Code to Add/Replace:**

* **Location:** Add after `findByStatusAndEquipmentUid`, before the closing `}`:

```java
    List<Rental> getCustomerDebtRentals(CustomerRef customerRef);
```

---

## Step F — Update RentalJpaEntityTransformer (component test)

* **File Path:**
  `component-test/src/test/java/com/github/jenkaby/bikerental/componenttest/transformer/RentalJpaEntityTransformer.java`
* **Action:** Modify Existing File

**Imports Required:**

```java
import com.github.jenkaby.bikerental.rental.domain.model.RentalStatus;
```

**Code to Add/Replace:**

* **Location:** Replace the `status` local variable and builder call:

Replace:

```java
        var status = DataTableHelper.getStringOrNull(entry, "status");
```

With:

```java
        var statusString = DataTableHelper.getStringOrNull(entry, "status");
        var status = statusString != null ? RentalStatus.valueOf(statusString) : null;
```

The `.status(status)` call in the builder remains unchanged — it now passes `RentalStatus` instead of `String`, which
matches the updated field type.

## 4. Validation Steps

```bash
./gradlew :service:compileJava
./gradlew :service:test "-Dspring.profiles.active=test" --tests BikeRentalApplicationTest
```
