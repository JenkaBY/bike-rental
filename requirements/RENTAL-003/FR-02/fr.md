# User Story: FR-02 — Lifecycle Endpoint & Request Validation

## 1. Description

**As an** operator,
**I want to** use a dedicated `PATCH /api/rentals/{rentalId}/lifecycles` endpoint to transition
rental status,
**So that** lifecycle transitions are a first-class operation, separate from the generic JSON Patch
update endpoint.

## 2. Context & Business Rules

* **Trigger:** An operator sends `PATCH /api/rentals/{rentalId}/lifecycles` with a JSON body.
* **Request body:**
  ```json
  { "status": "ACTIVE" }
  ```
  or
  ```json
  { "status": "CANCELLED" }
  ```
* **Allowed status values:** `ACTIVE` and `CANCELLED` only.
    - `COMPLETED`, `DRAFT`, `DEBT`, and any other string are **not accepted** at this endpoint.
    - An invalid or absent status returns `400 Bad Request` with `errorCode: CONSTRAINT_VIOLATION`
      and an `errors` array identifying the `status` field.
* **Routing:**
    - `ACTIVE` → delegates to `ActivateRentalUseCase`
    - `CANCELLED` → delegates to `CancelRentalUseCase`
* **Path variable:** `{rentalId}` is the numeric Long ID of the rental. A non-existent ID returns
  `404 Not Found`.
* **Response on success:** `200 OK` with the updated rental representation (same shape as
  `GET /api/rentals/{id}`).

## 3. Non-Functional Requirements (NFRs)

* **Performance:** No additional database round-trips beyond what the delegated use case requires.
* **Security/Compliance:** N/A (auth is not active in this project).
* **Usability/Other:** The endpoint must be documented via SpringDoc OpenAPI annotations consistent
  with existing controller conventions.

## 4. Acceptance Criteria (BDD)

**Scenario 1: Activate a rental — happy path**

* **Given** a rental in `DRAFT` status with a valid hold
* **When** `PATCH /api/rentals/{rentalId}/lifecycles` is called with `{ "status": "ACTIVE" }`
* **Then** the response is `200 OK` and the rental status in the response is `ACTIVE`

**Scenario 2: Cancel a rental — happy path**

* **Given** a rental in `DRAFT` or `ACTIVE` status
* **When** `PATCH /api/rentals/{rentalId}/lifecycles` is called with `{ "status": "CANCELLED" }`
* **Then** the response is `200 OK` and the rental status in the response is `CANCELLED`

**Scenario 3: Missing status field — 400**

* **Given** a valid rental ID
* **When** `PATCH /api/rentals/{rentalId}/lifecycles` is called with `{}`
* **Then** the response is `400 Bad Request` with `errorCode: CONSTRAINT_VIOLATION` and `errors`
  containing `{ "field": "status", ... }`

**Scenario 4: Null status — 400**

* **Given** a valid rental ID
* **When** `PATCH /api/rentals/{rentalId}/lifecycles` is called with `{ "status": null }`
* **Then** the response is `400 Bad Request` with `errorCode: CONSTRAINT_VIOLATION`

**Scenario 5: Invalid status value (e.g., COMPLETED) — 400**

* **Given** a valid rental ID
* **When** `PATCH /api/rentals/{rentalId}/lifecycles` is called with `{ "status": "COMPLETED" }`
* **Then** the response is `400 Bad Request` with `errorCode: CONSTRAINT_VIOLATION`

**Scenario 6: Status value DRAFT — 400**

* **Given** a valid rental ID
* **When** `PATCH /api/rentals/{rentalId}/lifecycles` is called with `{ "status": "DRAFT" }`
* **Then** the response is `400 Bad Request` with `errorCode: CONSTRAINT_VIOLATION`

**Scenario 7: Non-existent rental — 404**

* **Given** a rental ID that does not exist in the system
* **When** `PATCH /api/rentals/{rentalId}/lifecycles` is called with `{ "status": "ACTIVE" }`
* **Then** the response is `404 Not Found`

**Scenario 8: Invalid state transition — 409 / 422**

* **Given** a rental in `CANCELLED` status
* **When** `PATCH /api/rentals/{rentalId}/lifecycles` is called with `{ "status": "ACTIVE" }`
* **Then** the response reflects the invalid status transition (as mapped by `RentalRestControllerAdvice`)

## 5. Out of Scope

* Accepting `COMPLETED` or `DRAFT` as lifecycle status values.
* Changing the response schema for the rental representation.
* Authentication / authorisation.
* The internal business logic performed by each use case (covered in FR-03, FR-04).
