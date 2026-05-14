# User Story: FR-02 — Validate Date Range Consistency

## 1. Description

**As a** API consumer,
**I want to** receive a clear error when I accidentally supply a `from` date that is after the `to` date,
**So that** I can immediately identify and correct the invalid filter instead of receiving an empty
result set with no indication of the mistake.

## 2. Context & Business Rules

* **Trigger:** A caller sends `GET /api/rentals` with both `from` and `to` query parameters present.
* **Rules Enforced:**
    - When both `from` and `to` are provided, `from` must not be after `to` (i.e. `from <= to`).
    - The same-day case (`from == to`) is valid and must be accepted.
    - If only one of the two parameters is present, this validation does not apply.
    - Violation returns `HTTP 400` with a `ProblemDetail` response body whose `errorCode` property
      is `CONSTRAINT_VIOLATION`.

## 3. Non-Functional Requirements (NFRs)

* **Performance:** Validation is a simple in-memory comparison; no database calls are made before
  this guard passes.
* **Security/Compliance:** N/A.
* **Usability/Other:** The `ProblemDetail` error body should include a human-readable `detail` field
  explaining that `from` must not be after `to`.

## 4. Acceptance Criteria (BDD)

**Scenario 1: `from` after `to` — returns 400**

* **Given** the endpoint is called with `from=2026-02-20` and `to=2026-02-15`
* **When** `GET /api/rentals?from=2026-02-20&to=2026-02-15` is called
* **Then** `HTTP 400` is returned
* **And** the response body is a `ProblemDetail` with `errorCode = CONSTRAINT_VIOLATION`

**Scenario 2: `from` equals `to` (same day) — valid**

* **Given** the endpoint is called with `from=2026-02-15` and `to=2026-02-15`
* **When** `GET /api/rentals?from=2026-02-15&to=2026-02-15` is called
* **Then** `HTTP 200` is returned (no validation error)

**Scenario 3: `from` before `to` — valid**

* **Given** the endpoint is called with `from=2026-02-15` and `to=2026-02-20`
* **When** `GET /api/rentals?from=2026-02-15&to=2026-02-20` is called
* **Then** `HTTP 200` is returned (no validation error)

**Scenario 4: Only `from` provided — validation skipped**

* **Given** the endpoint is called with only `from=2026-02-20` (no `to`)
* **When** `GET /api/rentals?from=2026-02-20` is called
* **Then** `HTTP 200` is returned (range consistency validation does not apply)

**Scenario 5: Only `to` provided — validation skipped**

* **Given** the endpoint is called with only `to=2026-02-15` (no `from`)
* **When** `GET /api/rentals?to=2026-02-15` is called
* **Then** `HTTP 200` is returned (range consistency validation does not apply)

## 5. Out of Scope

* Validation of the date format itself (handled at deserialization / FR-01 Scenario 6).
* Maximum date range span enforcement.
* Validation of `from`/`to` in any endpoint other than `GET /api/rentals`.
