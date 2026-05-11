# Task 004: Implement `findByConditions` in `EquipmentRepositoryAdapter`

> **Applied Skill:** `spring-boot-data-ddd/SKILL.md` — `JpaSpecificationExecutor.findAll(Specification)` for
> programmatic predicate composition; `@Transactional(readOnly = true)` on read methods.

## 1. Objective

Implement the `findByConditions` method in `EquipmentRepositoryAdapter`. It builds a JPA
`Specification` that applies `condition_slug IN (conditions)` as a mandatory predicate and
optionally ANDs the existing `EquipmentSpec` text-search predicate when `searchText` is non-null.

## 2. File to Modify / Create

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/equipment/infrastructure/persistence/adapter/EquipmentRepositoryAdapter.java`
* **Action:** Modify Existing File

## 3. Code Implementation

**Imports Required:**

```java
import com.github.jenkaby.bikerental.shared.domain.model.Condition;
import org.springframework.data.jpa.domain.Specification;
import java.util.Set;
```

Add these three imports after the existing `import org.springframework.util.CollectionUtils;` line.
(`Specification` and `Set` may already be present; add only those that are missing.)

**Code to Add/Replace:**

* **Location:** Inside the `EquipmentRepositoryAdapter` class body, after the closing `}` of the
  existing `findAll(String statusSlug, ...)` method and before the `existsBySerialNumber` method.
  Find this exact block to locate the insertion point:

```java
    @Override
    public boolean existsBySerialNumber(SerialNumber serialNumber) {
        return jpaRepository.existsBySerialNumber(serialNumber.value());
```

Add the following **new method** immediately before that `existsBySerialNumber` block:

```java
    @Override
    @Transactional(readOnly = true)
    public List<Equipment> findByConditions(Set<Condition> conditions, String searchText) {
        if (conditions.isEmpty()) {
            throw new IllegalArgumentException("conditions must not be empty");
        }
        Specification<EquipmentJpaEntity> spec =
                (root, query, cb) -> root.get("conditionSlug").in(conditions);
        if (searchText != null) {
            var textSpec = SpecificationBuilder.specification(EquipmentSpec.class)
                    .withParam(EquipmentSpecConstant.SEARCH, searchText)
                    .build();
            spec = spec.and(textSpec);
        }
        return jpaRepository.findAll(spec).stream()
                .map(mapper::toDomain)
                .toList();
    }

```

## 4. Validation Steps

```bash
./gradlew :service:compileJava
```
