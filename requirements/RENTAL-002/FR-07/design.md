# System Design: RENTAL-002/FR-07 — Component Tests for `GET /api/rentals/available-equipments`

## 1. Architectural Overview

This story adds a Cucumber BDD component test suite that validates the `GET /api/rentals/available-equipments`
endpoint end-to-end against a real PostgreSQL database (managed by Testcontainers). It does not
introduce new production components; it exercises the full vertical slice introduced across
FR-01 through FR-06.

The test suite lives exclusively in the `component-test/` project. A new Cucumber feature file is
created under `features/rental/`. No new step-definition classes are required: all necessary step
vocabulary (`Given … equipment records exist in db`, `Given … rental equipment exists in the database
with the following data`, `When a GET request has been made to "…" endpoint`, `When a GET request has
been made to "…" endpoint with query parameters`, `Then the response status is …`,
`Then the response contains`, `Then the available equipment response only contains page of`) is either
already registered or added as a focused extension of the existing rental/equipment step classes.

---

## 2. Impacted Components

* **`component-test` project — new feature file
  (`features/rental/rental-available-equipments.feature`):**
  Adds six Cucumber scenarios covering the full acceptance-criteria matrix defined in FR-07's `fr.md`.
  No production code changes.

* **`RentalAvailabilityQueryWebSteps` (new step class — `steps/rental/`):**
  A thin step-definition class holding the single new `Then` step:
  `"the available equipment response only contains page of"`. It reads the page body via
  `ScenarioContext.getResponseAsPage(AvailableEquipmentResponse.class)` and validates
  the returned `items` list field-by-field (id, uid, serialNumber, typeSlug, model).
  Sorting: items sorted by `id` on both sides before zip-comparison.

  All other steps (`Given … equipment records exist`, `Given … rental equipment exists`,
  `When a GET request …`, `Then the response status is`, `Then the response contains`) are already
  provided by existing step classes and require no changes.

---

## 3. Abstract Data Schema Changes

None. The test suite reads from and writes to the existing `equipments`, `equipment_statuses`,
`equipment_types`, and `rental_equipments` tables using the already-registered Cucumber step
definitions.

---

## 4. Component Contracts & Payloads

* **Interaction: Cucumber test → `bike-rental-api`**
    * **Protocol:** HTTP GET (via `TestRestTemplate` wired into `WebRequestSteps`)
    * **Endpoint under test:** `GET /api/rentals/available-equipments`
    * **Response shape verified by tests:**
      ```
      Page<AvailableEquipmentResponse>
        items        : [ { id, uid, serialNumber, typeSlug, model } ]
        totalItems   : long
        pageRequest  : { size, page, sortBy }
      ```
    * **Error response shape (scenario 6):**
      ```
      ProblemDetail
        status        : 400
        errorCode     : CONSTRAINT_VIOLATION
        correlationId : <uuid>
      ```

---

## 5. Updated Interaction Sequence

### Feature file Background (shared by all scenarios)

1. Insert `equipment_statuses` rows: BROKEN, AVAILABLE, MAINTENANCE, RENTED.
2. Insert `equipment_types` rows: BICYCLE, SCOOTER.
3. Insert `equipment` rows (ids 1–7) as specified in `fr.md` equipment dataset table.
4. Insert `rentals` rows: one rental for equipment id 2 (status ACTIVE), one for id 7 (status ACTIVE
   at rental level, equipment row ASSIGNED).
5. Insert `rental_equipments` rows: id 2 → status ACTIVE; id 7 → status ASSIGNED.

### Scenario 1 — No filter, all GOOD + available equipment returned

1. HTTP GET `/api/rentals/available-equipments` with no query params.
2. `RentalAvailabilityQueryController` builds empty `EquipmentSearchFilter` and default `PageRequest(page=0, size=20)`.
3. Use case calls `EquipmentFacade.getEquipmentsByConditions(GOOD, emptyFilter)` → returns ids 1, 2, 3, 5, 6, 7.
4. Use case calls `EquipmentAvailabilityService.getUnavailableIds({1,2,3,5,6,7})` → returns {2, 7}.
5. Use case filters out {2, 7} → available list: ids 1, 3, 5, 6.
6. Controller returns `200 OK`. Test asserts `items` contains exactly ids 1, 3, 5, 6; `totalItems=4`;
   `pageRequest.size=20`; `pageRequest.page=0`.

### Scenario 2 — `q=0066`, occupied match excluded

1. HTTP GET `/api/rentals/available-equipments?q=0066`.
2. Equipment facade returns ids 5 (serialNumber EQ-0066), 6 (uid BIKE-0066), 7 (model Model 0066).
3. Unavailability check returns {7}.
4. Filtered list: ids 5, 6.
5. Test asserts `items` contains exactly ids 5, 6; `totalItems=2`.

### Scenario 3 — `q=E-BIKE`, only match is occupied → empty result

1. HTTP GET `/api/rentals/available-equipments?q=E-BIKE`.
2. Equipment facade returns id 2 (uid E-BIKE-001).
3. Unavailability check returns {2}.
4. Filtered list is empty.
5. Controller returns `200 OK`. Test asserts `items=[]`; `totalItems=0`.

### Scenario 4 — BROKEN equipment always excluded

1. HTTP GET `/api/rentals/available-equipments?q=EQ-004`.
2. Equipment facade filters by condition=GOOD; id 4 has condition BROKEN → facade returns empty list.
3. Use case short-circuits and returns `Page.empty(pageRequest)`.
4. Controller returns `200 OK`. Test asserts `items=[]`; `totalItems=0`.

### Scenario 5 — Pagination params reflected in response

1. HTTP GET `/api/rentals/available-equipments?page=0&size=2`.
2. Use case builds `PageRequest(page=0, size=2)`.
3. Available set is ids 1, 3, 5, 6 (4 items total); in-memory pagination returns first 2.
4. Controller returns `200 OK`. Test asserts `totalItems=4`; `pageRequest.page=0`; `pageRequest.size=2`;
   `items` list has size 2.

---

## 6. Non-Functional Architecture Decisions

* **Security & Auth:** No authentication is required (endpoint is open); no special security setup
  needed in component tests.
* **Scale & Performance:** Testcontainers spins up an isolated PostgreSQL instance per test run.
  Dataset is small (7 equipment rows, 2 rental rows); no caching concerns.
* **Test isolation:** Each scenario runs in its own `@ScenarioScope` context. The `@Transactional`
  rollback mechanism or `@Sql` cleanup used by the existing component-test infrastructure ensures
  no cross-scenario state leakage.
