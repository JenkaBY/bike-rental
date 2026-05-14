# System Design: FR-01 — Batch Equipment Fetch Endpoint

## 1. Architectural Overview

FR-01 adds a new HTTP handler to the existing `EquipmentQueryController` that exposes the already-implemented
batch-lookup capability of the equipment module over the public REST API. The application and domain layers require
no new logic — `GetEquipmentByIdsUseCase`, `GetEquipmentByIdsService`, and `EquipmentRepository.findByIds()` are
all already in place serving the internal `EquipmentFacade`. The only structural addition is a new mapper method on
`EquipmentQueryMapper` to convert a list of domain objects to response DTOs.

The new endpoint at `GET /api/equipments/batch` sits beside the existing read handlers within
`EquipmentQueryController`. Input validation (required parameter, positive integer constraint, 100-item cap) and
de-duplication are enforced at the web boundary before the use case is invoked. The response is a flat JSON array —
no pagination wrapper — because the caller controls the exact set of IDs.

## 2. Impacted Components

* **`EquipmentQueryController` (API):** Must gain a new handler method bound to `GET /api/equipments/batch`. The
  method must accept `ids` as a required comma-separated query parameter, validate that all values are positive
  integers, reject lists larger than 100 items with `400 Bad Request`, silently de-duplicate the list, delegate to
  `GetEquipmentByIdsUseCase`, and map the result through `EquipmentQueryMapper` to a `List<EquipmentResponse>`.
  OpenAPI annotations must document the endpoint summary, `ids` parameter format, and response codes `200` and `400`.

* **`EquipmentQueryMapper` (Web Query Mapper):** Must gain a new mapping method that converts a `List<Equipment>`
  to a `List<EquipmentResponse>`. Each element is mapped using the already-defined `toResponse(Equipment)` method
  (MapStruct derives list mappings automatically from the single-element method).

## 3. Abstract Data Schema Changes

None. The `equipment` table and all existing column structures remain unchanged. `EquipmentRepository.findByIds()`
already issues a single `IN`-predicate query against the existing primary-key column.

## 4. Component Contracts & Payloads

* **Interaction: External Client → `EquipmentQueryController`**
    * **Protocol:** HTTP GET
    * **Payload Changes:** New required query parameter `ids` — a comma-separated list of positive Long integers
      (e.g., `ids=1,2,3`). Maximum 100 values. Existing endpoints and parameters are unaffected.

* **Interaction: `EquipmentQueryController` → `GetEquipmentByIdsUseCase`**
    * **Protocol:** In-process method call
    * **Payload Changes:** The existing `execute(Collection<Long> ids)` signature is reused without modification. The
      controller passes a de-duplicated, validated list of IDs.

* **Interaction: `GetEquipmentByIdsService` → `EquipmentRepository`**
    * **Protocol:** In-process method call (domain port)
    * **Payload Changes:** `findByIds(Collection<Long> ids)` is reused without modification. Already performs a
      single `IN`-predicate query; missing IDs are naturally absent from the result.

* **Interaction: `EquipmentQueryController` → External Client**
    * **Protocol:** HTTP response
    * **Payload Changes:** New response shape: a flat JSON array of `EquipmentResponse` objects. Each object is
      identical in structure to the body returned by `GET /api/equipments/{id}`. No pagination envelope.

## 5. Updated Interaction Sequence

**Happy path — all requested IDs exist:**

1. External client sends `GET /api/equipments/batch?ids=1,2,3`.
2. `EquipmentQueryController` parses `ids` into `[1, 2, 3]`, validates all values are positive integers, confirms
   list size ≤ 100, de-duplicates (no-op here), and produces a clean `List<Long>`.
3. `EquipmentQueryController` calls `getEquipmentByIdsUseCase.execute([1, 2, 3])`.
4. `GetEquipmentByIdsService` calls `EquipmentRepository.findByIds([1, 2, 3])`.
5. `EquipmentRepositoryAdapter` issues a single `SELECT … WHERE id IN (1, 2, 3)` query and returns
   `List<Equipment>` with 3 elements.
6. `EquipmentQueryController` calls `mapper.toResponses(equipmentList)`.
7. `EquipmentQueryMapper` maps each `Equipment` to `EquipmentResponse`.
8. `EquipmentQueryController` returns `200 OK` with a JSON array of 3 objects.

**Happy path — some IDs do not exist:**

1. External client sends `GET /api/equipments/batch?ids=1,2,99`.
2. Steps 2–5 proceed as above; the repository returns only the 2 matching records.
3. Steps 6–8 proceed normally; the response array contains 2 objects. No error is raised for ID `99`.

**Happy path — empty result:**

1. External client sends `GET /api/equipments/batch?ids=91,92,93`.
2. Steps 2–5 proceed as above; the repository returns an empty list.
3. `EquipmentQueryController` returns `200 OK` with an empty JSON array `[]`.

**Unhappy path — `ids` parameter absent:**

1. External client sends `GET /api/equipments/batch` without `ids`.
2. `EquipmentQueryController` rejects the request with `400 Bad Request`.

**Unhappy path — malformed or non-positive value in `ids`:**

1. External client sends `GET /api/equipments/batch?ids=1,abc,3` or `GET /api/equipments/batch?ids=1,-5`.
2. `EquipmentQueryController` detects an invalid value during parsing/validation and returns `400 Bad Request`.

**Unhappy path — list exceeds 100 items:**

1. External client sends `GET /api/equipments/batch?ids=<101 IDs>`.
2. `EquipmentQueryController` detects that the list size exceeds the cap and returns `400 Bad Request`.

## 6. Non-Functional Architecture Decisions

* **Security & Auth:** The `ids` values are bound as a typed `Long` collection before they reach the persistence
  layer. No string interpolation into SQL occurs at any boundary. The `IN`-predicate query uses parameterised
  bindings, eliminating SQL injection risk.
* **Scale & Performance:** All matching records are resolved in a single database round-trip via the existing
  `findByIds` method. No N+1 queries are introduced. The 100-item cap bounds query plan complexity and protects
  against oversized `IN` clauses under high cardinality.
