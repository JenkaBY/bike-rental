# Future Extension: Service Expenses Integration

## Overview

This document describes how to integrate service-related expenses (operator salaries, spare parts,
equipment repairs) into the existing finance architecture without code changes to current components.

## Key Principles

- Reuse the double-entry `Transaction` / `TransactionRecord` model and the `TransactionRepository`.
- Extend enums and add distinct System Account sub-ledgers; no new tables are required.
- Use `source_type` / `source_id` to link expenses to domain entities (e.g., equipment) when appropriate.

## Additions (logical)

- `TransactionType`: add `EXPENSE`.
- `TransactionSourceType`: add `PAYROLL` and `EQUIPMENT`.
- `LedgerType` (sub-ledgers on the System Account): add `OPERATING_EXPENSE` and `MAINTENANCE_EXPENSE`.

## Ledger & Journal Patterns

- Operator salary (payroll):
  - DEBIT  → System Account / OPERATING_EXPENSE  (+amount)
  - CREDIT → System Account / CASH               (-amount)
  - `Transaction.type = EXPENSE`, `sourceType = PAYROLL`, `sourceId = null`

- Spare parts / repair:
  - DEBIT  → System Account / MAINTENANCE_EXPENSE  (+amount)
  - CREDIT → System Account / CASH                 (-amount)
  - `Transaction.type = EXPENSE`, `sourceType = EQUIPMENT`, `sourceId = <equipment UUID>`

Both cases are internal System Account transfers and emit a balanced journal with two `TransactionRecord` rows.

## Component-level Changes (new responsibilities)

- `ExpenseCommandController` (new API): `POST /api/finance/expenses` to record expenses.
- `RecordExpenseUseCase` (new port): `execute(RecordExpenseCommand)`.
- `RecordExpenseService` (implementation): resolves the two System Account sub-ledgers, mutates balances in a
  transactional boundary, constructs the `Transaction` (type=EXPENSE) and persists via `TransactionRepository`.

## Persistence & DDL

- No new tables required. `finance_transactions` and `finance_transaction_records` store expense journals.
- Liquibase changeset: insert the two new `SubLedger` rows for the System Account (`OPERATING_EXPENSE`,
  `MAINTENANCE_EXPENSE`) and a small reference-data migration for the new enum values if needed by code.

## Non-functional notes

- Use existing `idempotencyKey` deduplication for expense commands.
- Ensure `operatorId` is captured for auditability (already present on `Transaction`).
- For payroll (recurring), consider a scheduler or batch process that emits `EXPENSE` transactions.

## Summary

The current finance model was designed for extension: adding `EXPENSE`-type transactions plus two new
sub-ledgers on the System Account integrates service expenses cleanly without DDL changes. Implementation follows
the same `RecordDepositService` pattern (resolve ledgers, mutate balances, persist a balanced `Transaction`).
