# User Story: FR-TR-03 - Remove TariffFacade V1 from Tariff Module

## 1. Description

**As a** developer maintaining the codebase
**I want to** delete the deprecated V1 TariffFacade interface, its implementation, and all V1-only supporting classes
**So that** the tariff module exposes only the V2 API surface, the codebase has no dead code, and future contributors
cannot accidentally depend on the legacy pricing path

## 2. Context & Business Rules

* **Trigger:** FR-TR-01 and FR-TR-02 are both complete — the Rental module has zero import references to V1 tariff
  types.
* **Prerequisite:** FR-TR-01 and FR-TR-02 must be complete before this story is started.
* **Rules Enforced:**
    * All items listed below must be deleted; no stubs, empty implementations, or forwarding wrappers may remain.
    * The `@Deprecated(forRemoval=true)` annotation on `TariffFacade` confirms this deletion is intentional.
    * `ReturnEquipmentResult` and `RentalCommandMapper` in the Rental module reference `RentalCost` — these must be
      migrated to use V2 equivalents (`EquipmentCostBreakdown` / `RentalCostCalculationResult`) as part of FR-TR-02
      before this story deletes `RentalCost`.
    * After deletion, the tariff module's public API consists solely of `TariffV2Facade` and its associated types.

### Artefacts to Delete

#### Tariff module — public facade and implementation

| Artefact           | Type      |
|--------------------|-----------|
| `TariffFacade`     | Interface |
| `TariffFacadeImpl` | Class     |

#### Tariff module — V1 public DTO types

| Artefact     | Type                                                    |
|--------------|---------------------------------------------------------|
| `TariffInfo` | Record (V1 return type for `selectTariff` / `findById`) |
| `RentalCost` | Interface (V1 cost result)                              |

#### Tariff module — V1 application layer

| Artefact                     | Type               |
|------------------------------|--------------------|
| `CalculateRentalCostUseCase` | Use-case interface |
| `CalculateRentalCostService` | Service class      |
| `SelectTariffUseCase`        | Use-case interface |
| `SelectTariffService`        | Service class      |
| `TariffToInfoMapper`         | MapStruct mapper   |
| `TariffSelectionMapper`      | MapStruct mapper   |

#### Tariff module — V1 domain / infrastructure

| Artefact               | Type                                           |
|------------------------|------------------------------------------------|
| `BaseRentalCostResult` | Domain service class (implements `RentalCost`) |

#### Tariff module — V1 web layer

| Artefact                                                                                                       | Type                            |
|----------------------------------------------------------------------------------------------------------------|---------------------------------|
| V1 query/command controllers under `web/` that serve V1 tariff endpoints (e.g., deprecated `GET /api/tariffs`) | REST controllers                |
| V1 DTOs and mappers supporting those controllers                                                               | DTO records / MapStruct mappers |

## 3. Non-Functional Requirements (NFRs)

* **Performance:** N/A — deletion story.
* **Security/Compliance:** Removing the V1 endpoint reduces the API attack surface. Ensure the build succeeds with
  zero compilation errors and zero warnings related to removed symbols.
* **Usability/Other:** After removal, the OpenAPI documentation must list only V2 tariff endpoints
  (`/api/tariffs`). Any V1 tariff paths must return `404 Not Found`.

## 4. Acceptance Criteria (BDD)

**Scenario 1: Build is clean**

* **Given** FR-TR-03 changes are applied
* **When** `./gradlew build` is executed
* **Then** the build succeeds with no compilation errors
* **And** all existing tests pass

**Scenario 2: No V1 tariff references in the Rental module**

* **Given** FR-TR-03 changes are applied
* **When** the Rental module source is searched for `TariffFacade`, `TariffInfo`, `RentalCost`
  (the V1 interface, not `RentalCostV2`)
* **Then** zero matches are found

**Scenario 3: V1 tariff endpoints return 404**

* **Given** the application is started after FR-TR-03 is applied
* **When** a client calls any endpoint that was served by a V1 tariff controller (e.g., `GET /api/tariffs`)
* **Then** the response is `404 Not Found`

**Scenario 4: V2 tariff endpoints remain functional**

* **Given** FR-TR-03 changes are applied
* **When** a client calls `GET /api/tariffs`
* **Then** the response is `200 OK` with the tariff list

**Scenario 5: V1 Liquibase changelog files are physically removed**

* **Given** FR-TR-03 changes are applied
* **Then** `v1/tariffs.create-table.xml` is deleted from the project
* **And** `data/tariffs-provisioning.xml` is deleted from the project
* **And** their `<include>` entries are removed from `db.changelog-master.xml`

## 5. Out of Scope

* Any new functionality. This is a pure deletion / cleanup story.
* Changes to the V2 tariff API contract.
* The V2 changelog files (`v1/tariffs_v2.create-table.xml`, `data/tariffs_v2-provisioning.xml`) are NOT removed.
