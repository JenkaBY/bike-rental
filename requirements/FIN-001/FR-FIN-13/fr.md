# User Story: FR-FIN-13 - Retrieve Customer Account Balances (Ledgers)

## 1. Description

**As a** staff member
**I want to** retrieve a customer's current ledger balance breakdown via the API
**So that** I can inform the customer of their available and reserved funds before performing a deposit, withdrawal,
or rental

## 2. Context & Business Rules

* **Trigger:** Staff (or an authorised client) issues a GET request for a customer's balance before any financial
  operation.
* **Rules Enforced:**
    * The customer is identified by their UUID.
    * The customer's finance (`CustomerAccount`) must exist before the balance can be queried.
    * Three fields are always returned:
        * `walletBalance` — the customer's available (spendable) funds; equals the current balance of the
          `CUSTOMER_WALLET` sub-ledger.
        * `holdBalance` — funds currently reserved/pre-authorised for active rentals; equals the current balance
          of the `CUSTOMER_HOLD` sub-ledger.
        * `lastUpdatedAt` — the timestamp of the most recent ledger state change (UTC, ISO-8601). Always
          non-null because the `CustomerAccount` is created together with the customer record, which itself
          sets the initial timestamp.
    * All balance values are expressed with exactly 2 decimal places (precision `NUMERIC(19,2)`).
    * This endpoint is read-only — no ledger entries are created or modified.
    * A customer with an existing account but no transactions must return `walletBalance: 0.00` and
      `holdBalance: 0.00`; `lastUpdatedAt` reflects the account creation time. An empty account is not an error.

## 3. Non-Functional Requirements (NFRs)

* **Performance:** The response must be returned within 2 seconds under normal load (single DB read, no writes).
* **Security/Compliance:** The operation is read-only and must be logged with the `correlationId` for auditability.
  No state is mutated; the endpoint is safe to call multiple times with the same input.
* **Usability/Other:** The response uses the standard JSON envelope consistent with other query endpoints in the
  system. Error responses use the RFC 7807 `ProblemDetail` format with `correlationId` and `errorCode` fields.

## 4. Acceptance Criteria (BDD)

**Scenario 1: Retrieve balance for a customer with funds**

* **Given** a customer with a finance account whose `CUSTOMER_WALLET` balance is €120.00 and `CUSTOMER_HOLD`
  balance is €30.00
* **When** a GET request is made to `/api/finance/customers/{customerId}/balances`
* **Then** the response status is `200 OK`
* **And** the response body contains:
    * `walletBalance: 120.00`
    * `holdBalance: 30.00`
    * `lastUpdatedAt: "2026-04-07T10:30:00Z"` (timestamp of the most recent ledger mutation)

**Scenario 2: Retrieve balances for a customer with no transactions**

* **Given** a customer whose finance account was created at registration but has received no deposits
* **When** a GET request is made to `/api/finance/customers/{customerId}/balances`
* **Then** the response status is `200 OK`
* **And** the response body contains:
    * `walletBalance: 0.00`
    * `holdBalance: 0.00`
    * `lastUpdatedAt: "2026-04-07T09:00:00Z"` (account creation timestamp)

**Scenario 3: Customer finance account not found**

* **Given** a valid UUID that does not correspond to any registered finance account
* **When** a GET request is made to `/api/finance/customers/{customerId}/balances`
* **Then** the response status is `404 Not Found`
* **And** the response body is a `ProblemDetail` with `errorCode: RESOURCE_NOT_FOUND`

**Scenario 4: Invalid UUID format in path variable**

* **Given** a path variable `customerId` that is not a valid UUID (e.g., `"abc"`)
* **When** a GET request is made to `/api/finance/customers/abc/balances`
* **Then** the response status is `400 Bad Request`
* **And** the response body is a `ProblemDetail` with `errorCode: CONSTRAINT_VIOLATION`

## 5. Out of Scope

* Per-ledger transaction history (covered by FR-FIN-09 — customer transaction history).
* Exposing system asset sub-ledger balances (`CASH`, `CARD_TERMINAL`, `BANK_TRANSFER`, `REVENUE`, `ADJUSTMENT`).
* Aggregated balances across multiple customers.
* A computed `totalBalance` field — callers derive totals client-side if needed.
* Any write, deposit, or hold operation — this endpoint is strictly read-only.
