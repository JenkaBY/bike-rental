# Task 005: Delete V1 Facade, Public DTOs, Domain Service, and Facade-Supporting Application Layer

> **Applied Skill:** `spring-boot-modulith` — `TariffFacade` is the V1 Spring Modulith cross-module
> boundary. `@Deprecated(forRemoval=true)` on the interface confirms intentional removal. All
> application-layer services that existed solely to back the V1 facade are also deleted in this task.

## 1. Objective

Delete the V1 `TariffFacade` interface, its `TariffFacadeImpl` implementation, the two public DTO
types (`TariffInfo`, `RentalCost`), the domain service record `BaseRentalCostResult`, and the three
facade-specific application artefacts (`CalculateRentalCostUseCase`, `CalculateRentalCostService`,
`SelectTariffForRentalUseCase`, `SelectTariffForRentalService`, `TariffToInfoMapper`).

After this task, no class in any module may reference `TariffFacade` or its V1 DTO types. Rental
module migration (FR-TR-01 / FR-TR-02) has already removed all Rental-side references.

> **Prerequisite:** task-004 must be completed first, so that the web-layer files that imported
> `TariffToInfoMapper`, `TariffInfo`, and `TariffSelectionMapper` are already deleted.

## 2. Files to Delete

All paths are relative to `service/src/main/java/com/github/jenkaby/bikerental/tariff/`.

### Public facade boundary (package root)

| File                    | Type                                                        |
|-------------------------|-------------------------------------------------------------|
| `TariffFacade.java`     | Interface `@Deprecated(forRemoval=true)`                    |
| `TariffFacadeImpl.java` | `@Service` implementation                                   |
| `TariffInfo.java`       | V1 DTO record (return type for `selectTariff` / `findById`) |
| `RentalCost.java`       | V1 cost-result interface                                    |

### Domain service (domain/service)

| File                                       | Type                               |
|--------------------------------------------|------------------------------------|
| `domain/service/BaseRentalCostResult.java` | `record` implementing `RentalCost` |

### Application use-case interfaces (application/usecase)

| File                                                    | Type                                                  |
|---------------------------------------------------------|-------------------------------------------------------|
| `application/usecase/CalculateRentalCostUseCase.java`   | Interface + inner `CalculateRentalCostCommand` record |
| `application/usecase/SelectTariffForRentalUseCase.java` | Interface + inner `SelectTariffCommand` record        |

### Application services (application/service)

| File                                                    | Type       |
|---------------------------------------------------------|------------|
| `application/service/CalculateRentalCostService.java`   | `@Service` |
| `application/service/SelectTariffForRentalService.java` | `@Service` |

### Application mapper (application/mapper)

| File                                         | Type                  |
|----------------------------------------------|-----------------------|
| `application/mapper/TariffToInfoMapper.java` | `@Mapper` (MapStruct) |

## 3. Code Implementation

No code is written; all actions are **file deletions**.

Absolute paths to delete (10 files):

```
service/src/main/java/com/github/jenkaby/bikerental/tariff/TariffFacade.java
service/src/main/java/com/github/jenkaby/bikerental/tariff/TariffFacadeImpl.java
service/src/main/java/com/github/jenkaby/bikerental/tariff/TariffInfo.java
service/src/main/java/com/github/jenkaby/bikerental/tariff/RentalCost.java
service/src/main/java/com/github/jenkaby/bikerental/tariff/domain/service/BaseRentalCostResult.java
service/src/main/java/com/github/jenkaby/bikerental/tariff/application/usecase/CalculateRentalCostUseCase.java
service/src/main/java/com/github/jenkaby/bikerental/tariff/application/usecase/SelectTariffForRentalUseCase.java
service/src/main/java/com/github/jenkaby/bikerental/tariff/application/service/CalculateRentalCostService.java
service/src/main/java/com/github/jenkaby/bikerental/tariff/application/service/SelectTariffForRentalService.java
service/src/main/java/com/github/jenkaby/bikerental/tariff/application/mapper/TariffToInfoMapper.java
```

> **Boundary check:** The following must remain untouched:
> - `TariffV2Facade.java`, `TariffV2FacadeImpl.java` (sole public V2 module boundary)
> - All `*V2*` use cases and services
> - `domain/service/BaseRentalCostCalculationResult.java`, `BaseEquipmentCostBreakdown.java`,
    > `BaseRentalCostV2.java` (V2 domain service classes)
>
> **Verify the Rental module is clean:** After deletion, search the `rental` module source tree for
> any remaining reference to `TariffFacade`, `TariffInfo`, `RentalCost` (the V1 interface). Zero
> matches must be found (they were already removed in FR-TR-01 / FR-TR-02).

## 4. Validation Steps

```bash
./gradlew :service:compileJava
```

Expected: BUILD SUCCESSFUL — V2 facade (`TariffV2Facade`, `TariffV2FacadeImpl`) and all V2
application services compile cleanly. Zero references to `TariffFacade`, `TariffInfo`, `RentalCost`,
or `BaseRentalCostResult` remain in any surviving `.java` file.
