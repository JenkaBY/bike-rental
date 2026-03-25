# Initial User Request — FIN-001

## Original Request

As a stakeholder I want to implement a payment intake and processing system for renting bicycles and related
accessories.

The rental module accepts a request to create a rental with multiple types of equipment. This module must check whether
there are sufficient funds for the rental: if there are, the rental is created; if not, an insufficient funds error is
returned.

The financial module is still in the design phase. Several approaches for handling payments were proposed without the
need to send requests to external services such as banks — the accounting system will be entirely internal.

The system must also store the user's balance in case the user does not want to take their change. The system should be
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
At rental creation the expected return time is specified, so the estimated cost = tariff rate × expected duration.
The final cost is determined at return time.

### Deposit Operations

- Staff manually records cash or online transfer deposits at the counter.
- Admin can perform manual balance adjustments (positive or negative) with a mandatory reason.
- Deposit,Refund and rental are always separate operations.

### Account Types

- `CUSTOMER_WALLET` — customer's available spendable balance
- `CUSTOMER_HOLD` — reserved/pre-authorized funds
- `REVENUE` — shop's income
- `CASH` — shop's asset for cash deposits
- `ADJUSTMENT` — internal account for admin corrections

### Overtime Edge Case (FR-FIN-06)

If actual rental cost exceeds the held amount:

1. Capture the entire hold.
2. Attempt to debit the remainder from the customer's available balance.
3. If the available balance is also insufficient, capture what is available and flag the rental with a `DEBT` status for
   manual resolution by staff.

### Scope

- Includes: new Finance module (built from scratch), Rental module balance-check integration, and customer transaction
  history.
- Supersedes: US-FN-001, US-FN-002, US-FN-003, US-FN-004.

---

## Addendum — Additional Requirements (March 2026)

### New Requests

- An operator can accept cashless payments (card terminal or bank transfer) in addition to cash.
- A customer can withdraw funds from their wallet balance (cash or cashless; partial withdrawal allowed).
- Rental supports partial equipment return: a customer who rented multiple items can return them one at a time;
  financial accounting is deferred until the final item is returned.

### Clarifications Captured

#### Cashless Payment

- Supported methods: card terminal (POS) and bank transfer.
- Separate internal ledger accounts per method: `CARD_TERMINAL` and `BANK_TRANSFER` (in addition to the existing `CASH`
  account).
- Double-entry: `CARD_TERMINAL` or `BANK_TRANSFER` (debit) → `CUSTOMER_WALLET` (credit).
- Transaction type remains `DEPOSIT` with an additional payment-method attribute.

#### Withdrawal

- Staff records the withdrawal and selects the payout method (cash or cashless).
- Partial withdrawals are allowed: any positive amount up to the available balance.
- Only available (non-held) funds can be withdrawn — on-hold balance is excluded.
- Double-entry: `CUSTOMER_WALLET` (debit) → `CASH` / `CARD_TERMINAL` / `BANK_TRANSFER` (credit).
- New transaction type: `WITHDRAWAL`.

#### Partial Equipment Return

- Rental status remains `ACTIVE` throughout all partial returns — no new status introduced.
- The full financial hold stays intact until the final item is returned.
- Each returned item is recorded individually with its own return timestamp.
- Final settlement (capture / release) is triggered only when the last item is returned.
- At final settlement, each item's actual cost is computed using its own return timestamp:
  `tariff rate × (item return time − rental start time)`.
- The total actual cost (sum of all items) is then settled against the hold, following the existing FR-FIN-05 settlement
  model (under-capture → release remainder; over-capture → debit wallet or flag DEBT).

