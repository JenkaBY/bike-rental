# User Story: FR-07 — Component Tests for `GET /api/rentals/available-equipments`

## 1. Description

**As a** developer maintaining the BikeRental system,
**I want to** cover the `GET /api/rentals/available-equipments` endpoint with Cucumber component tests,
**So that** the endpoint's availability filtering logic (GOOD condition + not occupied) is continuously
validated end-to-end against a real database.

## 2. Context & Business Rules

* **Trigger:** FR-06 (`GET /api/rentals/available-equipments`) must be fully implemented before this
  story is executed.
* **Feature file location:** `component-test/src/test/resources/features/rental/rental-available-equipments.feature`
* **Initial equipment dataset (taken from `equipment.feature` Background):**

  | id | uid        | serialNumber | model      | condition | type    |
    |----|------------|--------------|------------|-----------|---------|
  | 1  | BIKE-001   | EQ-001       | Model A    | GOOD      | BICYCLE |
  | 2  | E-BIKE-001 | EQ-002       | Model B    | GOOD      | SCOOTER |
  | 3  | BIKE-003   | EQ-005       | Model C    | GOOD      | BICYCLE |
  | 4  | BIKE-002   | EQ-004       | Model C    | BROKEN    | BICYCLE |
  | 5  | BIKE-00-   | EQ-0066      | Model 1    | GOOD      | BICYCLE |
  | 6  | BIKE-0066  | EQ-007       | Model 2    | GOOD      | BICYCLE |
  | 7  | BIKE-009   | EQ-009       | Model 0066 | GOOD      | BICYCLE |

* **Rental occupancy applied in Background:**

  | equipmentId | status   | explanation                              |
    |-------------|----------|------------------------------------------|
  | 2           | ACTIVE   | E-BIKE-001 is in an active rental        |
  | 7           | ASSIGNED | BIKE-009 is assigned to a pending rental |

* **Effective "available" set (GOOD + not occupied):** ids 1, 3, 5, 6.

* **Rules Enforced:**
    - Only equipment with `condition = GOOD` is ever returned — BROKEN equipment is always excluded,
      regardless of whether it has a rental record.
    - Equipment whose `id` appears in a `rental_equipments` row with `status IN ('ACTIVE', 'ASSIGNED')`
      is considered occupied and must never appear in the response.
    - An empty result must return `200 OK` with `"content": []` — never `404`.
    - Default pagination is `page=0`, `size=20`.

## 3. Non-Functional Requirements (NFRs)

* **Performance:** N/A — component tests run against an isolated test database.
* **Security/Compliance:** N/A.
* **Usability/Other:**
    - Each scenario must be independently runnable (no shared mutable state between scenarios).
    - Follow the existing Cucumber conventions: `@ResetClock` if clock-sensitive, `@skip` for
      temporarily disabled scenarios.

## 4. Acceptance Criteria (BDD)

---

**Scenario 1: No filter — returns all GOOD and available equipment**

* **Given** the Background equipment and rental_equipment data are seeded
* **When** a `GET /api/rentals/available-equipments` request is made with no query parameters
* **Then** the response status is `200 OK`
* **And** the response `content` contains exactly equipment ids 1, 3, 5, 6 (BIKE-001, BIKE-003,
  BIKE-00-, BIKE-0066)
* **And** `$.totalItems` equals `4`
* **And** `$.pageRequest.page` equals `0`
* **And** `$.pageRequest.size` equals `20`

---

**Scenario 2: Filter by `q` — occupied equipment excluded from text-match results**

* **Given** the Background data is seeded
* **When** a `GET /api/rentals/available-equipments?q=0066` request is made
* **Then** the response status is `200 OK`
* **And** the response `content` contains exactly equipment ids 5, 6
  (serial `EQ-0066` and uid `BIKE-0066` — both match "0066"; id 7 also matches via model `Model 0066`
  but is occupied and must be excluded)
* **And** `$.totalItems` equals `2`

---

**Scenario 3: Filter by `q` — all matching equipment is occupied → empty result**

* **Given** the Background data is seeded
* **When** a `GET /api/rentals/available-equipments?q=E-BIKE` request is made
* **Then** the response status is `200 OK`
* **And** the response `content` is empty (`[]`)
* **And** `$.totalItems` equals `0`

---

**Scenario 4: BROKEN condition equipment always excluded**

* **Given** the Background data is seeded
* **When** a `GET /api/rentals/available-equipments?q=EQ-004` request is made
  (serial number of the BROKEN bike, id 4)
* **Then** the response status is `200 OK`
* **And** the response `content` is empty (`[]`)
* **And** `$.totalItems` equals `0`

---

**Scenario 5: Pagination — `page` and `size` params are reflected in the response**

* **Given** the Background data is seeded
* **When** a `GET /api/rentals/available-equipments?page=0&size=2` request is made
* **Then** the response status is `200 OK`
* **And** `$.totalItems` equals `4`
* **And** `$.pageRequest.page` equals `0`
* **And** `$.pageRequest.size` equals `2`
* **And** the response `content` contains exactly `2` items

---

## 5. Out of Scope

* Testing the internal use-case logic (two-phase filter) — covered by unit tests in FR-05.
* Testing invalid `page` parameter format (non-integer) — Spring MVC type mismatch, covered at the
  WebMVC test level in FR-06.
* Performance / load testing of the endpoint.
* Authentication / authorisation scenarios (the API is currently open).
