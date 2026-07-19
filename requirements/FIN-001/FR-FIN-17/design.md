# Design: FR-FIN-17 — Transaction Details

## Endpoint

`GET /api/finance/transactions/{transactionId}` → `TransactionDetailsResponse`

| Element | Type | Notes |
|---------|------|-------|
| `transactionId` | `UUID` (path) | malformed → `400`; unknown → `404` |
| `id, customerId, amount, type, recordedAt, paymentMethod, reason, sourceType, sourceId, operatorId` | flat fields | mirror `TransactionSummaryResponse` |
| `deltas` | `{wallet, hold, external}` | signed per-bucket change; `external = wallet + hold` |
| `balances` | `{wallet, hold}` | customer bucket balances **after**, always populated |
| `entries[]` | `{ledgerType, direction, amount, signedDelta, balanceAfter, systemLedger}` | one per double-entry leg; `balanceAfter` nullable |

## Layers (finance module, hexagonal)

* **web/query** — add `GET /{transactionId}` to `TransactionQueryController` (package-private, already
  mapped at `/api/finance/transactions`); new `TransactionDetailsResponse` (with nested
  `TransactionDetailEntryResponse`); shared `TransactionDeltasResponse` / `TransactionBalancesResponse`
  records reused by the per-customer history response. `TransactionQueryMapper` is the single transaction
  read mapper: `toResponse` (summary list), `toDetailsResponse(TransactionDetails)`,
  `toCustomerTransactionResponse(TransactionDetails)`, plus shared `default deltas(...)`/`balances(...)`/
  `direction(...)` methods.
* **application** — `GetTransactionDetailsUseCase` (returns the domain `TransactionDetails`),
  `GetTransactionDetailsService` (package-private, `@Transactional(readOnly = true)`): loads the
  transaction or throws `ResourceNotFoundException(Transaction.class, id)`, then delegates balance
  assembly to `TransactionDetailsAssembler`, which is shared with `GetTransactionHistoryService`.
* **domain** — new `TransactionDetails(Transaction transaction, Money walletBalanceAfter, Money
  holdBalanceAfter)` read model; new port `TransactionRepository.findById(UUID)`; **replace**
  `TransactionRecord.signedAmount()` with a ledger-aware `signedBalanceDelta()`.
* **infrastructure/persistence** — `TransactionJpaRepository.findWithRecordsById(UUID)`
  (`@EntityGraph(attributePaths = "records")`); `TransactionRepositoryAdapter.findById`
  (`@Transactional(readOnly = true)`, maps via `TransactionJpaMapper`).

## Key decisions

* **Ledger-aware `signedBalanceDelta` replaces `signedAmount`.** The old method was direction-only
  (`CREDIT = +amount`), which contradicts the balance movement for asset ledgers (a `CASH` `DEBIT`
  raises the balance). It has a single call site (`Transaction.bucketDelta`, which only sums non-asset
  customer buckets), so the replacement is behaviour-preserving there while making `entries[].signedDelta`
  agree with `balanceAfter`. The sign rule matches the running-balance backfill SQL exactly.
* **`balances` are always fully populated.** A transaction only carries running balances for the buckets
  it touches. To keep the contract identical to the history endpoint (both fields `@NotNull`), the
  untouched bucket is seeded from `findLatestLedgerBalancesBefore(customerRef, recordedAt)` — an existing
  finance port already used by `RunningBalanceCalculator`. This stays inside the finance module.
* **Domain read model, not an application DTO.** `TransactionDetails` bundles the transaction with the
  two seeded balances that `Transaction` alone cannot provide. The use case returns the domain object;
  mapping to the response happens in the web mapper (consistent with the FR-FIN-16 refactor that removed
  a redundant application-layer DTO).
* **`balanceAfter` is nullable.** The column is nullable and system legs may lack a running balance;
  the field carries `@Schema(nullable = true)` and no `@NotNull`.
* **One read model and one mapper for both endpoints.** `GetTransactionHistoryUseCase` returns
  `Page<TransactionDetails>` (its application-layer `TransactionDto` and the application `TransactionMapper`
  are deleted), so both transaction read use cases return the same domain object and one web mapper serves
  both responses. The seed/carry-forward balance logic lives once in `TransactionDetailsAssembler`
  (formerly `RunningBalanceCalculator`, now returning `Map<UUID, TransactionDetails>`). Because that
  assembler always seeds with `Money.zero()`, `balances` is never null — which also makes the pre-existing
  `@NotNull` on the history response's balances truthful.
* **Entry order is not asserted by index.** `TransactionJpaEntity.records` has no `@OrderBy`; tests match
  legs by `ledgerType` + `direction`. No `@OrderBy` is added — it would carry no business meaning.

## Persistence / Liquibase

* **None.** The lookup is by primary key and `idx_finance_transaction_records_transaction_id` already
  supports fetching the legs.

## Testing

* Unit: `TransactionRecordTest` — parameterized over `(ledgerType, direction, amount, expectedSignedDelta)`
  covering the asset-ledger inversion; the highest-value guard for the sign fix.
* WebMvc: `TransactionQueryControllerTest` gains a `GetTransactionDetails` nested block — 200 with the
  full breakdown, `404` (unknown id), `400` (malformed id).
* Component: scenarios live in `features/finance/transactions.feature`, merged into the same file as the
  FR-FIN-16 listing scenarios (same resource, two endpoints) under a shared `Background` — a dedicated
  customer (`CUS5`) carries the details-only fixtures so the listing scenarios' counts are unaffected.
  Covers DEPOSIT (customer + system leg), HOLD (two customer legs, nets to zero externally), CAPTURE
  (wallet balance seeded from the prior balance), and unknown id → 404. New
  `TransactionDetailsResponseRowTransformer` (typed expected-row records) and `TransactionDetailsWebSteps`
  (`the transaction details response contains` / `the transaction details entries only contain`). HTTP
  call reuses the existing `WebRequestSteps` path-variable step.
