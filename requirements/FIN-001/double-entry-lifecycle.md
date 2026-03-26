# Double-Entry Chain — Full Lifecycle (with System Account)

Each row is a balanced journal entry. Money flows from **Debit** to **Credit**.
`[S]` = System Account sub-ledger · `[C]` = Customer Account sub-ledger

---

## 1. Deposit

| Operation             | Debit                   | Credit                    | Notes                         |
|-----------------------|-------------------------|---------------------------|-------------------------------|
| Cash deposit          | `CASH` **[S]**          | `CUSTOMER_WALLET` **[C]** | Staff records cash at counter |
| Card deposit          | `CARD_TERMINAL` **[S]** | `CUSTOMER_WALLET` **[C]** | POS terminal payment          |
| Bank transfer deposit | `BANK_TRANSFER` **[S]** | `CUSTOMER_WALLET` **[C]** | Incoming wire                 |

---

## 2. Rental Lifecycle (Hold → Settle)

| Operation               | Debit                     | Credit                    | Notes                                             |
|-------------------------|---------------------------|---------------------------|---------------------------------------------------|
| Hold (rental created)   | `CUSTOMER_WALLET` **[C]** | `CUSTOMER_HOLD` **[C]**   | Tariff module computes planned cost; funds frozen |
| Capture (normal return) | `CUSTOMER_HOLD` **[C]**   | `REVENUE` **[S]**         | Tariff module computes final cost ≤ held amount   |
| Release (excess)        | `CUSTOMER_HOLD` **[C]**   | `CUSTOMER_WALLET` **[C]** | Remainder unfrozen back to wallet after capture   |

---

## 3. Overtime Settlement (final cost > held amount)

| Step | Operation                   | Debit                     | Credit            | Notes                                             |
|------|-----------------------------|---------------------------|-------------------|---------------------------------------------------|
| 3a   | Capture full hold           | `CUSTOMER_HOLD` **[C]**   | `REVENUE` **[S]** | Entire hold consumed                              |
| 3b   | Debit remainder from wallet | `CUSTOMER_WALLET` **[C]** | `REVENUE` **[S]** | If wallet covers the gap                          |
| 3c   | Partial capture + flag DEBT | `CUSTOMER_WALLET` **[C]** | `REVENUE` **[S]** | What's available; rental flagged `DEBT` for staff |

---

## 4. Withdrawal

| Operation            | Debit                     | Credit                  | Notes                             |
|----------------------|---------------------------|-------------------------|-----------------------------------|
| Cash payout          | `CUSTOMER_WALLET` **[C]** | `CASH` **[S]**          | Only available (non-held) balance |
| Card payout          | `CUSTOMER_WALLET` **[C]** | `CARD_TERMINAL` **[S]** | Partial withdrawal allowed        |
| Bank transfer payout | `CUSTOMER_WALLET` **[C]** | `BANK_TRANSFER` **[S]** | Partial withdrawal allowed        |

---

## 5. Manual Adjustment

| Operation            | Debit                     | Credit                    | Notes                                          |
|----------------------|---------------------------|---------------------------|------------------------------------------------|
| Top-up adjustment    | `ADJUSTMENT` **[S]**      | `CUSTOMER_WALLET` **[C]** | Admin adds funds; mandatory reason required    |
| Deduction adjustment | `CUSTOMER_WALLET` **[C]** | `ADJUSTMENT` **[S]**      | Admin removes funds; mandatory reason required |

---

## Key Invariants

- Every entry has exactly one `[S]` side and one `[C]` side — no entry stays entirely within a single account entity.
- `HOLD` and `RELEASE` are the only exception: both sides are `[C]` because money moves *within* the customer account (
  wallet ↔ hold), not out of it.
- The System Account's `CASH`, `CARD_TERMINAL`, and `BANK_TRANSFER` sub-ledgers always mirror real-world
  physical/electronic money flows — no money is created or destroyed.

# The Asset Story — Where Does the Money Actually Live?

## The Core Insight

Assets (`CASH`, `CARD_TERMINAL`, `BANK_TRANSFER`) move **only at the boundary** — when real-world money
physically enters or exits the shop. Everything in between is liability restructuring and income recognition.

---

## Tracing €100 Through the Full Lifecycle

```
Step 1 — DEPOSIT (customer pays €100 cash)
┌─────────────────────────────────────────────────────────────────┐
│  CASH [S]              +100   │  CUSTOMER_WALLET [C]      +100  │
│  (shop physically has €100)   │  (shop owes customer €100)      │
└─────────────────────────────────────────────────────────────────┘
  ↑ asset moves HERE, and only here

Step 2 — HOLD (rental created, estimated cost €80)
┌─────────────────────────────────────────────────────────────────┐
│  CUSTOMER_WALLET [C]    -80   │  CUSTOMER_HOLD [C]         +80  │
│  (available balance ↓)        │  (frozen balance ↑)             │
└─────────────────────────────────────────────────────────────────┘
  CASH [S] untouched — the €100 is still physically in the drawer

Step 3a — CAPTURE (actual cost €75, rental returned)
┌─────────────────────────────────────────────────────────────────┐
│  CUSTOMER_HOLD [C]      -75   │  REVENUE [S]               +75  │
│  (liability extinguished)     │  (income recognised)            │
└─────────────────────────────────────────────────────────────────┘
  CASH [S] untouched — shop has always had the €100

Step 3b — RELEASE (€5 excess returned to wallet)
┌─────────────────────────────────────────────────────────────────┐
│  CUSTOMER_HOLD [C]       -5   │  CUSTOMER_WALLET [C]        +5  │
│  (remaining hold cleared)     │  (available again)              │
└─────────────────────────────────────────────────────────────────┘

Step 4 — WITHDRAWAL (customer takes back the €25 change)
┌─────────────────────────────────────────────────────────────────┐
│  CUSTOMER_WALLET [C]    -25   │  CASH [S]                  -25  │
│  (liability gone)             │  (shop pays out physically)     │
└─────────────────────────────────────────────────────────────────┘
  ↑ asset moves HERE again — cash leaves the drawer
```

---

## Final Balance Sheet After All Steps

| Account           | Entity   | Δ       | Meaning                              |
|-------------------|----------|---------|--------------------------------------|
| `CASH`            | System   | **+75** | Shop retains €75 in the drawer       |
| `REVENUE`         | System   | **+75** | Shop recognises €75 of earned income |
| `CUSTOMER_WALLET` | Customer | **0**   | Customer has no remaining balance    |
| `CUSTOMER_HOLD`   | Customer | **0**   | No frozen funds remain               |

Assets (+75) = Revenue (+75) ✅ — the accounting equation holds.

---

## Why REVENUE Does Not "Accept" Money

`REVENUE` is an **income account**, not an asset. It answers the question:
*"How much has the shop earned?"* — not *"Where is the cash?"*

The cash is always in `CASH [S]` from the moment of deposit.
`REVENUE` simply records the moment the shop's *obligation to the customer* converts into *earned income*.

```
