# System Design: RENTAL-002/FR-06 — `GET /rentals/available-equipments` Endpoint

## 1. Architectural Overview

This story exposes the availability query use case (FR-05) as a public REST endpoint on the rental
module. A new read-only query controller is added following the existing `web/query/` layer pattern.
The controller is a thin adapter: it maps HTTP query parameters to `EquipmentSearchFilter` and
`PageRequest`, delegates to `GetAvailableForRentEquipmentsUseCase`, and serialises the result as a
`Page<EquipmentInfo>` response body.

No new business logic lives in the controller. All filtering, availability checking, and pagination
are encapsulated in the use case layer (FR-05).

---

## 2. Impacted Components

* **`RentalAvailabilityQueryController` (new — `rental/web/query/`):**
  New `@RestController` scoped to the rental module. Exposes:
    - `GET /rentals/available-equipments`
    - Query params: `q` (optional text, OR-search across uid/model/serialNumber), `page` (default 0),
      `size` (default 20)
    - Constructs `EquipmentSearchFilter` from `q` and `PageRequest` from page/size.
    - Delegates to `GetAvailableForRentEquipmentsUseCase.getAvailableEquipments(filter, pageRequest)`.
    - Returns `200 OK` with `Page<EquipmentInfo>` body (even when the result is empty).

* **`GetAvailableForRentEquipmentsUseCase` (consumed — no changes):**
  Injected into the controller; introduced in FR-05.

* **OpenAPI / SpringDoc annotations:**
  The controller is annotated with `@Operation`, `@Parameter`, and `@ApiResponse` consistent with
  other query controllers in the project. The spec is updated automatically by SpringDoc at runtime.

---

## 3. Abstract Data Schema Changes

None.

---

## 4. Component Contracts & Payloads

* **Interaction: HTTP client → `RentalAvailabilityQueryController`**
    * **Protocol:** HTTP GET
    * **Request:**
      ```
      GET /rentals/available-equipments
          ?q=<string>    (optional, case-insensitive partial match across uid / model / serialNumber)
          &page=<int>    (optional, default 0)
          &size=<int>    (optional, default 20)
      ```
    * **Response 200 OK:**
      ```json
      {
        "content": [
          {
            "id": 1,
            "uid": "BIKE-001",
            "serialNumber": "SN-ABC",
            "typeSlug": "mountain-bike",
            "statusSlug": "AVAILABLE",
            "model": "Trek Marlin 5",
            "condition": "GOOD"
          }
        ],
        "totalElements": 6,
        "totalPages": 1,
        "page": 0,
        "size": 20
      }
      ```
      Note: `content` may be empty; `totalElements` reflects the filtered (GOOD + available) count.
      Page size may be smaller than requested (best-effort pagination — see FR-05).

    * **Response 400 Bad Request (invalid params):**
      ```json
      {
        "status": 400,
        "errorCode": "CONSTRAINT_VIOLATION",
        "correlationId": "<uuid>",
        "errors": [{ "field": "size", "message": "must be greater than 0" }]
      }
      ```

* **Interaction: `RentalAvailabilityQueryController` → `GetAvailableForRentEquipmentsUseCase`**
    * **Protocol:** In-process use-case call
    * **Payload:** `EquipmentSearchFilter(q)`, `PageRequest(page, size)`

---

## 5. Updated Interaction Sequence

**Happy path — returns available equipment:**

1. HTTP client sends `GET /rentals/available-equipments?q=MTB&page=0&size=10`.
2. `RentalAvailabilityQueryController` constructs:
    - `EquipmentSearchFilter(q="MTB")`
    - `PageRequest(page=0, size=10)`
3. Controller calls `useCase.getAvailableEquipments(filter, pageRequest)`.
4. Use case executes two-phase filter (see FR-05 sequence).
5. Returns `Page<EquipmentInfo>` with matching available MTB equipment.
6. Controller serialises and returns `200 OK`.

**Empty result path:**

1. Use case returns `Page{content=[], totalElements=0}`.
2. Controller returns `200 OK` with `"content": []` — not `404`.

**Invalid parameter path:**

1. Client sends `GET /rentals/available-equipments?size=-1`.
2. Spring MVC / `@Validated` rejects the parameter before it reaches the use case.
3. `@RestControllerAdvice` returns `400 Bad Request` with structured `ProblemDetail`.

**OpenAPI discovery:**

1. Client sends `GET /v3/api-docs`.
2. SpringDoc includes `/rentals/available-equipments` with all query parameter definitions and
   the `Page<EquipmentInfo>` response schema.

---

## 6. Non-Functional Architecture Decisions

* **Security & Auth:** Endpoint is open (no `SecurityFilterChain` configured); consistent with all
  other current endpoints.
* **Scale & Performance:** Response time is dominated by the two DB queries in the use case layer.
  For typical inventories the endpoint is expected to respond within 200 ms. No caching at this
  stage.
* **Pagination caveat (documented):** The `size` field in the response may be smaller than the
  requested `size` query parameter due to in-memory availability filtering. Clients must not assume
  that a page with fewer items than `size` means the last page; they should use `totalElements`
  and `totalPages` for navigation.
