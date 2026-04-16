# System Design: FR-TR-03 - Remove TariffFacade V1 from Tariff Module

## 1. Architectural Overview

This story is a code-deletion cleanup. With FR-TR-01 and FR-TR-02 complete, the Rental module contains zero
references to V1 tariff types, and the V1 facade has no callers anywhere in the system. This story physically removes
the V1 `TariffFacade` interface, its implementation, all supporting V1 application-layer artefacts (use-case
interfaces, service classes, MapStruct mappers), the V1 DTO types (`TariffInfo`, `RentalCost`), the V1 domain
service class (`BaseRentalCostResult`), and the V1 HTTP endpoint controllers from the tariff module.

After this story, the tariff module's entire public boundary is `TariffV2Facade` and its associated V2 types. Any
HTTP request to a path that was previously served by a V1 controller returns `404 Not Found`. The OpenAPI
documentation lists only V2 tariff endpoints.

---

## 2. Impacted Components

* **`TariffFacadeImpl`** *(and `TariffFacade` interface)*: Deleted. The V1 public facade and its implementation are
  removed from the tariff module entirely. No stub, forwarding wrapper, or deprecation shim may remain.

* **`TariffQueryController`** *(V1 HTTP layer)*: V1 endpoint mappings (`GET /api/tariffs`, `GET /api/tariffs/{id}`,
  and any other paths served exclusively by V1 controllers) are removed. The controller class(es) are deleted.
  Requests to those paths will return `404 Not Found` from the framework.

* **`SelectTariffForRentalService`** and **`SelectTariffUseCase`**: Deleted — V1 auto-selection application layer
  is no longer needed.

* **`CalculateRentalCostService`** and **`CalculateRentalCostUseCase`**: Deleted — V1 cost-calculation application
  layer is no longer needed.

* **`TariffToInfoMapper`** and **`TariffSelectionMapper`**: Deleted — V1 MapStruct mappers with no remaining
  callers.

* **`TariffInfo`**: Deleted — V1 DTO record used as the return type for `selectTariff` / `findById` in the V1
  facade; has no callers after FR-TR-01 and FR-TR-02.

* **`RentalCost`**: Deleted — V1 cost-result interface used by `CalculateRentalCostService` and referenced from
  the Rental module's `ReturnEquipmentResult` / `RentalCommandMapper`; migrated to V2 equivalents in FR-TR-02.

* **`BaseRentalCostResult`**: Deleted — V1 domain service class that implements `RentalCost`; no remaining
  references.

* **All V1 web-layer DTOs and mappers** supporting the deleted V1 controllers: Deleted.

* **`TariffV2Facade`** *(unchanged)*: Remains as the sole public module boundary. No modification required.

---

## 3. Abstract Data Schema Changes

No schema changes. This story deletes code artefacts only. If any Liquibase changesets exist that are exclusive to
V1 features (e.g., columns or tables used only by deleted V1 services), a separate schema review is required before
removing them; data migration risk must be assessed independently. Column-level schema cleanup is explicitly out of
scope for this story.

---

## 4. Component Contracts & Payloads

* **Interaction: External Client → Tariff Module (V1 endpoints)**
    * **Protocol:** REST / HTTP
    * **Payload Changes:** All V1 endpoint paths are unmapped. Any client calling `GET /api/tariffs` or
      `GET /api/tariffs/{id}` (V1) after this story is applied will receive `404 Not Found`. No migration or
      redirect is provided.

* **Interaction: External Client → Tariff Module (V2 endpoints)**
    * **Protocol:** REST / HTTP
  * **Payload Changes:** No change. `GET /api/tariffs`, `GET /api/tariffs/{id}`, and related V2 paths
      continue to function as before.

* **Interaction: Any Module → `TariffFacade` (V1)**
    * **Protocol:** In-process synchronous call (Spring Modulith Facade)
    * **Payload Changes:** Interaction is deleted. No module retains a reference to `TariffFacade` or its types
      after FR-TR-01 and FR-TR-02 are complete.

---

## 5. Updated Interaction Sequence

### Verification Sequence — Build is Clean

1. After FR-TR-03 deletions are applied, `./gradlew build` is executed.
2. The compiler finds zero references to deleted types (`TariffFacade`, `TariffFacadeImpl`, `TariffInfo`,
   `RentalCost`, `BaseRentalCostResult`, etc.).
3. Build completes with no compilation errors; all existing unit and component tests pass.

### Verification Sequence — No V1 References in Rental Module

1. After FR-TR-03 changes, the rental module source tree is searched for `TariffFacade`, `TariffInfo`,
   `RentalCost` (V1 interface, not any V2 type).
2. Zero matches are found.

### Runtime Sequence — V1 Endpoint Returns 404

1. A client sends `GET /api/tariffs` to a running instance after FR-TR-03 is deployed.
2. The framework finds no mapped handler for that path.
3. Response: `404 Not Found`.

### Runtime Sequence — V2 Endpoint Remains Functional

1. A client sends `GET /api/tariffs`.
2. The V2 query controller handles the request normally.
3. Response: `200 OK` with the V2 tariff list.

---

## 6. Non-Functional Architecture Decisions

* **Security & Auth:** Removing the V1 endpoint set reduces the attack surface — previously reachable paths are
  now hard-404. No authentication change is required.

* **Scale & Performance:** Deletion only; no runtime performance impact expected. Build time may decrease
  marginally due to fewer compilation units and MapStruct annotation-processor targets.
