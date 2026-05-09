# User Story: FR-01 — Add Free-Text Search Parameter to Equipment List Endpoint

## 1. Description

**As an** operator using the equipment catalogue screen  
**I want to** type a free-text query to filter the equipment list  
**So that** I can quickly locate equipment by any part of its identifier, serial number, or model name without knowing
the exact filter category

## 2. Context & Business Rules

* **Trigger:** An operator calls `GET /api/equipments` with the optional query parameter `q`.
* **Rules Enforced:**
    * `q` is optional; when absent or blank, the text filter is not applied and all equipment matching the other active
      filters is returned.
    * When `q` is provided, only equipment where at least one of `uid`, `serialNumber`, or `model` **contains** the
      value of `q` is included in the result (OR logic across the three fields).
    * The match is **case-insensitive** (e.g., `q=bike` matches `BIKE-001`, `Mountain Bike`, `SN-bike22`).
    * The existing `status` and `type` filter parameters remain unchanged and compose with `q` using AND logic: all
      active filters must be satisfied simultaneously.
    * Pagination and sorting behaviour is unchanged.
    * `q` is propagated unchanged from controller through `SearchEquipmentsQuery.searchText` to
      `EquipmentRepository.findAll()`.

## 3. Non-Functional Requirements (NFRs)

* **Performance:** The search must not introduce N+1 queries. The database performs the filter in a single query with
  appropriate predicates.
* **Security/Compliance:** `q` is treated as a plain filter value; it must not be interpolated unsafely into SQL
  strings. The Specification API handles parameterisation.
* **Usability/Other:** The OpenAPI documentation for `GET /api/equipments` must be updated to describe the new `q`
  parameter with a descriptive summary and example value.

## 4. Acceptance Criteria (BDD)

**Scenario 1: Matching results returned for partial text query**

* **Given** the equipment catalogue contains entries with uid `BIKE-001`, serialNumber `SN-2024-BK`, and model `City
  Bike`
* **When** `GET /api/equipments?q=bike` is called
* **Then** the response is `200 OK` with a page that contains all three equipment items (uid, serialNumber, and model
  each independently matched the substring)

**Scenario 2: Search is case-insensitive**

* **Given** an equipment item has model `Mountain Bike`
* **When** `GET /api/equipments?q=MOUNTAIN` is called
* **Then** the response is `200 OK` and the page includes that equipment item

**Scenario 3: No matching results returns empty page**

* **Given** no equipment has uid, serialNumber, or model containing `xyz999`
* **When** `GET /api/equipments?q=xyz999` is called
* **Then** the response is `200 OK` with an empty content list and total elements = 0

**Scenario 4: Omitting `q` returns all results (no text filter)**

* **Given** multiple equipment items exist
* **When** `GET /api/equipments` is called without the `q` parameter
* **Then** the response is `200 OK` and no equipment is excluded due to a text filter (only `status` / `type` filters
  apply if provided)

**Scenario 5: `q` combines with `status` filter (AND logic)**

* **Given** equipment `BIKE-001` has status `available` and equipment `BIKE-002` has status `in-use`
* **When** `GET /api/equipments?q=bike&status=available` is called
* **Then** only `BIKE-001` appears in the result; `BIKE-002` is excluded because its status does not match

## 5. Out of Scope

* Fuzzy / phonetic matching — only exact substring containment is required.
* Full-text search indexing (e.g., PostgreSQL `tsvector`) — plain `ILIKE` is sufficient.
* Sorting by relevance score — standard sort order (`serialNumber` ASC) is unchanged.
* Changes to any endpoint other than `GET /api/equipments`.
* Front-end implementation.
