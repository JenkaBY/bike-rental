# Task 001: Create EquipmentSpecConstant Utility Class

> **Applied Skill:** N/A (utility class — mirrors `SpecConstant` pattern from
> `customer/infrastructure/persistence/specification/SpecConstant.java`)

## 1. Objective

Create a `@UtilityClass` that holds all string constants used in `@Spec` path and params attributes for the equipment
specification. Centralising these strings prevents typos and makes future additions to the spec consistent.

## 2. File to Modify / Create

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/equipment/infrastructure/persistence/specification/EquipmentSpecConstant.java`
* **Action:** Create New File

## 3. Code Implementation

**Imports Required:**

```java
import lombok.experimental.UtilityClass;
```

**Code to Add/Replace:**

* **Location:** New file — full content below.

* **Snippet:**
```java
package com.github.jenkaby.bikerental.equipment.infrastructure.persistence.specification;

import lombok.experimental.UtilityClass;

@UtilityClass
public class EquipmentSpecConstant {

    public static final String STATUS = "status";
    public static final String TYPE = "type";
    public static final String SEARCH = "q";

    public static final String STATUS_SLUG = "statusSlug";
    public static final String TYPE_SLUG = "typeSlug";
    public static final String UID = "uid";
    public static final String SERIAL_NUMBER = "serialNumber";
    public static final String MODEL = "model";
}
```

## 4. Validation Steps

```bash
./gradlew :service:compileJava
```

> New file compiles cleanly in isolation; no other file references it yet.
