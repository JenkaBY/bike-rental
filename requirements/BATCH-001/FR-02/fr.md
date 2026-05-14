# User Story: FR-02 — Batch Customer Fetch Endpoint

## 1. Description

**As a** frontend client rendering a rental list  
**I want to** fetch multiple customer records in a single HTTP request by providing a list of UUIDs  
**So that** the number of API calls required to populate customer details on rental cards is reduced from N individual
calls to one

## 2. Context & Business Rules

* **Trigger:** A client calls `GET /api/customers/batch` with the required query parameter `ids`.
* **Rules Enforced:**
    * `ids` is **required**. A request without `ids` must be rejected with `400 Bad Request`.
    * `ids` must be a comma-separated list of valid UUIDs (RFC 4122 format). Malformed UUID values must be rejected
      with `400 Bad Request`.
    * The list must contain between **1 and 100** UUIDs (inclusive). A list exceeding 100 items must be rejected with
      `400 Bad Request`.
    * Duplicate values within `ids` are silently de-duplicated; each customer record is returned at most once.
    * The endpoint returns only the records that exist. UUIDs that do not correspond to any customer record are
      **silently omitted** from the response — no error is raised for missing UUIDs.
    * The response is a flat, unordered JSON array of customer objects. No pagination wrapper is applied.
    * Each object in the response array carries the same fields as the response from `GET /api/customers/{id}`.

## 3. Non-Functional Requirements (NFRs)

* **Performance:** The endpoint must resolve all matching records in a single database query (no N+1 queries).
* **Security/Compliance:** The `ids` parameter is treated as a typed UUID list and must not be interpolated
  unsafely into SQL. Parameterised queries must be used.
* **Usability/Other:** The endpoint must be documented in the OpenAPI specification with a summary, parameter
  description, example value, and all documented response codes.

## 4. Acceptance Criteria (BDD)

**Scenario 1: All requested UUIDs exist — full result returned**

* **Given** customer records with UUIDs `aaaaaaaa-0000-0000-0000-000000000001`,
  `aaaaaaaa-0000-0000-0000-000000000002`, and `aaaaaaaa-0000-0000-0000-000000000003` all exist
* **When**
  `GET /api/customers/batch?ids=aaaaaaaa-0000-0000-0000-000000000001,aaaaaaaa-0000-0000-0000-000000000002,aaaaaaaa-0000-0000-0000-000000000003`
  is called
* **Then** the response is `200 OK` with a JSON array containing exactly 3 customer objects

**Scenario 2: Some requested UUIDs do not exist — partial result returned**

* **Given** customer records with UUIDs `aaaaaaaa-0000-0000-0000-000000000001` and
  `aaaaaaaa-0000-0000-0000-000000000002` exist, but `bbbbbbbb-0000-0000-0000-000000000099` does not
* **When**
  `GET /api/customers/batch?ids=aaaaaaaa-0000-0000-0000-000000000001,aaaaaaaa-0000-0000-0000-000000000002,bbbbbbbb-0000-0000-0000-000000000099`
  is called
* **Then** the response is `200 OK` with a JSON array containing exactly 2 customer objects; no error is raised for
  the missing UUID

**Scenario 3: No requested UUIDs exist — empty array returned**

* **Given** no customer records with the provided UUIDs exist
* **When** `GET /api/customers/batch?ids=cccccccc-0000-0000-0000-000000000001` is called
* **Then** the response is `200 OK` with an empty JSON array `[]`

**Scenario 4: Duplicate UUIDs are de-duplicated**

* **Given** a customer record with UUID `aaaaaaaa-0000-0000-0000-000000000001` exists
* **When** `GET /api/customers/batch?ids=aaaaaaaa-0000-0000-0000-000000000001,aaaaaaaa-0000-0000-0000-000000000001`
  is called
* **Then** the response is `200 OK` with a JSON array containing exactly 1 customer object

**Scenario 5: `ids` parameter is absent**

* **When** `GET /api/customers/batch` is called without the `ids` parameter
* **Then** the response is `400 Bad Request`

**Scenario 6: `ids` contains a malformed UUID**

* **When** `GET /api/customers/batch?ids=not-a-valid-uuid` is called
* **Then** the response is `400 Bad Request`

**Scenario 7: `ids` list exceeds 100 items**

* **When** `GET /api/customers/batch?ids=<101 comma-separated UUIDs>` is called
* **Then** the response is `400 Bad Request`

**Scenario 8: Each returned object matches single-fetch schema**

* **Given** a customer record with UUID `aaaaaaaa-0000-0000-0000-000000000001` exists
* **When** `GET /api/customers/batch?ids=aaaaaaaa-0000-0000-0000-000000000001` is called
* **Then** the response is `200 OK` with a single-element array, and the object contains the same fields as the
  response from `GET /api/customers/aaaaaaaa-0000-0000-0000-000000000001`

## 5. Out of Scope

* Frontend integration or Angular service changes.
* Pagination, sorting, or filtering by any customer attribute on this endpoint.
* Changes to the existing endpoints `GET /api/customers` (phone-search list) or `GET /api/customers/{id}`.
* Authentication and authorisation rules (follow the existing convention of the customer module).
