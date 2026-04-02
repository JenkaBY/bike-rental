# System Design: FR-FIN-06 - Rental Hold (Pre-Authorization)

## 1. Architectural Overview

FR-FIN-06 introduces the internal hold operation that freezes a customer's planned rental cost at the time a rental is
created. Unlike the deposit and withdrawal flows, this operation is **never initiated via HTTP** — it is called by the
Rental module through the `FinanceFacade` cross-module interface. There is no new REST controller.

The double-entry journal moves money entirely within the customer's account: `CUSTOMER_WALLET` (debit) →
`CUSTOMER_HOLD` (credit). No system sub-ledger is touched — the hold is a liability restructuring within a single
account entity, consistent with the lifecycle invariant that `HOLD` is the only operation where both sides are `[C]`.

The feature follows the existing `RecordWithdrawalService` pattern: load `CustomerAccount`, guard against insufficient
available balance, apply symmetric `debit` / `credit` calls on sub-ledgers, persist the mutated account, and persist a
`Transaction` with two `TransactionRecord` children. The `rentalRef.id()` (Long) supplied by the Rental module is
stored on the `Transaction` as `sourceId` (with `sourceType = "RENTAL"`); the idempotency key is deterministically
derived as `UuidCreator.getNameBasedMd5(String.valueOf(rentalRef.id()))`, making retried calls intrinsically safe.

---

## 2. Impacted Components

* **`TransactionRef` (new — shared domain value object):** Record in `shared/domain/` package following the
  `CustomerRef` pattern: `record TransactionRef(UUID id)`. Provides a typed wrapper for transaction identifiers
  used at cross-module boundaries.

* **`RentalRef` (new — shared domain value object):** Record in `shared/domain/` package:
  `record RentalRef(Long id)`. Wraps the rental's `Long` identifier for use in Finance domain commands and
  idempotency key derivation.

* **`HoldInfo` (new — finance facade DTO record):** Public result type returned by `FinanceFacade.holdFunds(...)`.
  Lives in the `com.github.jenkaby.bikerental.finance` package (same as the facade interface) so the Rental module
  can reference it across the module boundary. Fields: `transactionRef` (TransactionRef), `recordedAt` (Instant).

* **`FinanceFacade` (existing — interface extended):** Gains a new method:
  ```
  HoldInfo holdFunds(CustomerRef customerRef, RentalRef rentalRef, Money plannedCost)
  ```
  The `rentalRef` is required both for idempotency and audit linkage. Throws `InsufficientBalanceException` (a
  `BikeRentalException`) when available balance is insufficient — the Rental module maps this to a rental-creation
  failure.

* **`FinanceFacadeImpl` (existing — implementation extended):** Depends on new `RentalHoldUseCase`. Maps the facade
  call to `RentalHoldCommand(customerRef, rentalRef, plannedCost)`, calls `RentalHoldUseCase.execute(command)`, and
  maps the `HoldResult` to `HoldInfo`.

* **`TransactionType` (existing domain enum — extended):** Gains `HOLD`. Stored as `VARCHAR` in the
  `finance_transactions` table; no Liquibase changeset required for a new string value.

* **`RentalHoldUseCase` (new — finance module application use-case interface):** Port interface in
  `finance/application/usecase/`. Declares a single `execute(RentalHoldCommand)` operation returning `HoldResult`.

  Command record attributes:
  ```
  RentalHoldCommand(CustomerRef customerRef, RentalRef rentalRef, Money amount)
  ```
  Result record attributes:
  ```
  HoldResult(TransactionRef transactionRef, Instant recordedAt)
  ```

* **`RecordRentalHoldService` (new — finance module application service):** Implements `RentalHoldUseCase`. Runs
  within a single `@Transactional` boundary. Execution order:
    1. **Idempotency check:** derive idempotency key as
       `new IdempotencyKey(UuidCreator.getNameBasedMd5(String.valueOf(command.rentalRef().id())))`, then call
       `transactionRepository.findByIdempotencyKeyAndCustomerId(derivedKey, command.customerRef())`;
       if found, return the stored `HoldResult` unchanged — no balance mutation.
    2. **Account load:** `accountRepository.findByCustomerId(command.customerRef())` — throws
       `ResourceNotFoundException` if absent.
    3. **Balance guard:** `customerAccount.isBalanceSufficient(command.amount())` — throws
       `InsufficientBalanceException(available, amount)` if false.
    4. **Sub-ledger mutation:** call `customerAccount.getWallet().debit(command.amount())` (DEBIT CUSTOMER_WALLET)
       and `customerAccount.getOnHold().credit(command.amount())` (CREDIT CUSTOMER_HOLD).
    5. **Persist account:** `accountRepository.save(customerAccount)`.
    6. **Build transaction:** construct `Transaction` with type `HOLD`, `paymentMethod = null`,
       `sourceType = "RENTAL"`, `sourceId = command.rentalRef().id()` (Long, stored as string or numeric per
       the existing `sourceId` column type), `idempotencyKey = derivedKey` (from step 1),
       `operatorId = null`, and two `TransactionRecord` children from the sub-ledger change results.
    7. **Persist transaction:** `transactionRepository.save(transaction)`.
    8. **Return:** `HoldResult(new TransactionRef(transactionId), now)`.

* **`CustomerAccount` (existing — no changes):** `getWallet()`, `getOnHold()`, and `isBalanceSufficient(Money)` are
  already in place and reused without modification.

* **`AccountRepository` (existing — no interface changes):** `findByCustomerId` is reused; `getSystemAccount` is
  not called — there is no system account side to this operation.

* **`TransactionRepository` (existing — no interface changes):**
  `findByIdempotencyKeyAndCustomerId(IdempotencyKey, CustomerRef)` is reused for idempotency.

---

## 3. Abstract Data Schema Changes

No new tables, columns, or indexes are required. The `transaction_type` column in `finance_transactions` is VARCHAR and
accommodates the new `HOLD` value automatically. The `source_type` and `source_id` columns already exist and accept
`"RENTAL"` / `rentalId` values.

---

## 4. Component Contracts & Payloads

* **Interaction: Rental module → `FinanceFacade`**
    * **Protocol:** in-process Java facade call (Spring Modulith cross-module)
    * **Method:** `HoldInfo holdFunds(CustomerRef customerRef, RentalRef rentalRef, Money plannedCost)`
    * **Success:** returns `HoldInfo(transactionRef, recordedAt)` — Rental module confirms hold was created.
    * **Failure — insufficient balance:** throws `InsufficientBalanceException` — Rental module aborts rental
      creation and surfaces the error to the caller.
    * **Failure — customer account not found:** throws `ResourceNotFoundException` — Rental module treats as a
      fatal configuration error.

---

## 5. Updated Interaction Sequence

```
Rental Module
  └─ FinanceFacade.holdFunds(customerRef, rentalRef, plannedCost)
       └─ FinanceFacadeImpl
            └─ RentalHoldUseCase.execute(RentalHoldCommand)
                 └─ RecordRentalHoldService
                      ├─ derive idempotencyKey = UuidCreator.getNameBasedMd5(String.valueOf(rentalRef.id()))
                      ├─ TransactionRepository.findByIdempotencyKeyAndCustomerId(...)
                      │    └─ existing? → return HoldResult early
                      ├─ AccountRepository.findByCustomerId(customerRef)
                      ├─ customerAccount.isBalanceSufficient(amount)
                      │    └─ false? → throw InsufficientBalanceException
                      ├─ customerAccount.getWallet().debit(amount)      [DEBIT  CUSTOMER_WALLET]
                      ├─ customerAccount.getOnHold().credit(amount)     [CREDIT CUSTOMER_HOLD]
                      ├─ AccountRepository.save(customerAccount)
                      ├─ TransactionRepository.save(Transaction[HOLD])
                      └─ return HoldResult(new TransactionRef(transactionId), now)
```

---

## 6. Non-Functional Architecture Decisions

* **Atomicity:** The entire operation executes inside a single `@Transactional` boundary; no partial freeze is
  possible.
* **Idempotency:** The idempotency key is deterministically derived from the `Long` rental ID via
  `UuidCreator.getNameBasedMd5(String.valueOf(rentalRef.id()))` (MD5-based name UUID, namespace-free). The same
  `rentalId` will always produce the same UUID, making retries from the Rental module intrinsically safe.
* **Auditability:** The `Transaction` row with `source_type = "RENTAL"` and `source_id = rentalRef.id()` provides a
  permanent audit link between the rental and its hold entry — no foreign key is used (cross-module boundary).
* **No HTTP surface:** No new REST controller, DTO, OpenAPI annotation, or `@WebMvcTest` is needed. Test coverage
  is provided by a unit test for `RecordRentalHoldService`.
* **Available balance definition:** Follows the definition established in FR-FIN-05:
  `available = CUSTOMER_WALLET.balance − CUSTOMER_HOLD.balance`. `CustomerAccount.isBalanceSufficient(Money)` already
  encapsulates this calculation.
