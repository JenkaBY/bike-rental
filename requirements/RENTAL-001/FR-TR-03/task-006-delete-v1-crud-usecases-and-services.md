# Task 006: Delete Dead V1 CRUD Use Cases and Services

> **Applied Skill:** `spring-boot-data-ddd` — After the V1 web controllers are deleted, the
> remaining V1 CRUD use-case interfaces and service implementations have zero callers (no controller,
> no facade) and reference the `Tariff` domain model and `TariffRepository` port that will be
> removed in task-007. Delete them here to eliminate dangling references before the domain layer
> deletion.

## 1. Objective

Delete the seven V1 CRUD use-case interfaces and their corresponding `@Service` implementations.
These classes depended exclusively on the V1 `Tariff` domain model and `TariffRepository` port. No
V2 class references any of them.

> **Prerequisite:** task-005 must be completed first; the use-case interfaces deleted in this task
> are no longer referenced by any facade or facade-supporting service.

## 2. Files to Delete

All paths are relative to `service/src/main/java/com/github/jenkaby/bikerental/tariff/`.

### Use-case interfaces (application/usecase)

| File                                                              | Imports `Tariff`?                               |
|-------------------------------------------------------------------|-------------------------------------------------|
| `application/usecase/ActivateTariffUseCase.java`                  | Yes — return type `Tariff`                      |
| `application/usecase/DeactivateTariffUseCase.java`                | Yes — return type `Tariff`                      |
| `application/usecase/CreateTariffUseCase.java`                    | Yes — return type + `CreateTariffCommand`       |
| `application/usecase/UpdateTariffUseCase.java`                    | Yes — return type `Tariff`                      |
| `application/usecase/GetAllTariffsUseCase.java`                   | Yes — return type `Page<Tariff>`                |
| `application/usecase/GetTariffByIdUseCase.java`                   | Yes — return type `Tariff` / `Optional<Tariff>` |
| `application/usecase/GetActiveTariffsByEquipmentTypeUseCase.java` | Yes — return type `List<Tariff>`                |

### Service implementations (application/service)

| File                                                              | Type       |
|-------------------------------------------------------------------|------------|
| `application/service/ActivateTariffService.java`                  | `@Service` |
| `application/service/DeactivateTariffService.java`                | `@Service` |
| `application/service/CreateTariffService.java`                    | `@Service` |
| `application/service/UpdateTariffService.java`                    | `@Service` |
| `application/service/GetAllTariffsService.java`                   | `@Service` |
| `application/service/GetTariffByIdService.java`                   | `@Service` |
| `application/service/GetActiveTariffsByEquipmentTypeService.java` | `@Service` |

## 3. Code Implementation

No code is written; all actions are **file deletions**.

Absolute paths to delete (14 files):

```
service/src/main/java/com/github/jenkaby/bikerental/tariff/application/usecase/ActivateTariffUseCase.java
service/src/main/java/com/github/jenkaby/bikerental/tariff/application/usecase/DeactivateTariffUseCase.java
service/src/main/java/com/github/jenkaby/bikerental/tariff/application/usecase/CreateTariffUseCase.java
service/src/main/java/com/github/jenkaby/bikerental/tariff/application/usecase/UpdateTariffUseCase.java
service/src/main/java/com/github/jenkaby/bikerental/tariff/application/usecase/GetAllTariffsUseCase.java
service/src/main/java/com/github/jenkaby/bikerental/tariff/application/usecase/GetTariffByIdUseCase.java
service/src/main/java/com/github/jenkaby/bikerental/tariff/application/usecase/GetActiveTariffsByEquipmentTypeUseCase.java
service/src/main/java/com/github/jenkaby/bikerental/tariff/application/service/ActivateTariffService.java
service/src/main/java/com/github/jenkaby/bikerental/tariff/application/service/DeactivateTariffService.java
service/src/main/java/com/github/jenkaby/bikerental/tariff/application/service/CreateTariffService.java
service/src/main/java/com/github/jenkaby/bikerental/tariff/application/service/UpdateTariffService.java
service/src/main/java/com/github/jenkaby/bikerental/tariff/application/service/GetAllTariffsService.java
service/src/main/java/com/github/jenkaby/bikerental/tariff/application/service/GetTariffByIdService.java
service/src/main/java/com/github/jenkaby/bikerental/tariff/application/service/GetActiveTariffsByEquipmentTypeService.java
```

> **Do NOT delete:** The V2 counterparts — `ActivateTariffV2UseCase`, `ActivateTariffV2Service`,
> `DeactivateTariffV2UseCase`, `DeactivateTariffV2Service`, `CreateTariffV2UseCase`,
> `CreateTariffV2Service`, `UpdateTariffV2UseCase`, `UpdateTariffV2Service`,
> `GetAllTariffsV2UseCase`, `GetAllTariffsV2Service`, `GetTariffV2ByIdUseCase`,
> `GetTariffV2ByIdService`, `GetActiveTariffsV2ByEquipmentTypeUseCase`,
> `GetActiveTariffsV2ByEquipmentTypeService`, `RentalCostCalculationUseCase`,
> `RentalCostCalculationService`, `SelectTariffV2UseCase`, `SelectTariffV2Service` — all must remain.

## 4. Validation Steps

```bash
./gradlew :service:compileJava
```

Expected: BUILD SUCCESSFUL — the `Tariff` domain model and `TariffRepository` port still exist at
this point (deleted in task-007), so the infrastructure and domain layers compile cleanly. Zero V1
CRUD use-case or service references remain in any surviving source file.
