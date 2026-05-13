# Task 001: Add State Machine to RentalStatus Enum

> **Applied Skill:** `java.instructions.md` — immutable data structures, pattern matching;
> `springboot.instructions.md` — domain model discipline

## 1. Objective

Enrich the `RentalStatus` enum with a static, immutable allowed-transitions map and a
`validateTransitionTo(RentalStatus target)` method. This makes `RentalStatus` the single
authoritative owner of lifecycle state machine rules.

## 2. File to Modify / Create

* **File Path:** `service/src/main/java/com/github/jenkaby/bikerental/rental/domain/model/RentalStatus.java`
* **Action:** Modify Existing File

## 3. Code Implementation

**Imports Required:**

```java
import com.github.jenkaby.bikerental.rental.domain.exception.InvalidRentalStatusException;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
```

**Code to Add/Replace:**

* **Location:** Replace the entire file content with the following.

```java
package com.github.jenkaby.bikerental.rental.domain.model;

import com.github.jenkaby.bikerental.rental.domain.exception.InvalidRentalStatusException;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

public enum RentalStatus {
    DRAFT,
    ACTIVE,
    COMPLETED,
    CANCELLED,
    DEBT;

    private static final Map<RentalStatus, Set<RentalStatus>> ALLOWED_TRANSITIONS;

    static {
        var map = new EnumMap<RentalStatus, Set<RentalStatus>>(RentalStatus.class);
        map.put(DRAFT, Set.of(ACTIVE, CANCELLED));
        map.put(ACTIVE, Set.of(CANCELLED));
        map.put(COMPLETED, Set.of());
        map.put(CANCELLED, Set.of());
        map.put(DEBT, Set.of());
        ALLOWED_TRANSITIONS = Map.copyOf(map);
    }

    public void validateTransitionTo(RentalStatus target) {
        if (!ALLOWED_TRANSITIONS.getOrDefault(this, Set.of()).contains(target)) {
            throw new InvalidRentalStatusException(this, target);
        }
    }
}
```

## 4. Validation Steps

skip
