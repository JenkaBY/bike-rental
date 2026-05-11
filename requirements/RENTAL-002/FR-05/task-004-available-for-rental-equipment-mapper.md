# Task 004: Create `AvailableForRentalEquipmentMapper` Application Mapper

> **Applied Skill:** `d:\Projects\private\bikerent\.github\skills\mapstruct-hexagonal\SKILL.md` — Simple interface
> mapper (Pattern 1); placed in `rental/application/mapper/`; maps `EquipmentInfo` (equipment module Facade projection)
> → `AvailableForRentalEquipment` (rental domain record). All mapped fields share the same name; no `@Mapping`
> annotations required.

## 1. Objective

Create the MapStruct interface `AvailableForRentalEquipmentMapper` in `rental/application/mapper/`. It translates the
equipment module's `EquipmentInfo` into the rental domain's `AvailableForRentalEquipment`. This mapper is consumed by
`GetAvailableForRentEquipmentsService` (Task 002) to convert the Facade result list before pagination.

## 2. File to Modify / Create

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/rental/application/mapper/AvailableForRentalEquipmentMapper.java`
* **Action:** Create New File

## 3. Code Implementation

**Imports Required:** See snippet below.

**Code to Add/Replace:**

* **Location:** New file — entire file content below.
* **Snippet:**

```java
package com.github.jenkaby.bikerental.rental.application.mapper;

import com.github.jenkaby.bikerental.equipment.EquipmentInfo;
import com.github.jenkaby.bikerental.rental.domain.model.AvailableForRentalEquipment;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper
public interface AvailableForRentalEquipmentMapper {

    AvailableForRentalEquipment toDomain(EquipmentInfo equipmentInfo);

    List<AvailableForRentalEquipment> toDomainList(List<EquipmentInfo> equipmentInfos);
}
```

> **Key rules:**
> - All target fields (`id`, `serialNumber`, `uid`, `typeSlug`, `model`) match source field names on `EquipmentInfo`
    > exactly — no `@Mapping` annotations needed, and the `Mapping` import is removed.
> - `conditionSlug`, `statusSlug` on `EquipmentInfo` have no corresponding fields on `AvailableForRentalEquipment` —
    > MapStruct will NOT fail on unmapped **source** fields; only unmapped **target** fields trigger the build error
    > (per `mapstruct.unmappedTargetPolicy=ERROR`).
> - `toDomainList` is derived automatically from `toDomain`.

## 4. Validation Steps

Execute the following commands to ensure this task was successful. Do NOT run the full application server.

```bash
./gradlew :service:compileJava
```
