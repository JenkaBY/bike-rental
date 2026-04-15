# Task 002: Delete V1 Unit Tests (service module)

> **Applied Skill:** `spring-mvc-controller-test` — Unit test files that own references to deleted V1
> production types must be removed before the production types are deleted, so that
> `compileTestJava` output stays green after each subsequent task.

## 1. Objective

Physically delete all JUnit/WebMvc test classes in the `service` module that exercise V1 tariff
production code. After this task `./gradlew :service:compileTestJava` must succeed even though the
referenced production classes have not yet been deleted.

## 2. Files to Delete

All paths are relative to `service/src/test/java/com/github/jenkaby/bikerental/tariff/`.

| File (relative to tariff test root)                         | Why deleted                                                                            |
|-------------------------------------------------------------|----------------------------------------------------------------------------------------|
| `application/service/CalculateRentalCostServiceTest.java`   | Tests `CalculateRentalCostService`; imports `RentalCost`, `CalculateRentalCostUseCase` |
| `application/service/SelectTariffForRentalServiceTest.java` | Tests `SelectTariffForRentalService`                                                   |
| `application/service/ActivateTariffServiceTest.java`        | Tests `ActivateTariffService`; imports `Tariff`, `TariffRepository`                    |
| `application/service/DeactivateTariffServiceTest.java`      | Tests `DeactivateTariffService`; imports `Tariff`, `TariffRepository`                  |
| `application/service/CreateTariffServiceTest.java`          | Tests `CreateTariffService`; imports `Tariff`, `CreateTariffUseCase`                   |
| `application/service/UpdateTariffServiceTest.java`          | Tests `UpdateTariffService`; imports `Tariff`, `UpdateTariffUseCase`                   |
| `application/service/GetTariffByIdServiceTest.java`         | Tests `GetTariffByIdService`; imports `Tariff`, `GetTariffByIdUseCase`                 |
| `web/query/TariffQueryControllerTest.java`                  | Tests `TariffQueryController`; imports `TariffSelectionResponse`                       |
| `web/command/TariffCommandControllerTest.java`              | Tests `TariffCommandController`; imports `TariffRequest`, `TariffResponse`             |
| `domain/model/TariffTest.java`                              | Tests `Tariff` domain model; imports `Tariff`, `TariffStatus`                          |

> **Do NOT delete:** `TariffPeriodTest.java`, `TariffPeriodSelectorTest.java`,
> `TariffV2QueryControllerTest.java`, `TariffV2CommandControllerTest.java`,
> `TariffV2CalculationControllerTest.java` — these are V2 tests that must remain.

## 3. Code Implementation

No code is written; all actions are **file deletions**. Delete each of the 10 files listed above.

The absolute paths are:

```
service/src/test/java/com/github/jenkaby/bikerental/tariff/application/service/CalculateRentalCostServiceTest.java
service/src/test/java/com/github/jenkaby/bikerental/tariff/application/service/SelectTariffForRentalServiceTest.java
service/src/test/java/com/github/jenkaby/bikerental/tariff/application/service/ActivateTariffServiceTest.java
service/src/test/java/com/github/jenkaby/bikerental/tariff/application/service/DeactivateTariffServiceTest.java
service/src/test/java/com/github/jenkaby/bikerental/tariff/application/service/CreateTariffServiceTest.java
service/src/test/java/com/github/jenkaby/bikerental/tariff/application/service/UpdateTariffServiceTest.java
service/src/test/java/com/github/jenkaby/bikerental/tariff/application/service/GetTariffByIdServiceTest.java
service/src/test/java/com/github/jenkaby/bikerental/tariff/web/query/TariffQueryControllerTest.java
service/src/test/java/com/github/jenkaby/bikerental/tariff/web/command/TariffCommandControllerTest.java
service/src/test/java/com/github/jenkaby/bikerental/tariff/domain/model/TariffTest.java
```

## 4. Validation Steps

```bash
./gradlew :service:compileTestJava "-Dspring.profiles.active=test"
```

Expected: BUILD SUCCESSFUL — `service` test sources compile with no errors referencing deleted
classes. The 10 deleted test files no longer exist; all remaining test sources still compile cleanly.
