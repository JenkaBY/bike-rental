# User Story: FR-FIN-11 - Account-Type-Aware Sub-Ledger Debit/Credit Semantics

## 1. Description

**As a** system maintainer
**I want to** have sub-ledger balance arithmetic follow standard double-entry accounting rules per account category
**So that** system asset balances (CASH, CARD_TERMINAL, BANK_TRANSFER) correctly reflect real-world money flows and the
balance sheet always satisfies Assets = Liabilities + Income

## 2. Context & Business Rules

* **Trigger:** Identified discrepancy between the double-entry lifecycle specification and the current `SubLedger`
  implementation. All sub-ledgers currently use uniform arithmetic (`debit()` = subtract, `credit()` = add) regardless
  of account category. This is correct for liability accounts but inverted for asset accounts, producing negative asset
  balances after deposits.

* **Rules Enforced:**
    - Standard T-account rule: for **asset** sub-ledgers, DEBIT increases the balance, CREDIT decreases it.
    - Standard T-account rule: for **liability** and **income** sub-ledgers, CREDIT increases the balance, DEBIT
      decreases it.
    - Account categories are fixed and must be derivable from `LedgerType`:
        - **Asset:** `CASH`, `CARD_TERMINAL`, `BANK_TRANSFER`
        - **Liability:** `CUSTOMER_WALLET`, `CUSTOMER_HOLD`
        - **Income:** `REVENUE`
        - **Control (neutral/liability-style):** `ADJUSTMENT`
    - The `EntryDirection` recorded on `TransactionRecord` must remain `DEBIT` for the asset side of a deposit and
      `CREDIT` for the asset side of a withdrawal — matching the lifecycle specification exactly.
    - Calling `debit()` on a system asset sub-ledger must **increase** the balance (not decrease it).
    - Calling `credit()` on a system asset sub-ledger must **decrease** the balance (not increase it).
    - The `isSystemLedger()` bypass for insufficient-balance checks remains unchanged — system sub-ledgers may go
      negative if real-world asset falls below zero (e.g. overdraft scenario).

* **Expected balance progression example (CASH starting at 0):**
    - After a €100 deposit: CASH = **+100** (DEBIT CASH → asset increases)
    - After a €25 withdrawal: CASH = **+75** (CREDIT CASH → asset decreases)
    - Net CASH = +75 matches the €75 retained in the drawer from `double-entry-lifecycle.md`

## 3. Non-Functional Requirements (NFRs)

* **Performance:** No impact — balance arithmetic is in-memory.
* **Security/Compliance:** All existing `TransactionRecord` `EntryDirection` values (`DEBIT`/`CREDIT`) must retain
  their correct semantic meaning as specified in the lifecycle document; no data migration is required for new
  deployments.
* **Usability/Other:** The change must not alter any public API contracts, DTOs, or response formats.

## 4. Acceptance Criteria (BDD)

**Scenario 1: Deposit increases asset sub-ledger balance**

* **Given** the system account CASH sub-ledger has a balance of 0.00
* **When** a cash deposit of €50 is recorded
* **Then** the CASH sub-ledger balance is +50.00
* **And** the transaction record for CASH carries entry direction DEBIT

**Scenario 2: Withdrawal decreases asset sub-ledger balance**

* **Given** the system account CASH sub-ledger has a balance of 50.00 (after a prior deposit)
* **When** a cash withdrawal of €30 is recorded
* **Then** the CASH sub-ledger balance is +20.00
* **And** the transaction record for CASH carries entry direction CREDIT

**Scenario 3: Full lifecycle balance sheet holds**

* **Given** a €100 deposit, a rental capturing €75, and a €25 withdrawal are all recorded
* **Then** CASH = +75, REVENUE = +75, CUSTOMER_WALLET = 0, CUSTOMER_HOLD = 0
* **And** Assets (+75) = Income (+75) ✅

**Scenario 4: Liability sub-ledger semantics unchanged**

* **Given** the customer wallet has a balance of 50.00
* **When** a debit of €30 is applied to CUSTOMER_WALLET
* **Then** the wallet balance is 20.00 (debit still decreases a liability account)

**Scenario 5: Existing deposits and withdrawals produce correct balances end-to-end**

* **Given** the component test suite for deposits and withdrawals is executed
* **Then** all scenarios pass with asset sub-ledger balances matching the lifecycle specification values

## 5. Out of Scope

* Changes to the `EntryDirection` enum values or their names.
* Changes to `TransactionRecord` storage schema or existing persisted data.
* Any alteration to customer (liability) sub-ledger behaviour — only system (asset) sub-ledger arithmetic changes.
* `REVENUE` and `ADJUSTMENT` reclassification — they continue to follow liability-style arithmetic (credit increases).
* UI, API contract, or DTO changes.
