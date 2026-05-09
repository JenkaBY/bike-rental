# Task 004: Rewrite EquipmentRepositoryAdapter.findAll() to Use SpecificationBuilder

> **Applied Skill:** N/A (infrastructure adapter — mirrors `TransactionRepositoryAdapter.findTransactionHistory()`
> at `finance/infrastructure/persistence/adapter/TransactionRepositoryAdapter.java` as the canonical
> `SpecificationBuilder` pattern in this codebase)

## 1. Objective

Replace the stub `findAllByFilters` call in `EquipmentRepositoryAdapter.findAll()` with a full
`SpecificationBuilder`-based implementation that builds an `EquipmentSpec` from all three filter params
(`status`, `type`, `q`) and delegates to the `JpaSpecificationExecutor.findAll(spec, pageable)` method. This
completes the FR-02 infrastructure migration.

## 2. File to Modify / Create

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/equipment/infrastructure/persistence/adapter/EquipmentRepositoryAdapter.java`
* **Action:** Modify Existing File

## 3. Code Implementation

**Imports Required:**

Add the following two imports (position them after the existing Spring/Java imports):

```java
import com.github.jenkaby.bikerental.equipment.infrastructure.persistence.specification.EquipmentSpec;
import com.github.jenkaby.bikerental.equipment.infrastructure.persistence.specification.EquipmentSpecConstant;
import net.kaczmarzyk.spring.data.jpa.utils.SpecificationBuilder;
```

**Code to Add/Replace:**

* **Location:** Replace the entire `findAll` method inside `EquipmentRepositoryAdapter`.

* **Current code (as left by FR-01 task 004):**
```java
    @Override
    public Page<Equipment> findAll(String statusSlug, String typeSlug, String searchText, PageRequest request) {
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

        var spec = SpecificationBuilder.specification(EquipmentSpec.class)
                .withParam(EquipmentSpecConstant.STATUS, statusSlug)
                .withParam(EquipmentSpecConstant.TYPE, typeSlug)
                .withParam(EquipmentSpecConstant.SEARCH, searchText)
                .build();

        org.springframework.data.domain.Page<EquipmentJpaEntity> page = jpaRepository.findAll(spec, pageRequest);
        return pageMapper.toDomain(page)
                .map(mapper::toDomain);
    }
```

## 4. Validation Steps

```bash
./gradlew :service:test "-Dspring.profiles.active=test" --tests EquipmentQueryControllerTest
```

> All controller unit tests must pass. The full `service` module must also compile with zero errors:
>
> ```bash
> ./gradlew :service:compileJava
> ```
