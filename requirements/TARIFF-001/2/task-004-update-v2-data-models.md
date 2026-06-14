# Task 004: Update V2 data model records (Command, ItemV2, RequestDTO)

> **Applied Skill:** `java.instructions.md` — JSpecify null-safety; `springboot.instructions.md` — Bean Validation
> constraints

## 1. Objective

Align the three V2 data-transfer records with the FR-2 API contract:

1. **`EquipmentCostItemV2`** — remove the per-item `startAt` field; make `returnAt` nullable (signals an estimate).
2. **`RentalCostCalculationV2Command`** — rename `startedAt` → `startAt` and change its nullability to `@NonNull`
   (global rental start is always required).
3. **`CostCalculationV2Request`** — full rewrite: drop `actualDurationMinutes`, replace `Instant plannedStartAt` with
   `@NotNull LocalDateTime startAt`, replace per-item `Instant actualReturnAt` with `@Nullable LocalDateTime returnAt`.

## 2. Files to Modify

---

### 2a. EquipmentCostItemV2.java

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/tariff/EquipmentCostItemV2.java`
* **Action:** Modify Existing File

**Replace the entire file content with:**

```java
package com.github.jenkaby.bikerental.tariff;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.time.LocalDateTime;

public record EquipmentCostItemV2(
        @NonNull Long equipmentId,
        @NonNull String equipmentType,
        @Nullable LocalDateTime returnAt) {
}
```

---

### 2b. RentalCostCalculationV2Command.java

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/tariff/RentalCostCalculationV2Command.java`
* **Action:** Modify Existing File

**Replace the entire file content with:**

```java
package com.github.jenkaby.bikerental.tariff;

import com.github.jenkaby.bikerental.shared.domain.model.vo.DiscountPercent;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public record RentalCostCalculationV2Command(
        @NonNull List<EquipmentCostItemV2> equipments,
        @NonNull Duration plannedDuration,
        @Nullable DiscountPercent discount,
        @Nullable Long specialTariffId,
        @Nullable Money specialPrice,
        @NonNull LocalDateTime startAt
) {
}
```

---

### 2c. CostCalculationV2Request.java

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/tariff/web/query/dto/CostCalculationV2Request.java`
* **Action:** Modify Existing File

**Replace the entire file content with:**

```java
package com.github.jenkaby.bikerental.tariff.web.query.dto;

import com.github.jenkaby.bikerental.shared.web.support.PercentValue;
import com.github.jenkaby.bikerental.tariff.web.query.validation.SpecialTariffConsistency;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Request for rental cost calculation V2 (per-equipment return timestamps)")
@SpecialTariffConsistency
public record CostCalculationV2Request(
        @NotEmpty List<@NotNull @Valid EquipmentItemRequest> equipments,
        @NotNull LocalDateTime startAt,
        @NotNull @Positive Integer plannedDurationMinutes,
        @PercentValue Integer discountPercent,
        @Positive Long specialTariffId,
        @DecimalMin("0") BigDecimal specialPrice
) {
    @Schema(description = "Single equipment item for V2 cost calculation")
    public record EquipmentItemRequest(
            @NotNull @Positive Long equipmentId,
            @NotBlank String equipmentType,
            @Nullable LocalDateTime returnAt
    ) {
    }
}
```

> Note: `actualDurationMinutes` is intentionally removed — V2 uses per-item `returnAt` timestamps instead.
> The `Instant` type is replaced by `LocalDateTime` to match the rest of the system's business-time convention.

## 4. Validation Steps

skip
