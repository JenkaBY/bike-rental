# System Design: FR-FIN-07 - Normal Rental Settlement

## 1. Architectural Overview

FR-FIN-07 implements the settlement leg of the Pre-Authorization (Hold + Capture) payment flow. It is exclusively
triggered by the Rental module — never by an HTTP request — and executes the financial close-out of a completed rental
whose final cost is at or below the previously held amount.

Settlement is modelled as two possible sequential double-entry journal entries within a single atomic transaction
boundary: a mandatory `CAPTURE` entry that moves the earned revenue from `CUSTOMER_HOLD` to `REVENUE`, and a
conditional `RELEASE` entry that returns the unspent surplus from `CUSTOMER_HOLD` back to `CUSTOMER_WALLET`. The
release entry is only created when excess funds exist; a zero-amount release is explicitly forbidden. Both entries
operate across two account entities (customer account and system account), keeping the overall ledger balanced.

`FinanceFacade` gains a new `settleRental` method. When the computed final cost exceeds the held amount the method
immediately throws a typed exception, signalling the Rental module to defer to the over-budget settlement flow
(FR-FIN-08). This single facade method is the sole entry point; `ReturnEquipmentService` in the Rental module
replaces its current `FinanceFacade` call with this new one.

---

## 2. Impacted Components

* **`SettlementInfo` (new — finance facade DTO record):** Public result type returned by
  `FinanceFacade.settleRental(...)`. Placed in the `com.github.jenkaby.bikerental.finance` package (alongside the
  facade) so the Rental module can reference it across the module boundary. Fields: `captureTransactionRef` (
  TransactionRef),
  `releaseTransactionRef` (nullable TransactionRef — null when final cost equals held amount exactly), `recordedAt` (
  Instant).

* **`FinanceFacade` (existing — interface extended):** Gains a new method:
  ```
  SettlementInfo settleRental(CustomerRef customerRef, RentalRef rentalRef, Money finalCost)
  ```
  Throws `InsufficientHoldException` when the hold transaction for the rental cannot be found or the hold balance
  is zero. Throws `OverBudgetSettlementException` (a `BikeRentalException`) when `finalCost > heldAmount` —
  the Rental module treats this signal as a cue to invoke the over-budget path (FR-FIN-08). Both exception
  types are unchecked domain exceptions cross-module boundaries without leaking Finance internals.

* **`FinanceFacadeImpl` (existing — implementation extended):** Depends on the new `SettleRentalUseCase`. Maps
  the facade call to `SettleRentalCommand(customerRef, rentalRef, finalCost)`, delegates execution to
  `SettleRentalUseCase.execute(command)`, and maps the `SettlementResult` to `SettlementInfo`.

* **`SettleRentalUseCase` (new — finance module application use-case interface):** Port interface in
  `finance/application/usecase/`. Declares a single `execute(SettleRentalCommand)` returning `SettlementResult`.

  Command record attributes:
  ```
  SettleRentalCommand(CustomerRef customerRef, RentalRef rentalRef, Money finalCost)
  ```
  Result record attributes:
  ```
  SettlementResult(TransactionRef captureTransactionRef, TransactionRef releaseTransactionRef, Instant recordedAt)
  // releaseTransactionRef is null when no release entry was created
  ```

* **`SettleRentalService` (new — finance module application service):** Implements `SettleRentalUseCase`. Runs
  within a single `@Transactional` boundary. Full execution order specified in Section 5.

* **`TransactionType` (existing domain enum — extended):** Gains two new values: `CAPTURE` and `RELEASE`.
  Stored as `VARCHAR` in `finance_transactions`; no Liquibase changeset required for new string values. All
  switch expressions over `TransactionType` must handle both new values.

* **`TransactionRepository` (existing — interface extended):** Gains one new lookup method:
  ```
  Optional<Transaction> findByRentalRefAndType(RentalRef rentalRef, TransactionType type)
  ```
  Used by `SettleRentalService` to fetch the original `HOLD` transaction for a rental (to read `heldAmount`)
  and also to perform an idempotency check for the `CAPTURE` transaction. The query filters on
  `sourceType = "RENTAL"`, `sourceId = rentalRef.id()`, and `transactionType = type`.

* **`ReturnEquipmentService` (existing — Rental module, updated):** The existing call to
  `FinanceFacade.recordAdditionalPayment(...)` is replaced with `FinanceFacade.settleRental(customerRef,
  rentalRef, finalCost)`. The service propagates `OverBudgetSettlementException` without catching it for
  FR-FIN-07 scope; FR-FIN-08 will add the catch branch and over-budget handling in the same service.

* **`AccountRepository` (existing — no interface changes):** `findByCustomerId` and `getSystemAccount` are
  already available and are reused without modification.

* **`Account` / `SubLedger` (existing — no model changes):** `debit(Money)` and `credit(Money)` are reused on
  `CUSTOMER_HOLD`, `CUSTOMER_WALLET`, and `REVENUE` sub-ledgers.

---

## 3. Abstract Data Schema Changes

No new tables, columns, or indexes are required.

* **Entity: `Transaction`** (table `finance_transactions`)
    * **Attributes Modified:** `transaction_type` column gains two new enum values: `CAPTURE` and `RELEASE`.
      Existing records are unaffected; the column type and width are unchanged.

* **Relations:** Unchanged — the one-to-many relationship between `finance_transactions` and
  `finance_transaction_records` handles both new transaction types as-is.

---

## 4. Component Contracts & Payloads

* **Interaction: `ReturnEquipmentService` → `FinanceFacade`**
    * **Protocol:** In-process Java facade call (Spring Modulith cross-module)
    * **Method:** `SettlementInfo settleRental(CustomerRef customerRef, RentalRef rentalRef, Money finalCost)`
    * **Success:** returns `SettlementInfo(captureTransactionRef, releaseTransactionRef, recordedAt)`.
      `releaseTransactionRef` is `null` when `finalCost == heldAmount` (exact match, no release entry created).
    * **Failure — final cost exceeds hold:** throws `OverBudgetSettlementException` — caller defers to
      FR-FIN-08 flow.


* **Interaction: `SettleRentalService` → `TransactionRepository`**
    * **Protocol:** In-process synchronous (same `@Transactional` context)
    * **Idempotency read:** `findByRentalRefAndType(rentalRef, CAPTURE)` — if a CAPTURE transaction already
      exists for this rental, the settlement was already completed; return the stored result without any
      balance or data mutation.
    * **Hold lookup:** `findByRentalRefAndType(rentalRef, HOLD)` — retrieves the original hold transaction to
      extract `heldAmount` from the credit `TransactionRecord`.

* **Interaction: `SettleRentalService` → `AccountRepository`**
    * **Protocol:** In-process synchronous (same `@Transactional` context)
    * **Loads:** `findByCustomerId(customerRef)` for the customer account; `getSystemAccount()` for the system
      account (needed to resolve `REVENUE` sub-ledger).

---

## 5. Updated Interaction Sequence

**Happy path — settlement with excess (capture + release):**

1. `ReturnEquipmentService` determines all equipment has been returned and calls
   `FinanceFacade.settleRental(customerRef, rentalRef, finalCost)`.
2. `FinanceFacadeImpl` maps to `SettleRentalCommand` and calls `SettleRentalUseCase.execute(command)`.
3. `SettleRentalService` calls `TransactionRepository.findByRentalRefAndType(rentalRef, CAPTURE)` → empty
   (no prior settlement).
4. `SettleRentalService` calls `TransactionRepository.findByRentalRefAndType(rentalRef, HOLD)` → found;
   extracts `heldAmount` from the credit `TransactionRecord` amount.
5. `SettleRentalService` verifies `finalCost ≤ heldAmount` → if `finalCost > heldAmount`, throws
   `OverBudgetSettlementException`; caller propagates to FR-FIN-08 handler.
6. `SettleRentalService` loads `customerAccount` via `AccountRepository.findByCustomerId(customerRef)` →
   `ResourceNotFoundException` if absent.
7. `SettleRentalService` loads `systemAccount` via `AccountRepository.getSystemAccount()`.
8. **Capture entry (mandatory):**
    * `customerAccount.getOnHold().debit(finalCost)` — decrements `CUSTOMER_HOLD`.
    * `systemAccount.getRevenue().credit(finalCost)` — increments `REVENUE`.
9. Compute `excess = heldAmount − finalCost`.
10. **Release entry (conditional — only when `excess > 0`):**
    * `customerAccount.getOnHold().debit(excess)` — decrements `CUSTOMER_HOLD` to zero for this rental.
    * `customerAccount.getWallet().credit(excess)` — returns unspent funds to `CUSTOMER_WALLET`.
11. `AccountRepository.save(customerAccount)` and `AccountRepository.save(systemAccount)`.
12. Construct and persist `Transaction(type=CAPTURE, sourceType="RENTAL", sourceId=rentalRef.id(), …)` with
    two `TransactionRecord` children (CUSTOMER_HOLD debit, REVENUE credit) via `TransactionRepository.save(...)`.
13. If `excess > 0`, construct and persist `Transaction(type=RELEASE, sourceType="RENTAL", sourceId=rentalRef.id(), …)`
    with
    two `TransactionRecord` children (CUSTOMER_HOLD debit, CUSTOMER_WALLET credit) via
    `TransactionRepository.save(...)`.
14. `SettleRentalService` returns
    `SettlementResult(captureTransactionRef, releaseTransactionRef /* null if exact */, now)`.
15. `FinanceFacadeImpl` maps to `SettlementInfo` and returns it to `ReturnEquipmentService`.

**Happy path — exact match (capture only):**

Steps 1–8 are identical. At step 9, `excess = 0`. Step 10 is skipped entirely. Step 13 is skipped. Step 14
returns `SettlementResult(captureTransactionRef, null, now)`.

**Idempotency — duplicate settlement call:**

1–3. As above. Step 3 returns an existing CAPTURE transaction.

4. `SettleRentalService` reads `captureTransactionRef` and `recordedAt` from the existing record.
5. Checks whether a RELEASE transaction also exists for this rentalRef.
6. Returns `SettlementResult(captureTransactionRef, releaseTransactionRef /* null if absent */, existingRecordedAt)`
   without any
   balance mutation or new persistence.

**Error path — over-budget:**

Steps 1–5. Step 5: `finalCost > heldAmount` → `OverBudgetSettlementException` is thrown. No account mutations
or transaction records are created. FR-FIN-08 catch block in `ReturnEquipmentService` handles this case.

---

## 6. Non-Functional Architecture Decisions

* **Atomicity:** The entire settlement — both the capture and release entries — executes inside a single
  `@Transactional` boundary propagated from `SettleRentalService`. If either entry fails, the ORM rolls back
  all changes to both account sub-ledgers and both transaction records, leaving `CUSTOMER_HOLD` at its
  pre-settlement balance.

* **Idempotency:** Settlement idempotency is handled by looking up an existing `CAPTURE` transaction for the
  given `rentalRef` rather than by a client-supplied key. The combination of `sourceType = "RENTAL"` and
  `sourceId = rentalRef.id()` with `transactionType = CAPTURE` is unique per rental, making duplicate facade
  calls safe without extra state on the Rental side.

* **Auditability:** Both `CAPTURE` and `RELEASE` transaction rows carry `sourceType = "RENTAL"` and
  `sourceId = rentalRef.id()`, linking them permanently to their originating rental without requiring a
  foreign key across module boundaries.

* **Zero-amount guard:** The release `TransactionRecord` is never persisted when `excess = 0`. This maintains
  ledger integrity — no zero-amount entries exist — and prevents spurious audit noise.

* **No HTTP surface:** There is no new REST controller, DTO, or OpenAPI annotation for this feature. Test
  coverage is provided by unit tests for `SettleRentalService` (covering both the capture-only scenario and
  the capture + release scenario) and a Cucumber scenario verifying end-to-end settlement via the Rental
  return endpoint.

---

## 7. Exception Handling — Rental Module

This section defines how exceptions thrown by the `FinanceFacade` (or downstream finance services) are handled
inside the Rental module. The goal is consistent error mapping, safe propagation for cross-feature flows
(FR-FIN-08), and useful diagnostics for operators and tests.

- **Exception Types from Finance module**
    - `OverBudgetSettlementException` — thrown when `finalCost > heldAmount`. Semantically a business-level
      rejection that requires Rental flow branching (handled by FR-FIN-08). It is an unchecked `BikeRentalException`
      carrying an error code (`ErrorCodes.OVER_BUDGET_SETTLEMENT`) and a typed `Details` payload (final cost,
      held amount).

- **Service layer (`ReturnEquipmentService`) behaviour**
    - For FR-FIN-07 the service must *not* swallow `OverBudgetSettlementException`. It should allow the exception
      to propagate (so FR-FIN-08 can implement the alternate flow). A concise log line MUST be emitted before
      propagation with `correlationId` from MDC and contextual identifiers (`rentalRef`, `customerRef`).
    - Do not perform automated retries at the service level for these exceptions.

- **Web/controller layer behaviour**
    - Module-scoped `@RestControllerAdvice` (existing pattern) will convert `BikeRentalException` derivatives into
      `ProblemDetail` responses. The handler must include two extra properties on every response:
        - `correlationId` — taken from the MDC (fallback to a freshly generated UUID).
        - `errorCode` — the string constant provided by the exception (e.g. `ErrorCodes.OVER_BUDGET_SETTLEMENT`).
    - For `OverBudgetSettlementException` include the exception `Details` object serialized in the response body
      under `errors` (or a `details` property) so callers and automated tests can assert on `finalCost` vs
      `heldAmount` values.

- **Cross-module signalling and tests**
    - Unit and component tests should assert on the returned `ProblemDetail.errorCode` rather than raw exception
      messages. Cucumber steps (component tests) should provide expected `errorCode` values when asserting failure
      scenarios.
    - Integration tests that exercise settlement idempotency should ensure that repeated `settleRental` calls either
      return an existing `SettlementInfo` or raise no mutations; they must not be interpreted as `OverBudget` unless
      the input `finalCost` genuinely exceeds the held amount.

- **Operational concerns**
    - Emit metrics for settlement failures: counters `finance.settlement.over_budget` and
      `finance.settlement.insufficient_hold` with `rentalRef` and `customerRef` as labels (or as appropriate to your
      metrics cardinality policy).
    - Log at `WARN` for `OverBudgetSettlementException` including
      `correlationId` so traces can be correlated across services and test runs.

This section documents the runtime contract between Rental and Finance when exceptions occur. Implementation of
the FR-FIN-08 over-budget flow will add a catch branch inside `ReturnEquipmentService` to handle
`OverBudgetSettlementException` and transition into that feature's behaviour; until then the exceptions are left
to propagate and be handled by the global error mapping described above.
