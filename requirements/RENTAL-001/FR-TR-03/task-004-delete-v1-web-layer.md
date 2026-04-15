# Task 004: Delete V1 Web Layer (Controllers, DTOs, Mappers)

> **Applied Skill:** `spring-mvc-controller-test`, `mapstruct-hexagonal` — V1 REST controllers
> annotated `@Deprecated(forRemoval=true)` that serve `GET/POST/PUT/PATCH /api/tariffs` are removed
> together with their web-layer DTOs and MapStruct mappers.

## 1. Objective

Delete the V1 `TariffCommandController`, `TariffQueryController`, and all associated web-layer DTO
records and MapStruct mapper interfaces. After this task, any HTTP request to `/api/tariffs` (V1
paths) will return `404 Not Found` from the Spring framework (no mapped handler).

> **Prerequisite:** task-002 and task-003 must be completed first so that all test files that
> reference these web classes are already gone.

## 2. Files to Delete

All paths are relative to `service/src/main/java/com/github/jenkaby/bikerental/tariff/`.

### Controllers

| File (relative to tariff root)             | Annotation on class                                                |
|--------------------------------------------|--------------------------------------------------------------------|
| `web/command/TariffCommandController.java` | `@Deprecated(forRemoval = true)` `@RequestMapping("/api/tariffs")` |
| `web/query/TariffQueryController.java`     | `@Deprecated(forRemoval = true)` `@RequestMapping("/api/tariffs")` |

### Web query DTOs

| File                                         | Description                                        |
|----------------------------------------------|----------------------------------------------------|
| `web/query/dto/TariffResponse.java`          | V1 tariff response record; imports `TariffStatus`  |
| `web/query/dto/TariffSelectionResponse.java` | V1 tariff selection record; imports `TariffPeriod` |

> **Do NOT delete:** `CostCalculationRequest.java`, `CostCalculationResponse.java`,
> `CostEstimateV2Response.java`, `PricingParams.java`, `PricingTypeResponse.java`,
> `TariffSelectionV2Response.java`, `TariffV2Response.java` — these are V2/shared types.

### Web query mappers

| File                                          | Why deleted                                             |
|-----------------------------------------------|---------------------------------------------------------|
| `web/query/mapper/TariffQueryMapper.java`     | Maps `Tariff` (V1) → `TariffResponse` (V1)              |
| `web/query/mapper/TariffSelectionMapper.java` | Maps `TariffInfo` (V1) → `TariffSelectionResponse` (V1) |

> **Do NOT delete:** `TariffV2QueryMapper.java`, `BatchCalculationMapper.java`,
> `CalculationBreakdownMapper.java`, `DiscountDetailMapper.java` — these are V2/shared mappers.

### Web command DTO

| File                                 | Why deleted                     |
|--------------------------------------|---------------------------------|
| `web/command/dto/TariffRequest.java` | V1 create/update request record |

> **Do NOT delete:** `TariffV2Request.java` and the `pricingparams/` subpackage — these are V2.

### Web command mapper

| File                                          | Why deleted                                 |
|-----------------------------------------------|---------------------------------------------|
| `web/command/mapper/TariffCommandMapper.java` | Maps `TariffRequest` → V1 use-case commands |

> **Do NOT delete:** `TariffV2CommandMapper.java` — V2 mapper.

## 3. Code Implementation

No code is written; all actions are **file deletions**.

Absolute paths to delete (10 files):

```
service/src/main/java/com/github/jenkaby/bikerental/tariff/web/command/TariffCommandController.java
service/src/main/java/com/github/jenkaby/bikerental/tariff/web/query/TariffQueryController.java
service/src/main/java/com/github/jenkaby/bikerental/tariff/web/query/dto/TariffResponse.java
service/src/main/java/com/github/jenkaby/bikerental/tariff/web/query/dto/TariffSelectionResponse.java
service/src/main/java/com/github/jenkaby/bikerental/tariff/web/query/mapper/TariffQueryMapper.java
service/src/main/java/com/github/jenkaby/bikerental/tariff/web/query/mapper/TariffSelectionMapper.java
service/src/main/java/com/github/jenkaby/bikerental/tariff/web/command/dto/TariffRequest.java
service/src/main/java/com/github/jenkaby/bikerental/tariff/web/command/mapper/TariffCommandMapper.java
```

> **Boundary check:** After deletion, verify that `TariffV2QueryController`, `TariffV2CommandController`,
> `PricingTypeQueryController`, and `TariffV2CalculationController` are still present. They must not
> be touched.

## 4. Validation Steps

```bash
./gradlew :service:compileJava
```

Expected: BUILD SUCCESSFUL — the remaining V2 controllers and all shared infrastructure compile
cleanly. No references to deleted DTOs or controllers remain in any surviving source file.
