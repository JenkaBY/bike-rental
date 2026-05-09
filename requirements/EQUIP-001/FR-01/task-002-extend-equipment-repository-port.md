# Task 002: Extend EquipmentRepository Domain Port with searchText Parameter

> **Applied Skill:** N/A (domain port interface change — hexagonal architecture convention)

## 1. Objective

Add `searchText` as a new parameter to the `findAll()` method on the `EquipmentRepository` domain port interface
so that the infrastructure adapter can receive the free-text filter value from the application layer.

## 2. File to Modify / Create

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/equipment/domain/repository/EquipmentRepository.java`
* **Action:** Modify Existing File

## 3. Code Implementation

**Imports Required:**

None — no new imports needed.

**Code to Add/Replace:**

* **Location:** Replace the `findAll` method signature inside the `EquipmentRepository` interface.

* **Current code:**

```java
    Page<Equipment> findAll(String statusSlug, String typeSlug, PageRequest pageRequest);
```

* **Snippet (replace with):**

```java
    Page<Equipment> findAll(String statusSlug, String typeSlug, String searchText, PageRequest pageRequest);
```

## 4. Validation Steps

```bash
./gradlew :service:compileJava
```

> Compilation will fail in `SearchEquipmentsService` and `EquipmentRepositoryAdapter` because they still call
> `findAll` with the old 3-arg signature. These are intentional — fixed in the subsequent tasks.
