# Task 001: Add `toResponses` List-Mapping Method to `EquipmentQueryMapper`

> **Applied Skill:** `mapstruct-hexagonal` — Pattern 1 (Simple Interface Mapper); list mapping is derived
> automatically from the single-element method; no new `@Mapper(uses = ...)` delegate required.

## 1. Objective

Add a `List<EquipmentResponse> toResponses(List<Equipment> equipments)` method to `EquipmentQueryMapper` so the
batch controller handler can convert the domain list returned by the use case into a response list in a single call.
MapStruct will auto-derive the implementation from the already-defined `toResponse(Equipment)` method.

## 2. File to Modify / Create

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/equipment/web/query/mapper/EquipmentQueryMapper.java`
* **Action:** Modify Existing File

## 3. Code Implementation

**Imports Required:**

```java
import java.util.List;
```

**Code to Add/Replace:**

* **Location:** Inside the `EquipmentQueryMapper` interface body, immediately after the existing
  `EquipmentResponse toResponse(Equipment equipment)` method declaration (line 20 in the current file).

* **Snippet:**

```java
    List<EquipmentResponse> toResponses(List<Equipment> equipments);
```

The final mapper interface should look like:

```java
@Mapper(uses = {UidMapper.class, SerialNumberMapper.class, PageMapper.class})
public interface EquipmentQueryMapper {

    @Mapping(target = "status", source = "statusSlug")
    @Mapping(target = "type", source = "typeSlug")
    @Mapping(target = "conditionNotes", source = "condition")
    @Mapping(target = "condition", source = "conditionSlug")
    EquipmentResponse toResponse(Equipment equipment);

    List<EquipmentResponse> toResponses(List<Equipment> equipments);

    @Mapping(target = "typeSlug", source = "type")
    @Mapping(target = "statusSlug", source = "status")
    @Mapping(target = "pageRequest", source = "pageable")
    SearchEquipmentsUseCase.SearchEquipmentsQuery toSearchQuery(
            String status,
            String type,
            String searchText,
            Pageable pageable);
}
```

## 4. Validation Steps

skip
