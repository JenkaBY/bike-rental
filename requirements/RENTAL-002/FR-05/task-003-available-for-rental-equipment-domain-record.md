# Task 003: Create `AvailableForRentalEquipment` Domain Record

> **Applied Skill:** `d:\Projects\private\bikerent\.github\instructions\java.instructions.md` — Pure domain record
> in `rental/domain/model/`; no framework imports.

## 1. Objective

Create the rental-module domain record `AvailableForRentalEquipment` in `rental/domain/model/`. It carries the
identifying and descriptive fields of an equipment item that is confirmed to be in GOOD physical condition and not
currently occupied. `condition` is intentionally omitted — the record's existence already implies GOOD condition.

## 2. File to Modify / Create

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/rental/domain/model/AvailableForRentalEquipment.java`
* **Action:** Create New File

## 3. Code Implementation

**Imports Required:** See snippet below.

**Code to Add/Replace:**

* **Location:** New file — entire file content below.
* **Snippet:**

```java
package com.github.jenkaby.bikerental.rental.domain.model;

public record AvailableForRentalEquipment(
        Long id,
        String serialNumber,
        String uid,
        String typeSlug,
        String model
) {
}
```

> **Key rules:**
> - `condition` is deliberately omitted — the record's existence already implies GOOD condition (the service only
    > includes equipment that passed the `Condition.GOOD` filter).
> - `statusSlug` is also omitted — available equipment is operationally free by definition.
> - No imports required — no shared types referenced.
> - No framework annotations (`@Entity`, `@Component`, etc.) — pure domain record.

## 4. Validation Steps

Execute the following commands to ensure this task was successful. Do NOT run the full application server.

```bash
./gradlew :service:compileJava
```
