# System Design: FR-FIN-13 - Retrieve Customer Account Balances

## 1. Architectural Overview

FR-FIN-13 adds a read-only query capability to the Finance module: retrieving a customer's current ledger balance
breakdown via a dedicated REST endpoint. No new domain aggregates, no new transaction types, and no cross-module
dependencies are introduced.

The change is additive. A new `AccountQueryController` is wired to a new `GetCustomerAccountBalancesUseCase` / service
pair that delegates to the existing `AccountRepository.findByCustomerId()`. The only data-model change is the addition
of an `updatedAt` timestamp to `SubLedger` — required to satisfy the `lastUpdatedAt` response field. The response
returns `walletBalance` and `holdBalance` sourced directly from the two customer-owned sub-ledgers, and `lastUpdatedAt`
as the later of the two sub-ledger timestamps. Because `CustomerAccount` is always created together with the customer
record, `lastUpdatedAt` is guaranteed non-null.

---

## 2. Impacted Components

* **`AccountQueryController` (new — finance module `web/query/`):** Exposes
  `GET /api/finance/customers/{customerId}/balances`. Validates that `customerId` is a well-formed UUID (path
  variable constraint), delegates to `GetCustomerAccountBalancesUseCase`, and maps the result to
  `CustomerAccountBalancesResponse`. Returns `200 OK` on success; error handling falls through to the
  module-scoped `@RestControllerAdvice`.

* **`GetCustomerAccountBalancesUseCase` (new — finance module `application/usecase/`):** Read-only use-case
  interface declaring `execute(UUID customerId) : CustomerAccountBalances`. Accepts the raw customer UUID and
  returns a plain result type carrying the three response fields.

* **`GetCustomerAccountBalancesService` (new — finance module `application/service/`):** Implements
  `GetCustomerAccountBalancesUseCase`. Calls `AccountRepository.findByCustomerId(CustomerRef)`, throws
  `ResourceNotFoundException` when absent, then reads `wallet.getBalance()`, `onHold.getBalance()`, and
  computes `lastUpdatedAt` as the later of `wallet.getUpdatedAt()` and `onHold.getUpdatedAt()`. The operation
  is read-only — no `@Transactional` write boundary is required; a read-only transaction hint is sufficient.

* **`AccountRepository` (existing — `finance/domain/repository/`, unmodified):** The existing
  `findByCustomerId(CustomerRef)` method is consumed without change.

* **`SubLedger` (existing — `finance/domain/model/`, extended):** Gains one new field:
  `updatedAt` (`Instant`) — the timestamp of the last balance mutation (set at creation and updated on every
  `credit()` / `debit()` call). `CreateCustomerAccountService` must populate `updatedAt` to the account-creation
  instant when constructing new `SubLedger` instances, ensuring the field is always non-null.

* **`SubLedgerJpaEntity` (existing — `finance/infrastructure/persistence/entity/`, extended):** Gains the
  corresponding `updated_at` column mapping. The JPA mapper must propagate the `updatedAt` field in both
  domain→JPA and JPA→domain directions.

* **`bike-rental-db` (data store):** Receives one DDL change via a new Liquibase changeset: an `updated_at`
  column (Timestamp with timezone, not-null) on `finance_sub_ledgers`. The migration must backfill existing
  rows with `NOW()`.

---

## 3. Abstract Data Schema Changes

* **Entity: `SubLedger`** (existing table `finance_sub_ledgers`)
    * **Attributes Added:**
        * `updated_at` (Timestamp with timezone, not-null) — set on initial insert by
          `CreateCustomerAccountService`; updated on every `credit()` / `debit()` call inside the domain object.
    * **Migration note:** The Liquibase changeset must add the column with a `DEFAULT NOW()` clause so that all
      pre-existing rows receive a non-null value during the migration.

* **Relations:** No new relations. All reads traverse the existing `Account` → `SubLedger` one-to-many
  relationship already mapped in the persistence layer.

---

## 4. Component Contracts & Payloads

* **Interaction: External HTTP client → `AccountQueryController`**
    * **Protocol:** REST — `GET /api/finance/customers/{customerId}/balances`
    * **Path Variable:** `customerId` (UUID, validated — `400` on malformed UUID)
    * **Success Response:** `200 OK`
        ```
        {
          walletBalance:  Decimal (scale 2, e.g. 120.00)
          holdBalance:    Decimal (scale 2, e.g. 30.00)
          lastUpdatedAt:  Timestamp (UTC ISO-8601, non-null)
        }
        ```
    * **Error Responses:**
        * `400 Bad Request` (`CONSTRAINT_VIOLATION`) — `customerId` is not a valid UUID.
        * `404 Not Found` (`RESOURCE_NOT_FOUND`) — no `CustomerAccount` exists for the given UUID.

* **Interaction: `GetCustomerAccountBalancesService` → `AccountRepository`**
    * **Protocol:** In-process (read-only transaction)
    * **Payload Changes:** Consumes `findByCustomerId(CustomerRef)` — no signature change.

---

## 5. Updated Interaction Sequence

### Happy path — customer account found

1. HTTP client calls `GET /api/finance/customers/{customerId}/balances`.
2. `AccountQueryController` validates `customerId` format; rejects with `400` if malformed.
3. `AccountQueryController` calls `GetCustomerAccountBalancesUseCase.execute(customerId)`.
4. `GetCustomerAccountBalancesService` wraps the raw UUID in a `CustomerRef` and calls
   `AccountRepository.findByCustomerId(customerRef)`.
5. `AccountRepository` returns `Optional<CustomerAccount>`; service throws `ResourceNotFoundException` if empty
   → `404` returned to client.
6. Service reads `walletBalance` from `customerAccount.getWallet().getBalance()`.
7. Service reads `holdBalance` from `customerAccount.getOnHold().getBalance()`.
8. Service computes `lastUpdatedAt` as `max(wallet.getUpdatedAt(), onHold.getUpdatedAt())`.
9. Service returns `CustomerAccountBalances(walletBalance, holdBalance, lastUpdatedAt)`.
10. `AccountQueryController` maps the result to `CustomerAccountBalancesResponse` and returns `200 OK`.

### Unhappy path — customer account not found

1–4. Same as above.

5. `AccountRepository` returns `Optional.empty()`.
6. `GetCustomerAccountBalancesService` throws `ResourceNotFoundException`.
7. Module-scoped `@RestControllerAdvice` maps the exception to `404 Not Found` with `ProblemDetail`
   (`errorCode: RESOURCE_NOT_FOUND`, `correlationId` from MDC).

### Unhappy path — invalid UUID format

1. HTTP client calls `GET /api/finance/customers/abc/balances`.
2. `AccountQueryController` path-variable constraint violation is raised before reaching the service.
3. `CoreExceptionHandlerAdvice` maps the violation to `400 Bad Request` with `ProblemDetail`
   (`errorCode: CONSTRAINT_VIOLATION`).

---

## 6. Non-Functional Architecture Decisions

* **Security & Auth:** The endpoint is read-only and follows the same open-access posture as all current
  `/api/**` routes (no `SecurityFilterChain` is active). When the OAuth2/Security layer is activated in a
  future sprint, this endpoint should require at minimum a valid staff-role token; no customer-owned data
  mutation can occur via this endpoint.

* **Scale & Performance:** The operation performs a single indexed read on `finance_accounts` joined to
  `finance_sub_ledgers` (two rows per customer account). No caching is introduced at this stage; the
  read latency is expected to remain well within the 2-second NFR under normal PostgreSQL load. The
  `customer_id` index on the accounts table (already present from FR-FIN-01) ensures O(1) lookup.
