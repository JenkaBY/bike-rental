# Task 001: Create `RentalEquipmentRepository` Domain Port

> **Applied Skill:** `d:\Projects\private\bikerent\.github\skills\spring-boot-data-ddd\SKILL.md` — Domain port interface
> placed in
> `domain/repository/`; keeps `RentalRepository` focused on rental-aggregate queries by creating a dedicated port for
> rental-equipment-level queries.

## 1. Objective

Create the domain port (interface) `RentalEquipmentRepository` inside the rental module's `domain/repository/` package.
It declares the single method `findOccupiedEquipmentIds(Set<Long> candidateIds)` that returns the subset of IDs
currently in `ACTIVE` or `ASSIGNED` status. This is the contract the application layer and infrastructure adapter will
implement and consume respectively.

## 2. File to Modify / Create

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/rental/domain/repository/RentalEquipmentRepository.java`
* **Action:** Create New File

## 3. Code Implementation

**Imports Required:** None beyond `java.util.Set`.

**Code to Add/Replace:**

* **Location:** New file — entire file content below.
* **Snippet:**

```java
package com.github.jenkaby.bikerental.rental.domain.repository;

import java.util.Set;

public interface RentalEquipmentRepository {

    Set<Long> findOccupiedEquipmentIds(Set<Long> candidateIds);
}
```

## 4. Validation Steps

Execute the following commands to ensure this task was successful. Do NOT run the full application server.

```bash
./gradlew :service:compileJava
```
