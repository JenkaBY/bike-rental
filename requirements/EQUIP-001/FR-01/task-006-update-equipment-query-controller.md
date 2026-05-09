# Task 006: Update EquipmentQueryController to Accept q Query Parameter

> **Applied Skill:** N/A (controller parameter addition with OpenAPI annotation — standard Spring MVC pattern)

## 1. Objective

Add the optional `q` query parameter to `EquipmentQueryController.searchEquipments()`, forward it to
`mapper.toSearchQuery()`, and update the OpenAPI `@Operation` and `@Parameter` annotations to document the new
free-text search capability.

## 2. File to Modify / Create

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/equipment/web/query/controller/EquipmentQueryController.java`
* **Action:** Modify Existing File

## 3. Code Implementation

**Imports Required:**

None — no new imports needed (all required types are already imported).

**Code to Add/Replace:**

* **Location:** Replace the entire `searchEquipments` handler method (the `@GetMapping` method at the bottom of
  `EquipmentQueryController`).

* **Current code:**

```java
    @GetMapping
    @Operation(summary = "Search equipment", description = "Returns paginated equipment list filtered by status and/or type")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Equipment page returned"),
            @ApiResponse(responseCode = "400", description = "Invalid filter parameters",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<Page<EquipmentResponse>> searchEquipments(
            @Parameter(description = "Status slug filter", example = "available") @RequestParam(name = "status", required = false) String status,
            @Parameter(description = "Type slug filter", example = "bike") @RequestParam(name = "type", required = false) String type,
            @PageableDefault(size = 20, sort = "serialNumber", direction = Sort.Direction.ASC) Pageable pageable) {

        log.info("[GET] Search equipments filters status={} type={}", status, type);
        var query = mapper.toSearchQuery(status, type, pageable);
        var page = searchUseCase.execute(query).map(mapper::toResponse);
        return ResponseEntity.ok(page);
    }
```

* **Snippet (replace with):**

```java
    @GetMapping
    @Operation(summary = "Search equipment", description = "Returns paginated equipment list filtered by status, type, and/or free-text search across uid, serial number, and model")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Equipment page returned"),
            @ApiResponse(responseCode = "400", description = "Invalid filter parameters",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<Page<EquipmentResponse>> searchEquipments(
            @Parameter(description = "Status slug filter", example = "available") @RequestParam(name = "status", required = false) String status,
            @Parameter(description = "Type slug filter", example = "bike") @RequestParam(name = "type", required = false) String type,
            @Parameter(description = "Free-text search across uid, serial number, and model (case-insensitive substring match)", example = "city") @RequestParam(name = "q", required = false) String q,
            @PageableDefault(size = 20, sort = "serialNumber", direction = Sort.Direction.ASC) Pageable pageable) {

        log.info("[GET] Search equipments filters status={} type={} q={}", status, type, q);
        var query = mapper.toSearchQuery(status, type, q, pageable);
        var page = searchUseCase.execute(query).map(mapper::toResponse);
        return ResponseEntity.ok(page);
    }
```

## 4. Validation Steps

```bash
./gradlew :service:compileJava
```

> The controller will compile cleanly. `EquipmentQueryControllerTest` still has stale calls to the old
> 3-arg `mapper.toSearchQuery` stub and old `SearchEquipmentsQuery` constructors. These are fixed in task 007.
