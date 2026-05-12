# Task 004: Add `conditionSlug` Field to `EquipmentInfo` Record

> **Applied Skill:** `mapstruct-hexagonal/SKILL.md` — Facade DTO (record) must include all mapped fields;
`unmappedTargetPolicy=ERROR` enforces completeness.

## 1. Objective

Add a `Condition conditionSlug` component to the `EquipmentInfo` public record so the physical
condition is visible to all consumers of `EquipmentFacade` (including the `rental` module).

After this task, `EquipmentToInfoMapper` (which maps `Equipment → EquipmentInfo`) will auto-generate
the `conditionSlug` mapping without any changes to the mapper interface — MapStruct maps `conditionSlug`
on `Equipment` to `conditionSlug` on `EquipmentInfo` by name since both are `Condition`.

## 2. File to Modify / Create

* **File Path:** `service/src/main/java/com/github/jenkaby/bikerental/equipment/EquipmentInfo.java`
* **Action:** Modify Existing File

## 3. Code Implementation

**Imports Required:**

```java
import com.github.jenkaby.bikerental.shared.domain.model.Condition;
```

Add this import after the `package` declaration.

**Code to Add/Replace:**

* **Location:** The record component list. Current full file content:

```java
package com.github.jenkaby.bikerental.equipment;


public record EquipmentInfo(
        Long id,
        String serialNumber,
        String uid,
        String typeSlug,
        String statusSlug,
        String model
) {

    public boolean isAvailable() {
        return "AVAILABLE".equals(statusSlug);
    }
}
```

Replace with:

```java
package com.github.jenkaby.bikerental.equipment;

import com.github.jenkaby.bikerental.shared.domain.model.Condition;

public record EquipmentInfo(
        Long id,
        String serialNumber,
        String uid,
        String typeSlug,
        String statusSlug,
        String model,
        Condition conditionSlug
) {

    public boolean isAvailable() {
        return "AVAILABLE".equals(statusSlug);
    }
}
```

## 4. Validation Steps

```bash
./gradlew :service:compileJava
```

This triggers MapStruct annotation processing. A clean compile confirms that:

- `EquipmentJpaMapper.toDomain` maps `conditionSlug` from entity to domain.
- `EquipmentJpaMapper.toEntity` maps `conditionSlug` from domain to entity.
- `EquipmentToInfoMapper.toEquipmentInfo` maps `conditionSlug` from domain to `EquipmentInfo`.

No build warnings or `unmappedTargetPolicy` errors should appear.
