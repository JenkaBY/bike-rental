# User Story: FR-01 — Date Range Filter for Search Rentals

## 1. Description

**As a** API consumer (admin, back-office operator, or integration client),
**I want to** filter the rentals list by a creation date range (`from` and/or `to`),
**So that** I can narrow the search results to a specific time period without fetching the entire
rental history.

## 2. Context & Business Rules

* **Trigger:** A caller sends `GET /api/rentals` with at least one of the optional query parameters
  `from` or `to`.
* **Rules Enforced:**
    - Both `from` and `to` accept dates in the format `yyyy-MM-dd` (ISO-8601 date, e.g. `2026-02-15`).
    - Both parameters are optional and independent: either one may be provided alone, or both together.
    - `from` is inclusive and mapped to the **start of the UTC day**: `{date}T00:00:00Z`.
    - `to` is inclusive and mapped to the **end of the UTC day**: `{date}T23:59:59Z`.
    - The filter targets the `createdAt` field of the rental record (stored in UTC).
    - The date range filter is an **AND condition** applied on top of all other active filters
      (`status`, `customerId`, `equipmentUid`). Omitting these other filters does not remove the
      date range condition, and vice-versa.
    - When neither `from` nor `to` is provided, the behaviour of the endpoint is identical to the
      current baseline (no `createdAt` restriction).
    - Pagination and sort behaviour are unchanged.

## 3. Non-Functional Requirements (NFRs)

* **Performance:** The `createdAt` column must be queried with a range predicate. The database index
  on `createdAt` (if present) must be usable; no full-table scan should result from adding this filter.
* **Security/Compliance:** N/A — the filter does not introduce new data exposure beyond what the
  existing endpoint already returns.
* **Usability/Other:**
    - Invalid date strings (e.g. `2026-13-01`) must return `HTTP 400` with a descriptive
      `ProblemDetail` error body.
    - An unsupported date format (e.g. `15-02-2026`) must return `HTTP 400`.

## 4. Acceptance Criteria (BDD)

**Scenario 1: `from` only — returns rentals created on or after the given date (component test)**

* **Given** rentals exist with `createdAt` values of `2026-02-10`, `2026-02-15`, and `2026-02-20`
* **When** `GET /api/rentals?from=2026-02-15` is called
* **Then** `HTTP 200` is returned
* **And** only rentals with `createdAt >= 2026-02-15T00:00:00Z` are included in the response

**Scenario 2: `to` only — returns rentals created on or before the given date (component test)**

* **Given** rentals exist with `createdAt` values of `2026-02-10`, `2026-02-15`, and `2026-02-20`
* **When** `GET /api/rentals?to=2026-02-15` is called
* **Then** `HTTP 200` is returned
* **And** only rentals with `createdAt <= 2026-02-15T23:59:59Z` are included in the response

**Scenario 3: Both `from` and `to` — returns rentals within the inclusive range (component test)**

* **Given** rentals exist with `createdAt` values of `2026-02-10`, `2026-02-15`, and `2026-02-20`
* **When** `GET /api/rentals?from=2026-02-15&to=2026-02-15` is called
* **Then** `HTTP 200` is returned
* **And** only rentals with `createdAt` within `[2026-02-15T00:00:00Z, 2026-02-15T23:59:59Z]` are returned

**Scenario 4: `from`/`to` combined with `status` filter (component test)**

* **Given** rentals exist in `ACTIVE` and `COMPLETED` statuses across different dates
* **When** `GET /api/rentals?status=ACTIVE&from=2026-02-15&to=2026-02-20` is called
* **Then** `HTTP 200` is returned
* **And** only `ACTIVE` rentals with `createdAt` in the specified date range are returned

**Scenario 5: No date range params — existing behaviour unchanged**

* **Given** the endpoint is called without `from` or `to`
* **When** `GET /api/rentals?status=ACTIVE` is called
* **Then** `HTTP 200` is returned and results are not restricted by `createdAt`

**Scenario 6: Invalid date format — returns 400**

* **Given** the endpoint is called with a malformed date
* **When** `GET /api/rentals?from=15-02-2026` is called
* **Then** `HTTP 400` is returned with a `ProblemDetail` error body

## 5. Out of Scope

* Changes to the `GET /api/rentals/{id}` (single rental) endpoint.
* Filtering by any field other than `createdAt` (e.g. `startedAt`, `expectedReturnAt`).
* Date-time input (time-of-day precision is not accepted; only date-only values are supported).
* Timezone selection per-request (UTC is always used for boundary calculation).
* Maximum date range limit enforcement.
