# System Design: FR-FIN-11 - Account-Type-Aware Sub-Ledger Debit/Credit Semantics

## 1. Architectural Overview

FR-FIN-11 is a targeted domain model correction with no externally visible API or schema changes. The `SubLedger`
aggregate, the sole carrier of balance state in the `finance` module, currently uses uniform arithmetic for all ledger
types: `debit()` always subtracts and `credit()` always adds. This is correct for liability sub-ledgers
(`CUSTOMER_WALLET`, `CUSTOMER_HOLD`) but inverted for asset sub-ledgers (`CASH`, `CARD_TERMINAL`, `BANK_TRANSFER`),
where DEBIT must *increase* and CREDIT must *decrease* per standard T-account rules.

The fix is localised to a single domain entity: `SubLedger` is made aware of the account category of its `LedgerType`
and adjusts balance arithmetic accordingly, while the `EntryDirection` stored on each `TransactionRecord` remains
semantically correct. `LedgerType` gains an `isAssetLedger()` predicate (symmetric to the existing
`isSystemLedger()`). No service layer, no API contract, no database schema, and no Liquibase changeset is affected.
Component test assertions for system sub-ledger balances are updated to match the now-correct positive asset values.

---

## 2. Impacted Components

* **`LedgerType` (finance domain model — enum):** Gains a new predicate `isAssetLedger()` that returns `true` for
  `CASH`, `CARD_TERMINAL`, and `BANK_TRANSFER`. This isolates account-category knowledge inside the enum, keeping
  `SubLedger` free of hard-coded type comparisons.

* **`SubLedger` (finance domain model — aggregate member):** `debit()` and `credit()` are updated so that when
  `ledgerType.isAssetLedger()` is true, the arithmetic is reversed:
    - `debit()` on an asset sub-ledger: `balance += amount` (asset increases).
    - `credit()` on an asset sub-ledger: `balance -= amount` (asset decreases; no balance-sufficiency guard skipped —
      system ledgers can go negative as before).
    - Liability and income sub-ledgers retain existing arithmetic unchanged.
    - The `EntryDirection` tag returned by each method (`DEBIT` / `CREDIT`) is unchanged — it describes the
      accounting direction, not the arithmetic sign.

* **`withdrawals.feature` (component test — finance):** The system sub-ledger balance assertion in Scenario 1
  (`<subLedger> | ACC_S | <ledgerType> | 1 | 30`) must change to `-30` for `CASH` and equivalent asset types,
  since a withdrawal CREDITs the asset — decreasing it from `0` to `-30` in the isolated test scenario that has
  no prior deposit. *(Note: in a realistic lifecycle the balance would be positive after deposits; the test starts
  at 0 for isolation.)*

* **`deposits.feature` (component test — finance):** The system sub-ledger balance assertion (`balance = -50`)
  already expects a negative value because the current broken code subtracts on deposit. After the fix, DEBIT on an
  asset sub-ledger *adds*, so the expected balance changes to `50` (positive).

* **`transfers.feature` (component test — finance):** Same class of assertion as `deposits.feature` — the CASH
  balance after a cash deposit currently asserts `-<amount>`; this must be updated to `<amount>` (positive).

---

## 3. Abstract Data Schema Changes

None. No new tables, columns, indexes, or Liquibase changesets are required. The `balance` column on
`finance_sub_ledgers` already stores signed `NUMERIC` values; only the sign of the stored value changes for asset
rows after this fix.

---

## 4. Component Contracts & Payloads

No API contract, DTO, event payload, or Facade interface is altered. This is a pure domain internal change.

---

## 5. Updated Interaction Sequence

The sequences for Deposit and Withdrawal are unchanged structurally. Only the in-memory balance arithmetic inside
`SubLedger` changes. Shown below for the Deposit case; Withdrawal is symmetric:

**Deposit — CASH payment method (corrected balance direction)**

1. `DepositCommandController` receives `POST /api/finance/deposits` and delegates to `RecordDepositUseCase`.
2. `RecordDepositService` loads `CustomerAccount` and `SystemAccount` via `AccountRepository`.
3. `RecordDepositService` calls `systemAccount.getSubLedger(CASH).debit(amount)`.
4. `SubLedger.debit()` detects `CASH.isAssetLedger() == true` → `balance += amount` (was: `balance -= amount`).
   Returns `TransactionRecordWithoutId` tagged with `EntryDirection.DEBIT`.
5. `RecordDepositService` calls `customerAccount.getWallet().credit(amount)`.
6. `SubLedger.credit()` detects `CUSTOMER_WALLET.isAssetLedger() == false` → `balance += amount` (unchanged).
   Returns `TransactionRecordWithoutId` tagged with `EntryDirection.CREDIT`.
7. Both accounts are saved; `Transaction` with two `TransactionRecord` rows is persisted.
8. CASH sub-ledger balance in DB: **+50** (was: -50). `EntryDirection` label: `DEBIT` (unchanged).

**Withdrawal — CASH payout method (corrected balance direction)**

1–3. Identical to deposit flow up to sub-ledger resolution.

4. `RecordWithdrawalService` calls `customerAccount.getWallet().debit(amount)`.
5. `SubLedger.debit()` detects `CUSTOMER_WALLET.isAssetLedger() == false` → `balance -= amount` (unchanged).
6. `RecordWithdrawalService` calls `systemAccount.getSubLedger(CASH).credit(amount)`.
7. `SubLedger.credit()` detects `CASH.isAssetLedger() == true` → `balance -= amount` (was: `balance += amount`).
   Returns `TransactionRecordWithoutId` tagged with `EntryDirection.CREDIT`.
8. CASH sub-ledger balance in DB: **-30** in isolated test (0 → -30); **+75** in full lifecycle example.
   `EntryDirection`
   label: `CREDIT` (unchanged).

---

## 6. Non-Functional Architecture Decisions

* **Security & Auth:** No change. No new data surfaces or access boundaries are introduced.
* **Scale & Performance:** No change. The fix is two conditional branches inside an in-memory method; zero I/O
  overhead.
* **Test coverage:** All existing unit tests for `SubLedger`, `RecordDepositService`, and `RecordWithdrawalService`
  must have their balance-value expectations flipped for asset-type sub-ledgers. Component test feature files
  (`deposits.feature`, `transfers.feature`, `withdrawals.feature`) must update their system sub-ledger balance
  column assertions accordingly.
