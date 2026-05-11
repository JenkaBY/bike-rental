# Task 001: Add `conditionSlug` Field to `EquipmentResponse`

> **Applied Skill:** `springboot.instructions.md` — Web-layer DTO is a record; new fields annotated with `@Schema` for
> OpenAPI documentation.

## 1. Objective

Add a `String conditionSlug` component to the `EquipmentResponse` record so that the physical
condition is visible in the REST API response for all equipment endpoints.

## 2. File to Modify / Create

* **File Path:** `service/src/main/java/com/github/jenkaby/bikerental/equipment/web/query/dto/EquipmentResponse.java`
* **Action:** Modify Existing File

## 3. Code Implementation

**Imports Required:** No new imports needed.

**Code to Add/Replace:**

* **Location:** The record component list. Current full file content:

```java
package com.github.jenkaby.bikerental.equipment.web.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

@Schema(description = "Equipment record")
public record EquipmentResponse(
        @Schema(description = "Internal ID", example = "1") @NotNull Long id,
        @Schema(description = "Serial number", example = "SN-123456") @NotNull String serialNumber,
        @Schema(description = "UID tag", example = "BIKE-001") @NotNull String uid,
        @Schema(description = "Equipment type slug", example = "bike") @NotNull String type,
        @Schema(description = "Equipment status slug", example = "available") @NotNull String status,
        @Schema(description = "Model name", example = "Trek Marlin 5") @NotNull String model,
        @Schema(description = "Commissioned date", example = "2023-06-01") LocalDate commissionedAt,
        @Schema(description = "Condition", example = "Good") String condition
) {
}
```

Replace with:

```java
package com.github.jenkaby.bikerental.equipment.web.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

@Schema(description = "Equipment record")
public record EquipmentResponse(
        @Schema(description = "Internal ID", example = "1") @NotNull Long id,
        @Schema(description = "Serial number", example = "SN-123456") @NotNull String serialNumber,
        @Schema(description = "UID tag", example = "BIKE-001") @NotNull String uid,
        @Schema(description = "Equipment type slug", example = "bike") @NotNull String type,
        @Schema(description = "Equipment status slug", example = "available") @NotNull String status,
        @Schema(description = "Model name", example = "Trek Marlin 5") @NotNull String model,
        @Schema(description = "Commissioned date", example = "2023-06-01") LocalDate commissionedAt,
        @Schema(description = "Condition notes", example = "Good") String condition,
        @Schema(description = "Physical condition slug", example = "GOOD") String conditionSlug
) {
}
```

**Note:** The existing `condition` field description is updated from `"Condition"` to
`"Condition notes"` to disambiguate the two fields in the OpenAPI docs.

## 4. Validation Steps

```bash
./gradlew :service:compileJava
```

The build will report an `unmappedTargetPolicy` error on `EquipmentQueryMapper` until task-002
is complete. That is expected — proceed sequentially.
