# System Design: FR-03 — Backward Compatibility of Existing Single-Fetch Endpoints

## 1. Architectural Overview

FR-03 is a structural constraint, not a feature addition. It mandates that the changes introduced by FR-01 and FR-02
are strictly additive — new handler methods and new `/batch` sub-paths are appended to the existing controllers
without touching any existing method signature, routing annotation, validation rule, or response mapping.

Because the new batch handlers are registered under distinct paths (`/api/equipments/batch` and
`/api/customers/batch`) using separate handler methods, the existing handlers for the paginated list endpoints
(`GET /api/equipments`, `GET /api/customers`) and the single-fetch endpoints (`GET /api/equipments/{id}`,
`GET /api/customers/{id}`) are structurally unaffected at the routing level. The `/batch` path segment also avoids
any ambiguity with the path-variable handlers (`/{id}`) because it is a fixed literal segment, which the router
resolves with higher priority than a variable segment.

No schema migrations, existing use-case contract changes, or response DTO modifications are permitted as part of
this story.

## 2. Impacted Components

* **`EquipmentQueryController` (API):** The new `GET /api/equipments/batch` handler must be implemented as an
  additional method; no existing method body, parameter list, validation, or annotation may be altered.

* **`CustomerQueryController` (API):** The new `GET /api/customers/batch` handler must be implemented as an
  additional method; no existing method body, parameter list, validation, or annotation may be altered.

No other components are impacted by this story.

## 3. Abstract Data Schema Changes

None.

## 4. Component Contracts & Payloads

* **Interaction: External Client → `EquipmentQueryController` (existing endpoints)**
    * **Protocol:** HTTP GET
    * **Payload Changes:** None. The contracts for `GET /api/equipments/{id}`, `GET /api/equipments`,
      `GET /api/equipments/by-uid/{uid}`, and `GET /api/equipments/by-serial/{serialNumber}` are frozen.

* **Interaction: External Client → `CustomerQueryController` (existing endpoints)**
    * **Protocol:** HTTP GET
    * **Payload Changes:** None. The contracts for `GET /api/customers/{id}` and `GET /api/customers` are frozen.

## 5. Updated Interaction Sequence

No new interaction sequence is introduced. The existing sequences for all listed endpoints remain identical to their
pre-FR-01/FR-02 state:

1. External client sends a request to any existing equipment or customer read endpoint.
2. The router resolves the request to the unchanged existing handler method (fixed literal `/batch` path does not
   interfere with `/{id}` or the base collection path).
3. The existing handler executes with its unchanged logic and returns its unchanged response structure.

**Router disambiguation — `/batch` vs `/{id}`:**

1. External client sends `GET /api/equipments/batch?ids=1,2`.
2. The router matches the fixed literal segment `/batch` in preference to the variable segment `/{id}`.
3. The batch handler is invoked; the existing `/{id}` handler is not called.

1. External client sends `GET /api/equipments/5`.
2. The router matches the variable segment `/{id}` with value `5`.
3. The existing single-fetch handler is invoked; the batch handler is not called.

## 6. Non-Functional Architecture Decisions

* **Security & Auth:** No change to authentication, authorisation, or validation behaviour on any existing endpoint.
  The additive implementation model ensures existing security posture is preserved.
* **Scale & Performance:** No change to the query patterns or caching behaviour of existing endpoints. The addition
  of new handlers in the same controller class does not affect the performance of existing handlers.
