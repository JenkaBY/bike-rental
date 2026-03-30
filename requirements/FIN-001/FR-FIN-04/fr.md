# User Story: FR-FIN-04 - Manual Balance Adjustment

## 1. Description

**As an** admin
**I want to** apply a positive or negative manual adjustment to a customer's wallet
**So that** I can correct balance discrepancies that have no corresponding real-world transaction

## 3. Non-Functional Requirements (NFRs)

* **Performance:** Adjustment must persist and confirm within 2 seconds.
* **Security/Compliance:** Only admin-level users may perform adjustments. Every adjustment, including reason, operator
  ID, amount, and direction, must be persisted as an auditable journal entry.
* **Usability/Other:** The reason field is mandatory and must not be blank; the UI/API must enforce this before
  submission.

## 2. Context & Business Rules

* **Trigger:** An admin initiates a manual balance correction for a specific customer.
* **Rules Enforced:**
    * Amount must be non-zero (positive for top-up, negative for deduction).
    * A reason text is mandatory; empty or blank reasons are rejected.
    * The adjustment is recorded as a balanced double-entry journal:
        * Top-up: `ADJUSTMENT` (debit) → `CUSTOMER_WALLET` (credit).
        * Deduction: `CUSTOMER_WALLET` (debit) → `ADJUSTMENT` (credit).
    * A deduction must not reduce the customer's `CUSTOMER_WALLET` below zero; on-hold funds (`CUSTOMER_HOLD`) are
      unaffected.
    * Transaction type is `ADJUSTMENT`.
    * Adjustment is always a separate operation — it cannot be combined with a deposit, withdrawal, or rental
      settlement.

## 4. Acceptance Criteria (BDD)

**Scenario 1: Successful top-up adjustment**

* **Given** a customer with a `CUSTOMER_WALLET` balance of €40
* **When** an admin applies a +€10 adjustment with reason "Compensation for system error"
* **Then** a journal entry is created: `ADJUSTMENT` (debit €10) / `CUSTOMER_WALLET` (credit €10)
* **And** the customer's `CUSTOMER_WALLET` balance becomes €50
* **And** the transaction is stored with type `ADJUSTMENT` and the provided reason

**Scenario 2: Successful deduction adjustment**

* **Given** a customer with a `CUSTOMER_WALLET` balance of €40 and no funds on hold
* **When** an admin applies a -€15 adjustment with reason "Overcharge correction"
* **Then** a journal entry is created: `CUSTOMER_WALLET` (debit €15) / `ADJUSTMENT` (credit €15)
* **And** the customer's `CUSTOMER_WALLET` balance becomes €25

**Scenario 3: Deduction rejected when wallet balance would go negative**

* **Given** a customer with a `CUSTOMER_WALLET` balance of €10
* **When** an admin attempts a -€20 deduction
* **Then** the operation is rejected with an "insufficient balance" error
* **And** no journal entry is created

**Scenario 4: Adjustment rejected when reason is blank**

* **Given** a valid customer account
* **When** an admin submits an adjustment with an empty reason
* **Then** the operation is rejected with a validation error
* **And** no journal entry is created

## 5. Out of Scope

* Adjustments targeting `CUSTOMER_HOLD` directly.
* Bulk adjustments across multiple customers in a single operation.
* Admin approval workflows or four-eyes checks before an adjustment is applied.

## 6. Essential Sense & Accounting Impact (examples)

Purpose — essential sense

- Manual balance adjustment lets operators change a customer’s wallet (credit or debit) for business reasons (
  compensation, correction, chargeback), and deposits let customers add funds. Both must produce a single, auditable
  transaction record: `transactionId`, `walletBalance`, `recordedAt`.

Step‑by‑step examples (real numbers, simplified double‑entry view)

- Conventions used below:
    - Customer wallet = liability on system books (we owe the customer these funds).
    - Cash/Bank = asset.
    - Adjustment offset = an accounting account used to keep entries balanced (could be company cash, expense, revenue,
      or an internal reserve).

Example 1 — Customer deposit (safe, real money):

- Before:
    - Customer wallet = 40.00
    - Cash/Bank = 1,000.00
- Action: POST /api/finance/deposits with amount = 50.00 (paymentMethod = CASH)
- Double‑entry:
    - Debit Cash/Bank +50.00
    - Credit Customer wallet +50.00
- After:
    - Customer wallet = 90.00
    - Cash/Bank = 1,050.00
- Response: { transactionId: ..., walletBalance: 90.00, recordedAt: ... }
- Notes: money came from an external source (cash/bank); total system assets increased accordingly.

Example 2 — Manual credit adjustment (must be offset to avoid “creating money”):

- Before:
    - Customer wallet = 40.00
    - Internal Adjustment Reserve = 0.00
- Action: operator applies +10.00 adjustment
- Unsafe (bad) implementation:
    - Credit Customer wallet +10.00 (no offset) → Customer wallet = 50.00
    - Problem: no asset was debited → money effectively created from nothing.
- Safe implementation (recommended):
    - Debit Internal Adjustment Expense (or Company Cash) -10.00
    - Credit Customer wallet +10.00
- After (safe):
    - Customer wallet = 50.00
    - Internal Adjustment Expense (P&L) or Company Cash decreased by 10.00 (depends on chosen offset)
- Response: { transactionId: ..., walletBalance: 50.00, recordedAt: ... }

Example 3 — Manual debit adjustment (deduction):

- Before:
    - Customer wallet = 50.00
- Action: operator applies −20.00 adjustment
- Validation: check `subLedger.isSufficientBalance(Money.of(20.00))` → allowed if wallet >= 20.00
- Double‑entry:
    - Debit Customer wallet −20.00
    - Credit Revenue/Adjustment Offset +20.00 (or Company Cash if moving funds)
- After:
    - Customer wallet = 30.00
- Response: { transactionId: ..., walletBalance: 30.00, recordedAt: ... }
- If insufficient: service returns 422 Unprocessable Content with `finance.insufficient_balance`.

Key mechanisms already in place (from codebase)

- Idempotency key: prevents duplicate transactions from repeat requests.
- `transactionId` + `recordedAt`: immutable audit trail entry per action.
- `Money` value object + `isSufficientBalance()` checks: avoids negative balances unless allowed.
- Validation annotations and controller tests: guard request fields (reason, operatorId, amounts).
- Mapper & DTO changes to return unified `TransactionResponse` so both flows expose the same audit data.

How to ensure money is NOT created from nothing (practical controls)

- Require an offset account for every adjustment (enforce in service layer):
    - For deposits: `paymentMethod` must be present and mapped to an asset (cash/bank) entry.
    - For adjustments: require explicit offset account or force `operatorId` + `reason` + approval workflow; create a
      double‑entry offset (company expense, reserve or cash).
- Authorization & limits:
    - Only allow adjustments for users with specific roles; require approvals above thresholds.
- Audit + immutable records:
    - Record operator, reason, idempotencyKey, transactionId, recordedAt; persist full before/after balances and ledger
      entries.
- Reconciliation:
    - Daily job to reconcile cash/bank vs. sum of deposits/adjustments; flag discrepancies.
- Monitoring & alerts:
    - Alert on unusual patterns (many credits, large adjustments, sudden net increase in customer wallets).
- Tests:
    - Unit/component tests that assert accounting conservation: sum(assets) == sum(liabilities + equity) before and
      after any transaction when offsets are applied.
    - Negative tests: attempt an adjustment without an offset -> expect rejection or logged exception.

Practical recommendations (next steps you can take)

- Enforce offset requirement in `ApplyAdjustmentService`: require an `offsetAccount` or map adjustments to a predefined
  internal account. Fail fast if no valid offset.
- Add approval metadata (optional): `approvedBy`, `approvalTimestamp` for adjustments above threshold.
- Add reconciliation test that simulates a sequence of deposits/adjustments and asserts global ledger balance
  conservation.
- Add monitoring rule for large or frequent manual credits.

Short answer to your concern (“money can be spawned from nothing”)

- It can happen only if adjustments simply credit customer wallets without a corresponding debit. Prevent it by
  requiring an offset account (double‑entry), enforcing role/approval constraints, and running reconciliation checks.
  With those controls, every adjustment is a traced movement of value in the books, not free creation.

