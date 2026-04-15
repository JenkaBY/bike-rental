# Task 004: Remove `CostBreakdown` from `RentalReturnResponse`

> **Applied Skill:** `spring-boot-data-ddd` — API response records must not expose internal cost-calculation details;
> the `Rental` object is the sole payload for return operations.

## 1. Objective

`RentalReturnResponse` currently carries `List<CostBreakdown> costs` (per-item V1 breakdown). Remove both the `costs`
field and the nested `CostBreakdown` record. The response retains `RentalResponse rental` and
`@Nullable SettlementResponse settlement`.

This is a **breaking API change** — any consumers relying on the `costs` array must switch to
`POST /api/v2/tariffs/calculation` for itemised details.

## 2. File to Modify / Create

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/rental/web/command/dto/RentalReturnResponse.java`
* **Action:** Modify Existing File

## 3. Code Implementation

**Replace the entire file contents** with:

```java
package com.github.jenkaby.bikerental.rental.web.command.dto;

import com.github.jenkaby.bikerental.rental.web.query.dto.RentalResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import org.jspecify.annotations.Nullable;

@Schema(description = "Result of equipment return operation")
public record RentalReturnResponse(
        @Schema(description = "Updated rental details") RentalResponse rental,
        @Schema(description = "Settlement transaction related to rental return") @Nullable SettlementResponse settlement
) {
}
```

* **Diff summary:** Remove the `import java.math.BigDecimal;`, `import java.util.List;`, and the entire
  `CostBreakdown` nested record. Remove the `List<CostBreakdown> costs` component from the outer record.

## 4. Validation Steps

```bash
./gradlew :service:compileJava
```
