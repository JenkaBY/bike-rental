# User Story: FR-01 - Rental Optimistic Locking & Version in API

## 1. Description

**As an** API client (frontend) preparing the rental agreement signing flow
**I want to** receive a monotonically increasing `version` of a rental in every rental response and have concurrent rental modifications rejected
**So that** a stale client cannot overwrite newer rental state, and the `version` can later serve as a one-time fencing token during agreement signing (AGR-001/FR-02, FR-05)

## 2. Context & Business Rules

* **Trigger:** Any rental read or mutation through the existing REST API (`GET/POST/PUT/PATCH /api/rentals/**`).
* **Rules Enforced:**
    * The `rentals` table gains a `version BIGINT NOT NULL DEFAULT 0` column; existing rows start at `0`.
    * `RentalJpaEntity` carries a JPA `@Version` field; Hibernate increments it on every UPDATE of the rentals row.
    * Two concurrent modifications of the same rental result in exactly one winner; the loser receives `HTTP 409` with `errorCode = shared.resource.optimistic_lock` (already produced by the global `CoreExceptionHandlerAdvice` — no new handler code).
    * The domain aggregate `Rental` carries the `version` value untouched (read-only pass-through); domain logic never mutates it.
    * `RentalResponse` exposes `version` so the frontend can hold it as a fencing token.

## 3. Non-Functional Requirements (NFRs)

* **Performance:** No measurable overhead — a single extra column read/write per rental row.
* **Security/Compliance:** N/A (no new endpoints or data exposure beyond an integer counter).
* **Usability/Other:** The change is backward-compatible: existing clients ignore the extra JSON field.

## 4. Acceptance Criteria (BDD)

**Scenario 1: Version is exposed and starts at zero**

* **Given** a new draft rental is created via `POST /api/rentals`
* **When** the client reads the rental via `GET /api/rentals/{id}`
* **Then** the response body contains `version = 0`

**Scenario 2: Version increments on modification**

* **Given** an existing draft rental with `version = 0`
* **When** the client updates the draft via `PUT /api/rentals/{id}`
* **Then** the response body contains a `version` greater than `0`

**Scenario 3: Concurrent modification is rejected**

* **Given** two copies of the same rental state loaded in two transactions
* **When** both attempt to persist a modification
* **Then** the first commit succeeds and the second receives `HTTP 409` with `errorCode = shared.resource.optimistic_lock`

## 5. Out of Scope

* The `AWAITING_SIGNATURE` status and lifecycle changes (FR-02).
* Any agreement module code (FR-03..FR-05).
* Using the version as fencing token during signing (FR-05).
* Version columns for any table other than `rentals`.
