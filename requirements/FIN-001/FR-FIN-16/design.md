# Design: FR-FIN-16 — Cross-Customer Transaction Listing

## Endpoint

`GET /api/finance/transactions` → `Page<TransactionSummaryResponse>`

| Param | Type | Notes |
|-------|------|-------|
| `customerIds` | `Set<UUID>` | optional, `@Size(max = 100)`; any-of match |
| `fromDate` / `toDate` | `LocalDate` | `@DateTimeFormat(ISO.DATE)`; business-zone day bounds, `toDate` exclusive upper (next-day start) |
| `sourceId` | `String` | optional exact match |
| `sourceType` | `TransactionSourceType` | optional exact match |
| `ledgerTypes` | `Set<LedgerType>` | optional; transaction touched any of these ledgers |
| `page` / `size` / `sort` | `Pageable` | `@PageableDefault(size = 20, sort = "recordedAt", direction = DESC)` |

`TransactionSummaryResponse` fields: `id, customerId, amount, type, recordedAt, paymentMethod, reason, sourceType,
sourceId, operatorId, entries[]`. `entries[]` = `{ledgerType, direction, amount}` per double-entry leg. No running
balances/deltas (details endpoint owns those).

## Layers (finance module, hexagonal)

* **web/query** — `TransactionQueryController` (package-private), `TransactionFilterParams` (`@ModelAttribute`),
  `TransactionSummaryResponse`, `TransactionQueryMapper` (builds the domain `TransactionFilter`, maps DTO→response).
  Sort passes through via `PageMapper.toPageRequest`.
* **application** — `FindTransactionsUseCase` (+ `TransactionListItemDto`), `FindTransactionsService` (delegation only,
  no account-existence check), `TransactionListMapper` (domain `Transaction` → DTO, `records` → `entries`).
* **domain** — `TransactionFilter` (record, immutable, null→empty sets), `TransactionSortField` enum (api-name → entity
  property allowlist used for sort translation), new port `TransactionRepository.findTransactions(filter, pageRequest)`.
* **infrastructure/persistence** — `TransactionsSpec` (net.kaczmarzyk annotated: `customerIds` In, `fromDate`/`toDate`
  range, `sourceId`/`sourceType` Equal — **no join**), `TransactionsSpecParamsMapper` (single-value params),
  `BusinessDayBoundaryResolver` (shared start-of-day / next-day formatting, extracted from
  `CustomerTransactionsSpecParamsMapper`), `LedgerTypeExistsSpecification` (hand-written `EXISTS` subquery over
  `TransactionRecordJpaEntity`), `TransactionRepositoryAdapter.findTransactions` (builds spec, ANDs the ledger-type
  EXISTS, translates sort api-name → entity property, defaults `recordedAt DESC`).

## Key decisions

* **`ledgerTypes` uses an `EXISTS` subquery, not a join.** A join on `records` would duplicate a transaction row once
  per matching leg, corrupting page content and `totalElements` (a `HOLD` has two customer legs). `EXISTS` keeps each
  transaction row single. Note: the legacy `CustomerTransactionsSpec` uses an inner join + `In` and is therefore
  susceptible to this duplication — left unchanged here, but the new endpoint must not replicate it.
* **No sort allowlist / 400.** Sort is passed through; unknown properties surface as the framework default. Only
  `recordedAt`, `amount`, `type` are documented as supported (`type` translates to the `transactionType` column).
* **Business-day boundary logic is shared** via `BusinessDayBoundaryResolver` between the old and new param mappers.

## Persistence / Liquibase

* `idx_finance_transactions_recorded_at (recorded_at DESC)` — covers the default non-customer-scoped sort.
* `idx_finance_transaction_records_ledger_type` — supports the ledger-type `EXISTS` check.
* Registered at the bottom of `db.changelog-master.xml`; changesets `author="claude"`, guarded by `indexExists`.

## Testing

* WebMvc: `TransactionQueryControllerTest` — 200 with legs, empty page, and `400` cases (bad UUID, unknown
  `sourceType`, unknown `ledgerTypes`).
* Component: `features/finance/transactions.feature` — cross-customer, single/multi customer, ledger-type,
  sourceType, date-range, HOLD-listed-once, empty. New `TransactionResponseRowTransformer` +
  `TransactionsWebSteps`; shared `WebRequestSteps` now resolves comma-separated alias lists in query params.
  (This feature file was later merged with the FR-FIN-17 transaction-details scenarios — see that design doc.)
