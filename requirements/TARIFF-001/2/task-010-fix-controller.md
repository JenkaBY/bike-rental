# Task 010: Fix TariffV2CalculationController — URL typo and V2 facade wiring

> **Applied Skill:** `springboot.instructions.md` — @RestController pattern; `java.instructions.md` — no dead code

## 1. Objective

Fix two bugs in the existing `PUT` handler:

1. **URL typo**: `/calucations` → `/calculations`
2. **Wrong facade call**: `calculateRentalCost(command)` (V1) → `calculateRentalCostV2(command)` (V2)
3. **Wrong mapper call**: `requestMapper.toCommand(request)` (maps to V1 command) → `requestMapper.toV2Command(request)`

## 2. File to Modify

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/tariff/web/query/TariffV2CalculationController.java`
* **Action:** Modify Existing File

**Code to Replace:**

* **Location:** The entire `costCalculations` handler method.
* **Remove:**

```java
    @PutMapping("/calucations")
    @Operation(summary = "Calculate rental cost for multiple equipment items",
            description = "Supports normal mode (auto-select tariffs, apply discount) and SPECIAL mode (fixed group price)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cost calculation result",
                    content = @Content(schema = @Schema(implementation = CostCalculationResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "No suitable tariff found for an equipment type",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<CostCalculationResponse> costCalculations(@Valid @RequestBody CostCalculationV2Request request) {
        log.info("[PUT] Batch cost calculation for {} equipment item(s)", request.equipments().size());
        var command = requestMapper.toCommand(request);
        var result = tariffV2Facade.calculateRentalCost(command);
        return ResponseEntity.ok(requestMapper.toResponse(result));
    }
```

* **Replace with:**

```java
    @PutMapping("/calculations")
    @Operation(summary = "Calculate V2 rental cost with per-equipment return timestamps",
            description = "Supports normal mode (per-equipment billing from global startAt) and SPECIAL mode (fixed group price)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cost calculation result",
                    content = @Content(schema = @Schema(implementation = CostCalculationResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "No suitable tariff found for an equipment type",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<CostCalculationResponse> costCalculations(@Valid @RequestBody CostCalculationV2Request request) {
        log.info("[PUT] V2 batch cost calculation for {} equipment item(s)", request.equipments().size());
        var command = requestMapper.toV2Command(request);
        var result = tariffV2Facade.calculateRentalCostV2(command);
        return ResponseEntity.ok(requestMapper.toResponse(result));
    }
```

## 4. Validation Steps

skip