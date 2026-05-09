# Task 003: Extend EquipmentJpaRepository with JpaSpecificationExecutor and Remove findAllByFilters

> **Applied Skill:** `spring-boot-data-ddd` — Spring Data JPA repository interface conventions; removing a
> named-query method that is superseded by the Specification API.

## 1. Objective

Make `EquipmentJpaRepository` extend `JpaSpecificationExecutor<EquipmentJpaEntity>` so that the adapter can call
`findAll(Specification, Pageable)`. Remove the now-superseded custom JPQL method `findAllByFilters` and its
`@Query` annotation.

## 2. File to Modify / Create

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/equipment/infrastructure/persistence/repository/EquipmentJpaRepository.java`
* **Action:** Modify Existing File

## 3. Code Implementation

**Imports Required:**

Add the following import (all others already present):

```java
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
```

Remove the following imports that are no longer needed once `findAllByFilters` is deleted:

```java
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
```

**Code to Add/Replace:**

* **Location — interface declaration:** Replace the `extends` clause on `EquipmentJpaRepository`.

* **Current code:**

```java
public interface EquipmentJpaRepository extends JpaRepository<EquipmentJpaEntity, Long> {
```

* **Snippet (replace with):**

```java
public interface EquipmentJpaRepository extends JpaRepository<EquipmentJpaEntity, Long>,
        JpaSpecificationExecutor<EquipmentJpaEntity> {
```

---

* **Location — remove `findAllByFilters`:** Delete the entire `@Query`-annotated method at the bottom of the
  interface.

* **Current code to delete:**

```java
    @Query("SELECT e FROM EquipmentJpaEntity e WHERE " +
            "(:statusSlug IS NULL OR e.statusSlug = :statusSlug) AND " +
            "(:typeSlug IS NULL OR e.typeSlug = :typeSlug)")
    Page<EquipmentJpaEntity> findAllByFilters(@Param("statusSlug") String statusSlug,
                                              @Param("typeSlug") String typeSlug,
                                              Pageable pageRequest);
```

* **Snippet (replace with):** *(nothing — delete this block entirely)*

---

After these edits the complete file must look exactly like this:

```java
package com.github.jenkaby.bikerental.equipment.infrastructure.persistence.repository;

import com.github.jenkaby.bikerental.equipment.infrastructure.persistence.entity.EquipmentJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface EquipmentJpaRepository extends JpaRepository<EquipmentJpaEntity, Long>,
        JpaSpecificationExecutor<EquipmentJpaEntity> {

    Optional<EquipmentJpaEntity> findBySerialNumber(String serialNumber);

    Optional<EquipmentJpaEntity> findByUid(String uid);

    boolean existsBySerialNumber(String serialNumber);

    boolean existsByUid(String uid);

    List<EquipmentJpaEntity> findAllByIdIn(Collection<Long> ids);
}
```

## 4. Validation Steps

```bash
./gradlew :service:compileJava
```

> `EquipmentRepositoryAdapter` will fail to compile because its `findAll` body still calls
> `jpaRepository.findAllByFilters(...)` which no longer exists. This is expected and is resolved in task 004.
