# Task 003: Update SearchEquipmentsService to Forward searchText to Repository

> **Applied Skill:** N/A (thin application service — no mapping or business logic, pure delegation)

## 1. Objective

Update `SearchEquipmentsService` to forward the `searchText` field from `SearchEquipmentsQuery` to the
`EquipmentRepository.findAll()` call so that the value flows from the application layer into the infrastructure adapter.

## 2. File to Modify / Create

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/equipment/application/service/SearchEquipmentsService.java`
* **Action:** Modify Existing File

## 3. Code Implementation

**Imports Required:**

None — no new imports needed.

**Code to Add/Replace:**

* **Location:** Replace the `execute` method body inside `SearchEquipmentsService`.

* **Current code:**

```java
    @Override
    public Page<Equipment> execute(SearchEquipmentsQuery query) {
        PageRequest pageRequest = query.pageRequest();

        return repository.findAll(query.statusSlug(), query.typeSlug(), pageRequest);
    }
```

* **Snippet (replace with):**

```java
    @Override
    public Page<Equipment> execute(SearchEquipmentsQuery query) {
        PageRequest pageRequest = query.pageRequest();

        return repository.findAll(query.statusSlug(), query.typeSlug(), query.searchText(), pageRequest);
    }
```

## 4. Validation Steps

```bash
./gradlew :service:compileJava
```

> `SearchEquipmentsService` will now compile cleanly. Remaining compilation failures in
> `EquipmentRepositoryAdapter`, `EquipmentQueryMapper`, and `EquipmentQueryControllerTest` are expected and
> will be resolved in subsequent tasks.
