# Task 002: Add Batch Handler to `EquipmentQueryController`

> **Applied Skill:** N/A — standard Spring MVC handler addition with Bean Validation on query parameter and
> OpenAPI annotations following the existing controller patterns.

## 1. Objective

Inject `GetEquipmentByIdsUseCase` into `EquipmentQueryController` and add the
`GET /api/equipments/batch?ids=…` handler. The handler validates that `ids` is present, contains only positive
Long values, and does not exceed 100 items, then delegates to the use case and maps the result with
`EquipmentQueryMapper.toResponses()`.

## 2. File to Modify / Create

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/equipment/web/query/controller/EquipmentQueryController.java`
* **Action:** Modify Existing File

## 3. Code Implementation

**Imports Required:**

Add the following imports to the existing import block:

```java
import com.github.jenkaby.bikerental.equipment.application.usecase.GetEquipmentByIdsUseCase;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.List;
```

**Code to Add/Replace:**

### Step A — Add field and update constructor

* **Location:** Replace the existing field block and constructor of `EquipmentQueryController`.

* **Current code:**

```java
    private final GetEquipmentByIdUseCase getById;
    private final GetEquipmentByUidUseCase getByUid;
    private final GetEquipmentBySerialNumberUseCase getBySerial;
    private final SearchEquipmentsUseCase searchUseCase;
    private final EquipmentQueryMapper mapper;

    EquipmentQueryController(GetEquipmentByIdUseCase getById,
                             GetEquipmentByUidUseCase getByUid,
                             GetEquipmentBySerialNumberUseCase getBySerial,
                             SearchEquipmentsUseCase searchUseCase,
                             EquipmentQueryMapper mapper) {
        this.getById = getById;
        this.getByUid = getByUid;
        this.getBySerial = getBySerial;
        this.searchUseCase = searchUseCase;
        this.mapper = mapper;
    }
```

* **Snippet (replace with):**

```java
    private final GetEquipmentByIdUseCase getById;
    private final GetEquipmentByUidUseCase getByUid;
    private final GetEquipmentBySerialNumberUseCase getBySerial;
    private final SearchEquipmentsUseCase searchUseCase;
    private final GetEquipmentByIdsUseCase getByIds;
    private final EquipmentQueryMapper mapper;

    EquipmentQueryController(GetEquipmentByIdUseCase getById,
                             GetEquipmentByUidUseCase getByUid,
                             GetEquipmentBySerialNumberUseCase getBySerial,
                             SearchEquipmentsUseCase searchUseCase,
                             GetEquipmentByIdsUseCase getByIds,
                             EquipmentQueryMapper mapper) {
        this.getById = getById;
        this.getByUid = getByUid;
        this.getBySerial = getBySerial;
        this.searchUseCase = searchUseCase;
        this.getByIds = getByIds;
        this.mapper = mapper;
    }
```

---

### Step B — Add the batch handler method

* **Location:** Add the following new method inside the `EquipmentQueryController` class body, immediately
  **before** the closing `}` of the class (i.e., after the existing `searchEquipments` method).

* **Snippet:**

```java
    @GetMapping("/batch")
    @Operation(
            summary = "Batch get equipment by IDs",
            description = "Returns a flat list of equipment records for the provided IDs. IDs that do not match any record are silently omitted.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Equipment list returned",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = EquipmentResponse.class)))),
            @ApiResponse(responseCode = "400", description = "Invalid ids parameter — missing, non-numeric, non-positive, or more than 100 elements",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<List<EquipmentResponse>> getBatchEquipments(
            @Parameter(description = "Comma-separated list of positive equipment IDs, 1–100 elements", example = "1,2,3")
            @RequestParam(name = "ids")
            @NotEmpty(message = "ids must not be empty")
            @Size(max = 100, message = "ids must contain at most 100 elements")
            List<@Positive(message = "Each equipment ID must be a positive number") Long> ids) {
        log.info("[GET] Batch fetch equipment ids count={}", ids.size());
        var distinctIds = ids.stream().distinct().toList();
        var equipment = getByIds.execute(distinctIds);
        return ResponseEntity.ok(mapper.toResponses(equipment));
    }
```

## 4. Validation Steps

skip