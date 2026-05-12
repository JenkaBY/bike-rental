# Task 002: Add `getEquipmentsByConditions` to `EquipmentFacade` Interface

> **Applied Skill:** `spring-boot-modulith/SKILL.md` — Facade interface is the only public cross-module entry point;
> method signature uses only public module types.

## 1. Objective

Declare the new query method on `EquipmentFacade` so downstream modules can call it. The method
accepts a set of physical conditions and an optional search filter, returning an unpaged list of
matching `EquipmentInfo` records.

## 2. File to Modify / Create

* **File Path:** `service/src/main/java/com/github/jenkaby/bikerental/equipment/EquipmentFacade.java`
* **Action:** Modify Existing File

## 3. Code Implementation

**Imports Required:**

```java
import com.github.jenkaby.bikerental.shared.domain.model.Condition;
import java.util.Set;
```

Add these imports after the existing `import java.util.List;` line.

**Code to Add/Replace:**

* **Location:** Inside the `EquipmentFacade` interface body, after the existing `findByIds` method.
  Current full file content:

```java
package com.github.jenkaby.bikerental.equipment;

import java.util.List;
import java.util.Optional;


public interface EquipmentFacade {

    Optional<EquipmentInfo> findById(Long equipmentId);

    List<EquipmentInfo> findByIds(List<Long> equipmentIds);
}
```

Replace with:

```java
package com.github.jenkaby.bikerental.equipment;

import com.github.jenkaby.bikerental.shared.domain.model.Condition;

import java.util.List;
import java.util.Optional;
import java.util.Set;


public interface EquipmentFacade {

    Optional<EquipmentInfo> findById(Long equipmentId);

    List<EquipmentInfo> findByIds(List<Long> equipmentIds);

    List<EquipmentInfo> getEquipmentsByConditions(Set<Condition> conditions, EquipmentSearchFilter filter);
}
```

## 4. Validation Steps

skip validation
