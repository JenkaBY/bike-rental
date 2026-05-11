# Task 002: Add `conditionSlug` Mapping to `EquipmentQueryMapper`

> **Applied Skill:** `mapstruct-hexagonal/SKILL.md` — Explicit `@Mapping` for Enum→String field;
`unmappedTargetPolicy=ERROR` requires every target field to be covered.

## 1. Objective

Add an explicit `@Mapping` annotation to `EquipmentQueryMapper.toResponse(Equipment)` that maps
`Equipment.conditionSlug` (`Condition` enum) to `EquipmentResponse.conditionSlug` (`String`).
MapStruct supports built-in enum-to-String conversion via `Enum.name()`, but the annotation is
required to satisfy `unmappedTargetPolicy=ERROR` when the field names are the same but types differ.

## 2. File to Modify / Create

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/equipment/web/query/mapper/EquipmentQueryMapper.java`
* **Action:** Modify Existing File

## 3. Code Implementation

**Imports Required:** No new imports needed (`@Mapping` is already imported).

**Code to Add/Replace:**

* **Location:** The `toResponse` method. Current content:

```java
    @Mapping(target = "status", source = "statusSlug")
    @Mapping(target = "type", source = "typeSlug")
    EquipmentResponse toResponse(Equipment equipment);
```

Replace with:

```java
    @Mapping(target = "status", source = "statusSlug")
    @Mapping(target = "type", source = "typeSlug")
    @Mapping(target = "conditionSlug", source = "conditionSlug")
    EquipmentResponse toResponse(Equipment equipment);
```

## 4. Validation Steps

```bash
./gradlew :service:compileJava
```

Clean compile with zero MapStruct warnings confirms the mapping is correct. Then run unit tests:

```bash
./gradlew :service:test "-Dspring.profiles.active=test" --tests "com.github.jenkaby.bikerental.equipment.*"
```
