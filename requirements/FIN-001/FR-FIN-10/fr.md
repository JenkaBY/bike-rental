# User Story: FR-FIN-10 - Customer Transaction History

## 1. Description

**As an** authorized user (staff or admin)
**I want to** query a paginated list of all ledger entries associated with a customer's finance account
**So that** I can review a full audit trail of all financial activity for that customer

## 2. Context & Business Rules

* **Trigger:** An authorized user requests the transaction history for a specific customer.
* **Rules Enforced:**
    * The query must be scoped to a single customer by their customer ID.
    * Results must include all journal entries associated with the customer's sub-ledgers (`CUSTOMER_WALLET` and
      `CUSTOMER_HOLD`).
    * Each entry must include at a minimum: account sub-ledger name, amount, direction (debit/credit), transaction type,
      timestamp, and optional metadata (e.g., reason for adjustments, payment method for deposits/withdrawals).
    * Results must be returned in reverse-chronological order (most recent first) by default.
    * Pagination is mandatory — the caller specifies page number and page size; the response includes the total number
      of entries.
    * The customer's finance account must exist; querying for an unknown customer returns a "not found" error.
    * Access to another customer's history is not permitted; access control is enforced at the API level.
* **Optional Filters:** The caller may supply any combination of the following filter parameters. When multiple filters
  are provided they combine with AND logic. Omitting a filter does not restrict results for that dimension.
    * `fromDate` — include only entries whose recorded date is on or after this date (date only, inclusive).
    * `toDate` — include only entries whose recorded date is on or before this date (date only, inclusive).
    * `sourceId` — include only entries whose source identifier matches this value exactly.
    * `sourceType` — include only entries whose source type matches this value. Supplying an unrecognised source type
      value is rejected with a validation error. Currently the only supported value is `RENTAL`.

## 3. Non-Functional Requirements (NFRs)

* **Performance:** The first page of results must load within 2 seconds for up to 10,000 journal entries.
* **Security/Compliance:** Only authenticated staff or admin users may query this endpoint. The query itself is not a
  mutating operation and does not create journal entries.
* **Usability/Other:** The response must clearly indicate total available records and current page metadata to support
  UI pagination controls.

## 4. Acceptance Criteria (BDD)

**Scenario 1: Successful paginated query returns entries in reverse-chronological order**

* **Given** a customer with 25 journal entries
* **When** an authorized staff member queries the history with page size 10 and page number 1
* **Then** 10 entries are returned, representing the 10 most recent entries
* **And** the response includes total entry count (25) and current page metadata
* **And** entries are ordered from most recent to oldest

**Scenario 2: Second page returns next set of entries**

* **Given** the same customer with 25 journal entries and a page size of 10
* **When** the querier requests page 2
* **Then** the next 10 entries (entries 11–20 in reverse-chronological order) are returned

**Scenario 3: Query for customer with no transactions**

* **Given** a customer with a newly created finance account and no journal entries
* **When** an authorized user queries the transaction history
* **Then** an empty list is returned with a total count of 0

**Scenario 4: Query rejected for unknown customer**

* **Given** a customer ID that does not correspond to any registered finance account
* **When** an authorized user requests the transaction history
* **Then** the request is rejected with a "customer account not found" error

**Scenario 5: Query result includes all required entry fields**

* **Given** a customer with a deposit journal entry
* **When** the history is queried
* **Then** the entry includes: sub-ledger name, amount, direction, transaction type, timestamp, and payment method
  metadata

**Scenario 6: Filter by date range returns only entries within the range**

* **Given** a customer with journal entries recorded across multiple dates
* **When** an authorized user queries the history with `fromDate` = D1 and `toDate` = D2
* **Then** only entries whose recorded date falls within [D1, D2] inclusive are returned
* **And** entries outside that date range are excluded

**Scenario 7: Filter by sourceType returns only matching entries**

* **Given** a customer with journal entries of different source types
* **When** an authorized user queries the history with `sourceType` = `RENTAL`
* **Then** only entries with source type `RENTAL` are returned

**Scenario 8: Filter by sourceId returns only entries linked to that source**

* **Given** a customer with journal entries linked to different source identifiers
* **When** an authorized user queries the history with a specific `sourceId`
* **Then** only entries whose source identifier matches that value are returned

**Scenario 9: Combined filters apply AND logic**

* **Given** a customer with journal entries covering various dates and source types
* **When** an authorized user queries with `fromDate`, `toDate`, and `sourceType` = `RENTAL`
* **Then** only entries that satisfy all three conditions simultaneously are returned

**Scenario 10: Invalid sourceType value is rejected**

* **Given** an authorized user supplies an unrecognised value for `sourceType`
* **When** the history is queried
* **Then** the request is rejected with a validation error identifying `sourceType` as the invalid field

**Scenario 11: No filters provided — full history returned**

* **Given** a customer with existing journal entries and no filter parameters supplied
* **When** an authorized user queries the transaction history
* **Then** all entries are returned paginated in reverse-chronological order, matching prior behaviour

## 5. Out of Scope

* Querying System Account ledger entries (admin-only capability, not in this story).
* Filtering by transaction type or amount (may be added in a future story).
* CSV/export functionality for the transaction history.
