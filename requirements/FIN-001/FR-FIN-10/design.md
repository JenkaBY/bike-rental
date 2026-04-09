# System Design: FR-FIN-10 - Customer Transaction History

## 1. Architectural Overview

FR-FIN-10 introduces a read-only query capability into the `finance` module. An authorized user (staff or admin) sends
a paginated HTTP GET request scoped to a specific customer. The `AccountQueryController` (or a dedicated sibling
controller) routes the call to a new `GetTransactionHistoryUseCase` application service. That service delegates to an
extended `TransactionRepository` port that accepts a structured filter specification and returns a paginated, ordered
slice of `Transaction` aggregates. The `TransactionRecord` children on each `Transaction` carry the per-sub-ledger
debit/credit lines; only entries whose sub-ledgers are customer-owned (`CUSTOMER_WALLET`, `CUSTOMER_HOLD`) are
included.

The filter specification is resolved by the `specification-arg-resolver` library, which binds individual HTTP query
parameters (`fromDate`, `toDate`, `sourceId`, `sourceType`) to a composable predicate at the infrastructure layer. All
supplied predicates are combined with AND logic. No new modules or external services are introduced; the change is
entirely within the `finance` bounded context.

---

## 2. Impacted Components

* **`AccountQueryController` (Finance Query API):** Gains a new endpoint
  `GET /api/finance/customers/{customerId}/transactions`. Accepts path variable `customerId` (UUID), pagination
  parameters (`page`, `size`), and the four optional filter parameters (`fromDate`, `toDate`, `sourceId`,
  `sourceType`). Returns a paginated response body. Validates that `sourceType` (when present) maps to a known
  `TransactionSourceType` value; rejects unrecognised values with a 400 validation error.

* **`GetTransactionHistoryUseCase` (New Application Use-Case interface):** Defines the contract for retrieving a
  paginated, filtered slice of `Transaction` records for a given customer. Accepts a customer identifier, pagination
  parameters, and a structured filter object. Returns a page result carrying the total count and ordered entry list.

* **`GetTransactionHistoryService` (New Application Service):** Implements `GetTransactionHistoryUseCase`.
  Verifies the customer finance account exists (throws a "not found" error when absent). Delegates the filtered,
  paginated query to `TransactionRepository`. Maps the result page to use-case output records.

* **`TransactionRepository` (Finance Domain Repository Port):** Extended with a new query method that accepts a
  customer reference, a `Specification`-compatible filter predicate, and pagination/sort parameters. Returns a page of
  `Transaction` domain objects ordered by `recordedAt` descending.

* **`TransactionRepositoryAdapter` (Finance Infrastructure Adapter):** Implements the extended repository port. Uses
  the `specification-arg-resolver`-produced `Specification` to build a dynamic JPA predicate. Applies a mandatory
  filter restricting results to sub-ledgers `CUSTOMER_WALLET` and `CUSTOMER_HOLD` (customer-owned entries only). No
  system-account sub-ledger entries are ever returned.

* **`TransactionHistoryFilter` (New Value Object — Application/Web Layer):** Carries the four optional filter fields
  resolved from HTTP query parameters. Each field is nullable / optional; absent fields impose no constraint.
  `sourceType` is typed as `TransactionSourceType` (enum) so framework validation rejects unrecognised string values
  before the service layer is reached.

* **`TransactionHistoryResponse` / `TransactionEntryDto` (New Response DTOs):** Represent the paginated response.
  `TransactionEntryDto` exposes: sub-ledger name (`LedgerType`), amount, direction (`EntryDirection`), transaction type
  (`TransactionType`), `recordedAt` timestamp, and optional metadata (`paymentMethod`, `reason`).

---

## 3. Abstract Data Schema Changes

* **Entity: `Transaction`**
    * **Attributes Added/Modified:** No schema changes required. The `Transaction` aggregate already holds `sourceType`
      (nullable enum), `sourceId` (nullable string), `recordedAt` (timestamp), `type`, `paymentMethod`, and `reason`.
      The `TransactionRecord` children already hold `subLedgerRef`, `ledgerType`, `direction`, and `amount`.

* **Indexing:**
    * Add a composite index on `transaction(customer_id, recorded_at DESC)` to satisfy the reverse-chronological
      pagination performance requirement (first page ≤ 2 seconds for 10 000 rows).
    * Consider a partial index on `transaction(customer_id, source_type, recorded_at DESC)` if `sourceType` filter
      usage is expected to be high-volume.

---

## 4. Component Contracts & Payloads

* **Interaction: `External HTTP Client` → `AccountQueryController`**
    * **Protocol:** REST / HTTP GET
    * **Request:**
      `GET /api/finance/customers/{customerId}/transactions?page=0&size=10&fromDate=2026-01-01&toDate=2026-03-31&sourceId=<uuid>&sourceType=RENTAL`
    * **Path variable:** `customerId` (UUID) — identifies the customer whose history is requested.
    * **Optional query parameters:** `fromDate` (date, ISO-8601 `yyyy-MM-dd`), `toDate` (date, ISO-8601 `yyyy-MM-dd`),
      `sourceId` (string, exact match), `sourceType` (enum string — currently `RENTAL`).
    * **Payload Changes (Response):**
      ```
      {
        "totalElements": 25,
        "totalPages": 3,
        "page": 0,
        "size": 10,
        "entries": [
          {
            "subLedger": "CUSTOMER_WALLET",
            "amount": { "value": 50.00, "currency": "EUR" },
            "direction": "CREDIT",
            "type": "DEPOSIT",
            "recordedAt": "2026-03-15T10:30:00Z",
            "paymentMethod": "CASH",
            "reason": null
          }
        ]
      }
      ```
    * **Error Responses:**
        * `404` — customer finance account not found; `errorCode: RESOURCE_NOT_FOUND`.
        * `400` — invalid `customerId` UUID format or unrecognised `sourceType` value; `errorCode: CONSTRAINT_VIOLATION`
          with an `errors` array identifying the offending field.

* **Interaction: `AccountQueryController` → `GetTransactionHistoryUseCase`**
    * **Protocol:** In-process synchronous call
    * **Payload Changes:** Passes `customerId` (UUID), `TransactionHistoryFilter` (with up to four optional fields),
      and `Pageable` (page number, page size, fixed sort `recordedAt DESC`).

* **Interaction: `GetTransactionHistoryService` → `TransactionRepository`**
    * **Protocol:** In-process synchronous call (JPA via repository port)
    * **Payload Changes:** Passes `CustomerRef`, a composed `Specification<TransactionJpaEntity>` predicate (built from
      the filter), and `Pageable`. Returns `Page<Transaction>`.

---

## 5. Updated Interaction Sequence

### Happy Path — Paginated Query with Optional Filters

1. External client sends `GET /api/finance/customers/{customerId}/transactions` with pagination and any combination of
   filter parameters.
2. `AccountQueryController` validates `customerId` format (UUID) and, if `sourceType` is present, validates it maps to
   a known `TransactionSourceType` enum value. Returns `400` immediately on failure.
3. `AccountQueryController` constructs a `TransactionHistoryFilter` from the validated query parameters and calls
   `GetTransactionHistoryUseCase.execute(customerId, filter, pageable)`.
4. `GetTransactionHistoryService` calls `TransactionRepository.findByCustomer(...)` to confirm the customer finance
   account exists. Returns `404` (`ResourceNotFoundException`) if absent.
5. `GetTransactionHistoryService` calls
   `TransactionRepository.findTransactionHistory(CustomerRef, Specification, Pageable)`.
6. `TransactionRepositoryAdapter` combines the mandatory customer-sub-ledger predicate (restrict to `CUSTOMER_WALLET`
   and `CUSTOMER_HOLD`) with each non-null filter predicate using AND, then executes the JPA query with `ORDER BY
   recorded_at DESC`.
7. The adapter returns a `Page<Transaction>` to the service.
8. `GetTransactionHistoryService` maps each `Transaction` and its `TransactionRecord` children to use-case output
   records and returns the page result.
9. `AccountQueryController` maps the result to `TransactionHistoryResponse` and returns `200 OK`.

### Unhappy Path — Unknown Customer

1–3. Same as above.

4. `GetTransactionHistoryService` determines no finance account exists for the given `customerId`.
5. Throws `ResourceNotFoundException("CustomerAccount", customerId)`.
6. `CoreExceptionHandlerAdvice` maps this to a `404 ProblemDetail` with `errorCode: RESOURCE_NOT_FOUND` and
   `correlationId`.

### Unhappy Path — Invalid sourceType Value

1. External client sends `sourceType=UNKNOWN_VALUE`.
2. `AccountQueryController` (or bound `TransactionHistoryFilter` via framework validation) fails enum conversion.
3. Returns `400 ProblemDetail` with `errorCode: CONSTRAINT_VIOLATION` and an `errors` array identifying `sourceType` as
   the invalid field.

### Happy Path — No Filters Supplied

1. External client sends the request with only `customerId`, `page`, and `size`.
2. `TransactionHistoryFilter` carries all-null fields; the repository applies no additional predicates beyond the
   mandatory customer sub-ledger scope.
3. Full history is returned paginated in reverse-chronological order.

---

## 6. Non-Functional Architecture Decisions

* **Security & Auth:** The endpoint must be accessible only to authenticated users with `STAFF` or `ADMIN` role. No
  cross-customer access: the `customerId` path variable is the enforced scope; the service never returns entries
  belonging to a different customer. As the system currently has no active `SecurityFilterChain`, role enforcement is
  noted as a prerequisite; access control must be activated before this endpoint is exposed in production.

* **Scale & Performance:** The query path is read-only and non-mutating; no locking is required. A composite database
  index on `(customer_id, recorded_at DESC)` is mandatory to satisfy the ≤ 2-second SLA for the first page with up to
  10 000 entries. Pagination is enforced at the repository layer (no unbounded fetches). No caching is applied to this
  endpoint — the history is expected to change frequently.
