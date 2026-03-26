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

## 5. Out of Scope

* Querying System Account ledger entries (admin-only capability, not in this story).
* Filtering or searching by transaction type, date range, or amount (may be added in a future story).
* CSV/export functionality for the transaction history.
