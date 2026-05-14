# User Story: FR-01 — Batch Equipment Fetch Endpoint

## 1. Description

**As a** frontend client rendering a rental list  
**I want to** fetch multiple equipment records in a single HTTP request by providing a list of IDs  
**So that** the number of API calls required to render the rental list page is reduced from N×K to a fixed constant

## 2. Context & Business Rules

* **Trigger:** A client calls `GET /api/equipments/batch` with the required query parameter `ids`.
* **Rules Enforced:**
    * `ids` is **required**. A request without `ids` must be rejected with `400 Bad Request`.
    * `ids` must be a comma-separated list of positive integers (Long). Non-numeric or non-positive values must be
      rejected with `400 Bad Request`.
    * The list must contain between **1 and 100** IDs (inclusive). A list exceeding 100 items must be rejected with
      `400 Bad Request`.
    * Duplicate values within `ids` are silently de-duplicated; each equipment record is returned at most once.
    * The endpoint returns only the records that exist. IDs that do not correspond to any equipment record are
      **silently omitted** from the response — no error is raised for missing IDs.
    * The response is a flat, unordered JSON array of equipment objects. No pagination wrapper is applied.
    * Each object in the response array carries the same fields as the response from `GET /api/equipments/{id}`.

## 3. Non-Functional Requirements (NFRs)

* **Performance:** The endpoint must resolve all matching records in a single database query (no N+1 queries).
* **Security/Compliance:** The `ids` parameter is treated as a typed integer list; it must not be interpolated
  unsafely into SQL. Parameterised queries must be used.
* **Usability/Other:** The endpoint must be documented in the OpenAPI specification with a summary, parameter
  description, example value, and all documented response codes.

## 4. Acceptance Criteria (BDD)

**Scenario 1: All requested IDs exist — full result returned**

* **Given** equipment records with IDs `1`, `2`, and `3` all exist in the catalogue
* **When** `GET /api/equipments/batch?ids=1,2,3` is called
* **Then** the response is `200 OK` with a JSON array containing exactly 3 equipment objects, one per requested ID

**Scenario 2: Some requested IDs do not exist — partial result returned**

* **Given** equipment records with IDs `1` and `2` exist, but ID `99` does not
* **When** `GET /api/equipments/batch?ids=1,2,99` is called
* **Then** the response is `200 OK` with a JSON array containing exactly 2 equipment objects (IDs `1` and `2`);
  no error is raised for the missing ID `99`

**Scenario 3: No requested IDs exist — empty array returned**

* **Given** no equipment records with IDs `91`, `92`, or `93` exist
* **When** `GET /api/equipments/batch?ids=91,92,93` is called
* **Then** the response is `200 OK` with an empty JSON array `[]`

**Scenario 4: Duplicate IDs are de-duplicated**

* **Given** an equipment record with ID `5` exists
* **When** `GET /api/equipments/batch?ids=5,5,5` is called
* **Then** the response is `200 OK` with a JSON array containing exactly 1 equipment object for ID `5`

**Scenario 5: `ids` parameter is absent**

* **When** `GET /api/equipments/batch` is called without the `ids` parameter
* **Then** the response is `400 Bad Request`

**Scenario 6: `ids` contains a non-numeric value**

* **When** `GET /api/equipments/batch?ids=1,abc,3` is called
* **Then** the response is `400 Bad Request`

**Scenario 7: `ids` contains a non-positive value**

* **When** `GET /api/equipments/batch?ids=1,-5,3` is called
* **Then** the response is `400 Bad Request`

**Scenario 8: `ids` list exceeds 100 items**

* **When** `GET /api/equipments/batch?ids=1,2,...,101` is called with 101 comma-separated IDs
* **Then** the response is `400 Bad Request`

**Scenario 9: Each returned object matches single-fetch schema**

* **Given** an equipment record with ID `7` exists
* **When** `GET /api/equipments/batch?ids=7` is called
* **Then** the response is `200 OK` with a single-element array, and the object contains the same fields as the
  response from `GET /api/equipments/7`

## 5. Out of Scope

* Frontend integration or Angular service changes.
* Pagination, sorting, or filtering by equipment status, type, or any other attribute on this endpoint.
* Changes to the existing endpoints `GET /api/equipments`, `GET /api/equipments/{id}`,
  `GET /api/equipments/by-uid/{uid}`, or `GET /api/equipments/by-serial/{serialNumber}`.
* Authentication and authorisation rules (follow the existing convention of the equipment module).
