# Task 003: Create `AvailableEquipmentResponse` DTO and `RentalAvailabilityQueryMapper`

> **Applied Skill:** `d:\Projects\private\bikerent\.github\skills\mapstruct-hexagonal\SKILL.md` — Pattern 1 (simple
> interface mapper); placed in `rental/web/query/mapper/`; maps `AvailableForRentalEquipment` (domain) →
> `AvailableEquipmentResponse` (web DTO). Response record annotated with SpringDoc `@Schema` consistent with
> `EquipmentItemResponse`.

## 1. Objective

Create the read-only web response record `AvailableEquipmentResponse` in `rental/web/query/dto/` and the MapStruct
interface `RentalAvailabilityQueryMapper` in `rental/web/query/mapper/` that produces it from
`AvailableForRentalEquipment`.

## 2. File to Modify / Create

### 2a — Response DTO

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/rental/web/query/dto/AvailableEquipmentResponse.java`
* **Action:** Create New File

```java
package com.github.jenkaby.bikerental.rental.web.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Equipment available for a new rental")
public record AvailableEquipmentResponse(
        @Schema(description = "Equipment ID", example = "1") Long id,
        @Schema(description = "Equipment UID", example = "BIKE-001") String uid,
        @Schema(description = "Serial number", example = "SN-ABC-001") String serialNumber,
        @Schema(description = "Equipment type slug", example = "mountain-bike") String typeSlug,
        @Schema(description = "Model name", example = "Trek Marlin 5") String model
) {
}
```

### 2b — Web-Layer Mapper

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/rental/web/query/mapper/RentalAvailabilityQueryMapper.java`
* **Action:** Create New File

```java
package com.github.jenkaby.bikerental.rental.web.query.mapper;

import com.github.jenkaby.bikerental.rental.domain.model.AvailableForRentalEquipment;
import com.github.jenkaby.bikerental.rental.web.query.dto.AvailableEquipmentResponse;
import org.mapstruct.Mapper;

@Mapper
public interface RentalAvailabilityQueryMapper {

    AvailableEquipmentResponse toResponse(AvailableForRentalEquipment equipment);
}
```

> **Key rules:**
> - `condition` is absent from both `AvailableForRentalEquipment` and `AvailableEquipmentResponse` — all available
    > equipment is implicitly GOOD condition.
> - All remaining fields match by name exactly — no `@Mapping` annotations required.
> - No `uses = {...}` clause required — all field types are plain `String` / `Long` with no custom converters.
> - The mapper is in `web/query/mapper/` (not `application/mapper/`) because its sole concern is producing an HTTP
    > response DTO from a domain object.

## 4. Validation Steps

Execute the following commands to ensure this task was successful. Do NOT run the full application server.

```bash
./gradlew :service:compileJava
```
