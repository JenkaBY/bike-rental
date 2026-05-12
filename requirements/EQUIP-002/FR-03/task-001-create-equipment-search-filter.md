# Task 001: Create `EquipmentSearchFilter` Record

> **Applied Skill:** `spring-boot-modulith/SKILL.md` — Public API types must live in the module root package to be
> accessible across module boundaries.

## 1. Objective

Create the `EquipmentSearchFilter` record in the equipment module's public API package (same package
as `EquipmentFacade` and `EquipmentInfo`). This is the input type for the new
`getEquipmentsByConditions` Facade method, allowing callers to pass an optional free-text search
token `q` that is matched case-insensitively and partially against uid, model, and serialNumber.

## 2. File to Modify / Create

* **File Path:** `service/src/main/java/com/github/jenkaby/bikerental/equipment/EquipmentSearchFilter.java`
* **Action:** Create New File

## 3. Code Implementation

**Imports Required:** None

**Code to Add/Replace:**

* **Location:** New file — full content below.

```java
package com.github.jenkaby.bikerental.equipment;

public record EquipmentSearchFilter(String q) {

    public static EquipmentSearchFilter empty() {
        return new EquipmentSearchFilter(null);
    }
}
```

## 4. Validation Steps

```bash
./gradlew :service:compileJava
```
