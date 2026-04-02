# Initial User Request — FIN-001

## Original Request

As a stakeholder I want to implement a payment intake and processing system for renting bicycles and related
accessories.

The rental module accepts a request to create a rental with multiple types of equipment. This module must check whether
there are sufficient funds for the rental: if there are, the rental is created; if not, an insufficient funds error is
returned.

The accounting system will be entirely internal — no requests to external banking services are needed.

The system must store the user's balance in case the user does not want to take their change. The system should be
flexible and support future evolution. The system must support double-entry accounting for audit.

The current financial module is considered deprecated — everything should be built from scratch.

---

## Clarifications Captured

### Payment Flow

Option B — **Pre-Authorization (Hold + Capture)** selected:

- At rental creation, the estimated cost is frozen (held) in a separate hold ledger.
- At rental return, the actual cost is captured from the hold; excess is released back to the wallet.
- Architecture must allow future migration to an event-driven (Option C) extension.

### Cost Model

Duration is **open-ended** — the customer returns equipment whenever they decide.
At rental creation the expected return time is specified; the **Tariff module** computes the **planned cost** =
tariff rate × expected duration. At rental return, the **Tariff module** computes the **final cost** =
tariff rate × actual duration. The Finance module consumes both outputs but never calculates costs itself.

### Account Types

All ledger accounts used in the system are owned by one of two account entities: a **Customer Account** (one per
customer, created on registration) or the **System Account** (a single, shop-owned account provisioned at startup).

| Account           | Category  | Account Entity   | Description                                                     |
|-------------------|-----------|------------------|-----------------------------------------------------------------|
| `CUSTOMER_WALLET` | Liability | Customer Account | Customer's available spendable balance                          |
| `CUSTOMER_HOLD`   | Liability | Customer Account | Reserved / pre-authorized funds, locked during active rental    |
| `REVENUE`         | Income    | System Account   | Shop's earned income, credited at rental settlement             |
| `CASH`            | Asset     | System Account   | Physical cash held by the shop                                  |
| `CARD_TERMINAL`   | Asset     | System Account   | Funds received via POS card terminal                            |
| `BANK_TRANSFER`   | Asset     | System Account   | Funds received via bank transfer                                |
| `ADJUSTMENT`      | Control   | System Account   | Absorbs manual admin corrections with no real-world counterpart |

### System Account

- Exactly **one** System Account exists; it is provisioned automatically at application startup.
- It owns the three payment-method sub-ledgers — `CASH`, `CARD_TERMINAL`, `BANK_TRANSFER` — plus `REVENUE` and
  `ADJUSTMENT`.
- Every deposit and withdrawal double-entry must reference one of the System Account's payment-method sub-ledgers as
  the asset side. This guarantees no money is created or destroyed — every journal entry has a real source and a real
  destination.
- The System Account has no customer owner and is never tied to a customer identity.

### Deposit Operations

- Staff manually records deposits at the counter.
- Supported payment methods: **cash**, **card terminal (POS)**, and **bank transfer**.
- Double-entry per method:
  - `CASH` (debit) → `CUSTOMER_WALLET` (credit)
  - `CARD_TERMINAL` (debit) → `CUSTOMER_WALLET` (credit)
  - `BANK_TRANSFER` (debit) → `CUSTOMER_WALLET` (credit)
- Transaction type: `DEPOSIT` with a payment-method attribute.
- Admin can perform manual balance adjustments (positive or negative) with a mandatory reason.
  - Double-entry: `ADJUSTMENT` ↔ `CUSTOMER_WALLET` (direction depends on sign).
- Deposit, refund, and rental are always separate operations.

### Withdrawal Operations

- Staff records the withdrawal at the counter and selects the payout method (cash or cashless).
- Partial withdrawals are allowed: any positive amount up to the available balance.
- Only available (non-held) funds can be withdrawn — on-hold balance is excluded.
- Double-entry per method:
  - `CUSTOMER_WALLET` (debit) → `CASH` (credit)
  - `CUSTOMER_WALLET` (debit) → `CARD_TERMINAL` (credit)
  - `CUSTOMER_WALLET` (debit) → `BANK_TRANSFER` (credit)
- Transaction type: `WITHDRAWAL`.

### Overtime Edge Case (FR-FIN-06)

If the actual rental cost exceeds the held amount:

1. Capture the entire hold.
2. Attempt to debit the remainder from the customer's available balance.
3. If the available balance is also insufficient, capture what is available and flag the rental with a `DEBT` status for
   manual resolution by staff.

### Partial Equipment Return

- A customer who rented multiple items can return them one at a time.
- Rental status remains `ACTIVE` throughout all partial returns — no new status introduced.
- The full financial hold stays intact until the final item is returned.
- Each returned item is recorded individually with its own return timestamp.
- Final settlement (capture / release) is triggered only when the last item is returned.
- At final settlement, each item's actual cost is computed using its own return timestamp:
  `tariff rate × (item return time − rental start time)`.
- The total actual cost (sum of all items) is settled against the hold following the FR-FIN-05 model
  (under-capture → release remainder; over-capture → debit wallet or flag `DEBT`).

### Scope

- Includes: new Finance module (built from scratch), Rental module balance-check integration, and customer transaction
  history.
- Supersedes: US-FN-001, US-FN-002, US-FN-003, US-FN-004.

---

## Additional change request (2026-04-01)

Original short request: "requirements/FIN-001/FR-FIN-05/fr.md the endpoint must support idempotency"

Action taken: `FR-FIN-05/fr.md` was updated to add an **Idempotency** Non-Functional Requirement and an
acceptance scenario describing duplicate-submission handling using an `idempotencyKey` provided by the client.

Planned follow-ups: add backend implementation story to persist and enforce `idempotencyKey`, add tests and API
documentation updates.

---

## Additional change request (2026-04-02)

**Root cause identified:** During review of the FR-FIN-05 implementation, a discrepancy was found between the
`double-entry-lifecycle.md` specification and the `SubLedger` domain model. The `SubLedger` class uses uniform
arithmetic (`debit()` = subtract, `credit()` = add) for all account types. This is correct for liability accounts
(`CUSTOMER_WALLET`, `CUSTOMER_HOLD`) but inverted for asset accounts (`CASH`, `CARD_TERMINAL`, `BANK_TRANSFER`):
a deposit was producing a negative CASH balance (-50) instead of the correct positive value (+50).

**Decision — Option B selected:** Make `SubLedger` account-type-aware. For **asset** sub-ledgers, `debit()`
increases the balance and `credit()` decreases it (standard T-account rule). Liability and income sub-ledger
arithmetic remains unchanged.

**Impact:** `RecordDepositService` and `RecordWithdrawalService` service calls remain structurally unchanged;
the correct balance direction is now enforced inside `SubLedger` based on `LedgerType` category. Component test
assertions for system sub-ledger balances must be updated to reflect correct positive asset values.

**Formalised as:** `FR-FIN-11`.

---

## Additional change request (2026-04-02) — Net-balance ledger invariant

**Finding:** During implementation review of `FR-FIN-06` (Rental Hold), a subtle accounting inconsistency was
identified in `CustomerAccount.availableBalance()`. The original design computed available balance as
`CUSTOMER_WALLET.balance − CUSTOMER_HOLD.balance`. However, because every `SubLedger.debit()` / `credit()` call
mutates the sub-ledger's running balance **in place**, `getBalance()` always returns the **net** of all transactions
ever applied to that ledger. When the hold operation debits `CUSTOMER_WALLET`, the wallet balance already reflects the
post-hold net-available amount. Subtracting `CUSTOMER_HOLD.balance` a second time would double-count the deduction.

**Decision:** `CustomerAccount.availableBalance()` returns `getWallet().getBalance()` directly. The wallet
sub-ledger is the authoritative net-available balance; `CUSTOMER_HOLD` tracks the reserved amount in-place and is
not subtracted again at query time.

**Impact:** `isBalanceSufficient(Money)` continues to delegate to `availableBalance()` and correctly reflects
the real spendable balance before applying any mutation. No structural change to `RecordRentalHoldService`; only
the `availableBalance()` implementation is updated.
