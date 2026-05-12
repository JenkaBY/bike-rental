# User Story: FR-06 — `GET /rentals/available-equipments` Endpoint

## 1. Description

**As a** rental staff member or integrated client application,
**I want to** query the list of equipment that is currently available for a new rental via a REST endpoint,
**So that** I can present only genuinely available options to the customer before creating a rental.

## 2. Context & Business Rules

* **Trigger:** FR-05 (`GetAvailableForRentEquipmentsUseCase`) must be complete before this story is
  implemented.
* **Rules Enforced:**
    - **HTTP method:** `GET`
    - **Path:** `/rentals/available-equipments`
    - **Query parameters (all optional):**
      | Parameter | Type | Description |
      |---|---|---|
      | `q` | String | Partial, case-insensitive match against UID, model, or serial number (OR across all three) |
      | `page` | int (default 0) | Zero-based page number |
      | `size` | int (default 20) | Page size |
    - The endpoint delegates entirely to `GetAvailableForRentEquipmentsUseCase`; no additional business
      logic lives in the controller.
    - **Response on success:** `200 OK` with body `Page<EquipmentInfo>`:
      ```json
      {
        "content": [ { "id": 1, "uid": "BIKE-001", "model": "...", "condition": "GOOD", ... } ],
        "totalElements": 6,
        "totalPages": 1,
        "page": 0,
        "size": 10
      }
      ```
    - **Note:** `totalElements` and `size` reflect the **filtered** count (GOOD + available); the page
      size may be smaller than requested (best-effort pagination — see FR-05).
    - **Response on empty result:** `200 OK` with empty `content` array (not `404`).
    - **Response on invalid params:** `400 Bad Request` with structured `ProblemDetail` (standard
      constraint violation format).
    - No request body is accepted.
    - No authentication is required (consistent with current API policy — all endpoints are open).
    - The endpoint lives in the rental module. It is placed in a query controller following the existing
      `web/query/` layer pattern (`EquipmentQueryController` equivalent but for rental availability).

## 3. Non-Functional Requirements (NFRs)

* **Performance:** Response time dominated by two sequential DB queries (EQUIP-002 FR-03 + RENTAL-002
  FR-02). For typical inventories (<1 000 bikes) this is expected to be under 200 ms.
* **Security/Compliance:** N/A — endpoint is open; no sensitive data is exposed beyond what existing
  equipment endpoints already return.
* **Usability/Other:**
    - The endpoint must be documented via SpringDoc OpenAPI annotations consistent with other controllers.
    - WebMVC tests must cover: happy path (200), empty result (200), invalid param format (400).
    - Component test (Cucumber) is NOT required for this story — happy path only suffices at the WebMVC
      test level for now.

## 4. Acceptance Criteria (BDD)

**Scenario 1: Returns available equipment with no filters**

* **Given** 3 GOOD bikes are available and 1 GOOD bike is ACTIVE in a rental
* **When** `GET /rentals/available-equipments` is called with no query params
* **Then** HTTP `200 OK` is returned with 3 equipment items in `content`

**Scenario 2: Returns empty list when no equipment is available**

* **Given** all GOOD bikes are currently in ACTIVE or ASSIGNED rentals
* **When** `GET /rentals/available-equipments` is called
* **Then** HTTP `200 OK` is returned with `"content": []` and `"totalElements": 0`

**Scenario 3: Text filter narrows results**

* **Given** 5 available GOOD bikes, 2 with model containing "MTB"
* **When** `GET /rentals/available-equipments?q=MTB` is called
* **Then** HTTP `200 OK` is returned with 2 items

* **Given** bike A has `uid = "ALPHA"`, bike B has `model = "BETA"`, both available and GOOD
* **When** `GET /rentals/available-equipments?uid=ALPHA&model=BETA` is called
* **Then** both bikes A and B are returned

**Scenario 5: Invalid pagination param returns 400**

* **Given** a caller passes `?size=-1`
* **When** `GET /rentals/available-equipments?size=-1` is called
* **Then** HTTP `400 Bad Request` is returned with a structured `ProblemDetail`

**Scenario 6: Endpoint is discoverable via OpenAPI**

* **Given** the application is running
* **When** `GET /v3/api-docs` is requested
* **Then** the spec includes `/rentals/available-equipments` with correct parameter definitions

## 5. Out of Scope

* Date-range filtering (`from`, `to` params) — deferred to a later story.
* Authentication / authorization — no security filter chain is active.
* Sorting query parameters — not specified; default order from the use case applies.
* Guaranteed exact page sizes — best-effort pagination is a known v1 limitation (see FR-05).
