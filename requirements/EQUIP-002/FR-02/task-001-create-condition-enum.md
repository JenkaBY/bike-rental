# Task 001: Create `Condition` Enum in `shared` Module

> **Applied Skill:** `java.instructions.md` — Enum declaration, no-framework domain type in shared kernel.

## 1. Objective

Create the `Condition` enum in the `shared` module so it is accessible to both the `equipment` and
`rental` modules without creating a circular dependency.

## 2. File to Modify / Create

* **File Path:** `service/src/main/java/com/github/jenkaby/bikerental/shared/domain/model/Condition.java`
* **Action:** Create New File

## 3. Code Implementation

**Imports Required:** None

**Code to Add/Replace:**

* **Location:** New file — full content below.

```java
package com.github.jenkaby.bikerental.shared.domain.model;

public enum Condition {
    GOOD,
    MAINTENANCE,
    BROKEN,
    DECOMMISSIONED
}
```

## 4. Validation Steps

```bash
./gradlew :service:compileJava
```
