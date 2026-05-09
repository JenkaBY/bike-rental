# Task 001: Extend SearchEquipmentsQuery with searchText Field

> **Applied Skill:** N/A (plain Java record field addition — no framework pattern required)

## 1. Objective

Add a new nullable `searchText` field to the `SearchEquipmentsQuery` inner record inside `SearchEquipmentsUseCase`
so that the free-text `q` parameter can be carried from the web layer into the application layer.

## 2. File to Modify / Create

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/equipment/application/usecase/SearchEquipmentsUseCase.java`
* **Action:** Modify Existing File

## 3. Code Implementation

**Imports Required:**

None — no new imports needed.

**Code to Add/Replace:**

* **Location:** Replace the entire `SearchEquipmentsQuery` inner record inside `SearchEquipmentsUseCase`.

* **Current code:**

```java
    record SearchEquipmentsQuery(
            String statusSlug,
            String typeSlug,
            PageRequest pageRequest
    ) {
    }
```

* **Snippet (replace with):**

```java
    record SearchEquipmentsQuery(
            String statusSlug,
            String typeSlug,
            String searchText,
            PageRequest pageRequest
    ) {
    }
```

## 4. Validation Steps

```bash
./gradlew :service:compileJava
```

> Compilation will fail with "method does not match" errors in `SearchEquipmentsService`, `EquipmentQueryMapper`,
> and `EquipmentQueryControllerTest` because they still construct `SearchEquipmentsQuery` with the old 3-arg
> signature. These are intentional — they will be fixed in the subsequent tasks.
