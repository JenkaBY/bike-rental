# Task 002: Add `conditionSlug` Field to `EquipmentJpaEntity`

> **Applied Skill:** `spring-boot-data-ddd/SKILL.md` — `@Enumerated(EnumType.STRING)` mapping for enum columns.

## 1. Objective

Map the new `condition_slug` database column to the `EquipmentJpaEntity` using `@Enumerated(EnumType.STRING)`
so Hibernate can read and write the physical condition of equipment.

**Note:** The entity already has a `private String condition` field that maps to an existing `condition TEXT`
column (a free-text notes field). That field is unchanged. The new `conditionSlug` field is a separate mapping
to the new `condition_slug` column.

## 2. File to Modify / Create

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/equipment/infrastructure/persistence/entity/EquipmentJpaEntity.java`
* **Action:** Modify Existing File

## 3. Code Implementation

**Imports Required:**

```java
import com.github.jenkaby.bikerental.shared.domain.model.Condition;
```

Add this import after the existing `import java.time.LocalDate;` line.

**Code to Add/Replace:**

* **Location:** Inside the class body, immediately after the existing `statusSlug` field declaration
  and before the `model` field declaration. Current code to find the insertion point:

```java
    @Column(name = "status_slug", nullable = false, length = 50)
    private String statusSlug;

    @Column(length = 200)
    private String model;
```

Replace with:

```java
    @Column(name = "status_slug", nullable = false, length = 50)
    private String statusSlug;

    @Enumerated(EnumType.STRING)
    @Column(name = "condition_slug", nullable = false, length = 50)
    private Condition conditionSlug;

    @Column(length = 200)
    private String model;
```

## 4. Validation Steps

```bash
./gradlew :service:compileJava
```
