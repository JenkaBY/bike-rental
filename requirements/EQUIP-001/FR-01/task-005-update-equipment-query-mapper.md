# Task 005: Update EquipmentQueryMapper to Map searchText into SearchEquipmentsQuery

> **Applied Skill:** `mapstruct-hexagonal` — Pattern 4b (source path disambiguation for multi-parameter methods)

## 1. Objective

Extend the `toSearchQuery` method on `EquipmentQueryMapper` to accept the new `searchText` parameter and map it
directly to the `searchText` field on `SearchEquipmentsQuery`. MapStruct derives the mapping by name — no
`@Mapping` annotation is required.

## 2. File to Modify / Create

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/equipment/web/query/mapper/EquipmentQueryMapper.java`
* **Action:** Modify Existing File

## 3. Code Implementation

**Imports Required:**

None — no new imports needed.

**Code to Add/Replace:**

* **Location:** Replace the `toSearchQuery` method signature and its `@Mapping` annotations inside
  `EquipmentQueryMapper`.

* **Current code:**

```java
    @Mapping(target = "typeSlug", source = "type")
    @Mapping(target = "statusSlug", source = "status")
    @Mapping(target = "pageRequest", source = "pageable")
    SearchEquipmentsUseCase.SearchEquipmentsQuery toSearchQuery(
            String status,
            String type,
            Pageable pageable);
```

* **Snippet (replace with):**

```java
    @Mapping(target = "typeSlug", source = "type")
    @Mapping(target = "statusSlug", source = "status")
    @Mapping(target = "pageRequest", source = "pageable")
    SearchEquipmentsUseCase.SearchEquipmentsQuery toSearchQuery(
            String status,
            String type,
            String searchText,
            Pageable pageable);
```

> **How MapStruct resolves this:** When a mapping method has multiple parameters, MapStruct matches each
> source parameter to the target field by name. The new `searchText` parameter name exactly matches the
> `searchText` field on `SearchEquipmentsQuery`, so no explicit `@Mapping` is required.

## 4. Validation Steps

```bash
./gradlew :service:compileJava
```

> `EquipmentQueryMapper` will compile cleanly. The remaining compilation failure is in
> `EquipmentQueryControllerTest` (the test still constructs `SearchEquipmentsQuery` with the old 3-arg
> form and calls `mapper.toSearchQuery` with 3 args). That is fixed in task 007.
