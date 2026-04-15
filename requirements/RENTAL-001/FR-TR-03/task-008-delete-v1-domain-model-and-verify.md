# Task 008: Delete V1 Domain Model and Final Verification

> **Applied Skill:** `spring-boot-data-ddd` — `Tariff.java` is the V1 aggregate root. With all
> callers (infrastructure, application services, web layer) already deleted in tasks 004–007,
> deleting the domain model and its status enum completes the V1 purge and satisfies FR-TR-03
> acceptance criteria 1 and 2.

## 1. Objective

Delete the `Tariff` aggregate and `TariffStatus` enum — the last surviving V1 domain artefacts.
After this task, the entire V1 tariff stack is purged and all remaining production code belongs
exclusively to the V2 surface. Run the full service unit test suite to confirm acceptance criteria 1
and 2 are met.

> **Prerequisite:** task-007 must be completed first; `TariffJpaMapper`, `TariffRepository`, and
> `TariffRepositoryAdapter` (all of which imported `Tariff`) are already deleted.

## 2. Files to Delete

All paths are relative to `service/src/main/java/com/github/jenkaby/bikerental/tariff/domain/model/`.

| File                | Why deleted                                                             |
|---------------------|-------------------------------------------------------------------------|
| `Tariff.java`       | V1 aggregate root; imports `TariffStatus`, `TariffPeriod`, `Money`      |
| `TariffStatus.java` | V1 status enum; only referenced by `Tariff` and its now-deleted callers |

> **Do NOT delete:** `TariffPeriod.java` — this enum is still used by
> `TariffPeriodSelector`, `OvertimeCalculationStrategy`, and `ProportionalOvertimeCalculationStrategy`
> (all V2-supporting utilities). `TariffV2.java`, `TariffV2Status.java`, `PricingType.java`,
> `DailyTariffV2.java`, `DegressiveHourlyTariffV2.java`, `FlatFeeTariffV2.java`,
> `FlatHourlyTariffV2.java`, `SpecialTariffV2.java` — all V2 domain classes, must remain.

## 3. Code Implementation

No code is written; all actions are **file deletions**.

Absolute paths to delete (2 files):

```
service/src/main/java/com/github/jenkaby/bikerental/tariff/domain/model/Tariff.java
service/src/main/java/com/github/jenkaby/bikerental/tariff/domain/model/TariffStatus.java
```

## 4. Post-deletion Verification Checklist

Before running the validation commands, manually confirm each of these conditions:

- [ ] No file under `service/src/main/java/` contains `import com.github.jenkaby.bikerental.tariff.TariffFacade`
- [ ] No file under `service/src/main/java/` contains `import com.github.jenkaby.bikerental.tariff.TariffInfo`
- [ ] No file under `service/src/main/java/` contains `import com.github.jenkaby.bikerental.tariff.RentalCost` _(the V1
  interface — not `RentalCostV2`)_
- [ ] No file under `service/src/main/java/com/github/jenkaby/bikerental/rental/` references any of the three symbols
  above
- [ ] `service/src/main/resources/db/changelog/v1/tariffs.create-table.xml` does **not** exist
- [ ] `service/src/main/resources/db/changelog/data/tariffs-provisioning.xml` does **not** exist
- [ ] `db.changelog-master.xml` does **not** contain `v1/tariffs.create-table.xml` or `data/tariffs-provisioning.xml`
- [ ] `component-test/.../tariff/tariff.feature` does **not** exist
- [ ] `component-test/.../tariff/tariff-selection.feature` does **not** exist

## 5. Validation Steps

```bash
./gradlew :service:test "-Dspring.profiles.active=test"
```

Expected:

- BUILD SUCCESSFUL
- Zero compilation errors in both `compileJava` and `compileTestJava`
- All remaining unit tests pass (V2 tariff tests, rental tests, equipment tests, etc.)
- No test references `TariffFacade`, `TariffInfo`, or V1 `RentalCost`
