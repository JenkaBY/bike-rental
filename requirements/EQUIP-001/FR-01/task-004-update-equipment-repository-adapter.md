# Task 004: Update EquipmentRepositoryAdapter to Accept searchText Parameter

> **Applied Skill:** N/A (infrastructure adapter method signature update — parameter is received but not yet
> applied to persistence query; FR-02 will replace the query implementation entirely)

## 1. Objective

Update `EquipmentRepositoryAdapter.findAll()` to satisfy the new 4-argument `EquipmentRepository` port contract.
The `searchText` parameter is accepted but not forwarded to `findAllByFilters` (which does not support it); the full
Specification-based implementation is handled in FR-02.

## 2. File to Modify / Create

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/equipment/infrastructure/persistence/adapter/EquipmentRepositoryAdapter.java`
* **Action:** Modify Existing File

## 3. Code Implementation

**Imports Required:**

None — no new imports needed.

**Code to Add/Replace:**

* **Location:** Replace the `findAll` method inside `EquipmentRepositoryAdapter`.

* **Current code:**

```java
    @Override
    public Page<Equipment> findAll(String statusSlug, String typeSlug, PageRequest request) {
        var pageRequest = pageMapper.toSpring(request);

        org.springframework.data.domain.Page<EquipmentJpaEntity> page = jpaRepository.findAllByFilters(statusSlug, typeSlug, pageRequest);
        return pageMapper.toDomain(page)
                .map(mapper::toDomain);
    }
```

* **Snippet (replace with):**

```java
    @Override
    public Page<Equipment> findAll(String statusSlug, String typeSlug, String searchText, PageRequest request) {
        var pageRequest = pageMapper.toSpring(request);

        org.springframework.data.domain.Page<EquipmentJpaEntity> page = jpaRepository.findAllByFilters(statusSlug, typeSlug, pageRequest);
        return pageMapper.toDomain(page)
                .map(mapper::toDomain);
    }
```

> **Note:** `searchText` is intentionally not yet passed to `findAllByFilters`. FR-02 will replace the
> `findAllByFilters` call entirely with a `JpaSpecificationExecutor`-based implementation that applies all
> three filter parameters.

## 4. Validation Steps

```bash
./gradlew :service:compileJava
```

> The `EquipmentRepositoryAdapter` and the domain port are now consistent. Remaining compilation failures in
> `EquipmentQueryMapper` and `EquipmentQueryControllerTest` are expected and will be resolved in tasks 005–007.
