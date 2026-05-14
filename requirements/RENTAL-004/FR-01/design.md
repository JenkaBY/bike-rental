# System Design: FR-01 — Date Range Filter for Search Rentals

## 1. Architectural Overview

This story extends the existing `GET /api/rentals` endpoint with two optional date-range query
parameters (`from`, `to`) that narrow the result set by the `createdAt` field of the `rentals`
table. The change touches all four layers of the rental module's hexagonal stack: the web layer
(new controller parameters), the application layer (extended query record), the domain port
(unified repository method replacing per-combination overloads), and the infrastructure layer
(dynamic JPA query implementation).

The principal structural change is a migration of `RentalRepository`'s four per-combination search
methods (`findByStatus`, `findByStatusAndCustomerId`, `findByCustomerId`,
`findByStatusAndEquipmentUid`) into a single unified method that accepts a domain filter object.
This eliminates the combinatorial branching in `FindRentalsService` and makes the date range an
orthogonal AND-predicate that composes cleanly with every other filter.

The dynamic query is implemented using the existing `net.kaczmarzyk:specification-arg-resolver`
library, consistent with the `CustomerTransactionsSpec` and `CustomerSpec` patterns already in the
codebase. A new `RentalSpec` annotated Specification interface is introduced in the rental
infrastructure layer, and the adapter builds it programmatically via `SpecificationBuilder`.
No new HTTP endpoints, domain events, or database tables are introduced.

---

## 2. Impacted Components

* **`RentalQueryController` (API — `rental/web/query/`):**
  Gains two new `@RequestParam(required = false)` parameters of type date (`from`, `to`). Spring
  MVC's standard date deserialization handles format validation; an unparseable date string returns
  `HTTP 400` before the controller body is reached. Both values are forwarded into
  `FindRentalsUseCase.FindRentalsQuery`.

* **`FindRentalsUseCase` (Application Use Case Interface — `rental/application/usecase/`):**
  The nested `FindRentalsQuery` record gains two new nullable date fields — `from` and `to`. Both
  fields are optional; existing callers that supply neither receive the current baseline behaviour.

* **`FindRentalsService` (Application Service — `rental/application/service/`):**
  Removes the four-branch dispatch logic. Constructs a `RentalSearchFilter` from the query fields
  and delegates to the single unified `RentalRepository.findAll` method. Date-to-Instant
  conversion is the responsibility of `RentalSearchFilter.toMap()`, keeping the service free of
  time-zone arithmetic.

* **`RentalRepository` (Domain Port — `rental/domain/repository/`):**
  The four per-combination search methods are replaced by a single method:
  `findAll(RentalSearchFilter filter, PageRequest pageRequest)`.
  The existing `getCustomerDebtRentals` method is unaffected.

* **`RentalSearchFilter` (Domain Model — `rental/domain/model/`):** *(new)*
  A domain record carrying all optional search predicates: `status` (`RentalStatus`), `customerId`
  (`UUID`), `equipmentUid` (`String`), `from` (`LocalDate`), `to` (`LocalDate`). Mirrors the
  `TransactionHistoryFilter` pattern: exposes a `toMap()` method that serialises each non-null
  field to its string representation for use with `SpecificationBuilder`. Date boundaries are
  converted inside `toMap()`: `from` → `{date}T00:00:00Z` (UTC start of day), `to` →
  `{date}T23:59:59Z` (UTC end of day, using `plusDays(1)` with `LessThan` spec or direct
  end-of-day Instant formatting).

* **`RentalRepositoryAdapter` (Infrastructure Adapter — `rental/infrastructure/persistence/adapter/`):**
  Replaces the four delegating methods with a single implementation of `findAll`. Uses
  `SpecificationBuilder.specification(RentalSpec.class)` and populates params via
  `filter.toMap().forEach(specBuilder::withParam)`, then calls `jpaRepository.findAll(spec,
  pageable)`. Consistent with `TransactionRepositoryAdapter.findTransactionHistory()`.

* **`RentalJpaRepository` (JPA Repository — `rental/infrastructure/persistence/repository/`):**
  The four per-combination derived query methods are removed. `RentalJpaRepository` additionally
  extends `JpaSpecificationExecutor<RentalJpaEntity>` to enable `findAll(Specification, Pageable)`.
  The `findAllByCustomerIdAndStatusOrderByCreatedAtAsc` method used by `getCustomerDebtRentals` is
  not affected.

* **`RentalSpec` (Specification Interface — `rental/infrastructure/persistence/specification/`):** *(new)*
  An annotated interface extending `Specification<RentalJpaEntity>`, following the same
  `@And({@Spec(...)})` pattern as `CustomerTransactionsSpec`. Declares specs for:
    - `status` → `Equal` on `RentalJpaEntity.status`
    - `customerId` → `Equal` on `RentalJpaEntity.customerId`
    - `equipmentUid` → `Equal` on the joined alias field, using `@Join` on `rentalEquipments`
      with `distinct = true` to avoid duplicate rows (mirrors the `DISTINCT` guard in the
      existing `findByStatusAndEquipmentUid` JPQL)
    - `createdFrom` → `GreaterThanOrEqual` on `createdAt`, config `SpecConstant.INSTANT_FORMAT`
    - `createdTo` → `LessThan` on `createdAt`, config `SpecConstant.INSTANT_FORMAT`
      (adapter passes the start-of-next-day instant so the boundary is `< nextDay`, equivalent
      to `<= {date}T23:59:59Z`)

* **`SpecConstant` (Utility — `rental/infrastructure/persistence/specification/`):** *(new)*
  `@UtilityClass` holding field-name constants for `RentalSpec` and the shared Instant format
  string `"yyyy-MM-dd'T'HH:mm:ss'Z'"` (same value as `finance` module's `SpecConstant`).

---

## 3. Abstract Data Schema Changes

None. The `createdAt` column (`Instant`, stored in UTC) already exists on the `rentals` table and
is set immutably at record creation via the JPA `@PrePersist` lifecycle callback. No new columns,
indexes, or Liquibase changesets are required by this story.

> **Note for implementation:** If a covering index on `createdAt` does not exist, a follow-up
> migration adding one is recommended for production workloads but is out of scope for this story.

---

## 4. Component Contracts & Payloads

* **Interaction: HTTP Client → `RentalQueryController`**
    * **Protocol:** REST / HTTP GET
    * **Endpoint:** `GET /api/rentals`
    * **Payload Changes:** Two new optional query parameters added:
        - `from` — date in `yyyy-MM-dd` format; maps to the UTC start of that calendar day
        - `to` — date in `yyyy-MM-dd` format; maps to the UTC end of that calendar day
          All previously existing parameters (`status`, `customerId`, `equipmentUid`, pagination) are
          unchanged. An invalid date format (e.g. `15-02-2026`) results in `HTTP 400` from Spring MVC
          deserialization before any application logic executes.
    * **Success Response:** `HTTP 200` — existing `Page<RentalSummaryResponse>` shape; unchanged.

* **Interaction: `RentalQueryController` → `FindRentalsUseCase`**
    * **Protocol:** In-process method call
    * **Payload Changes:** `FindRentalsQuery` record gains `from: LocalDate` (nullable) and
      `to: LocalDate` (nullable).

* **Interaction: `FindRentalsService` → `RentalRepository`**
    * **Protocol:** In-process method call
    * **Payload Changes:** The four separate `findByX` calls are replaced by a single
      `findAll(RentalSearchFilter, PageRequest)` call. `RentalSearchFilter` carries:
        - `status: RentalStatus` (nullable)
        - `customerId: UUID` (nullable)
        - `equipmentUid: String` (nullable)
        - `from: LocalDate` (nullable)
        - `to: LocalDate` (nullable)

* **Interaction: `RentalRepositoryAdapter` → `RentalSpec` (via `SpecificationBuilder`)**
    * **Protocol:** In-process `SpecificationBuilder` construction
    * **Payload Changes:** `filter.toMap()` produces a `Map<String, String>` whose entries are
      forwarded to `SpecificationBuilder.withParam()`. The map keys match the `params` attributes
      declared on `RentalSpec` `@Spec` annotations. Null values in the map are not forwarded,
      so the corresponding predicate is omitted from the generated SQL.

* **Interaction: `RentalRepositoryAdapter` → `RentalJpaRepository`**
    * **Protocol:** In-process JPA call (`JpaSpecificationExecutor.findAll(Specification, Pageable)`)
    * **Payload Changes:** A single `findAll(spec, pageable)` call replaces all four derived-query
      method calls. The built `Specification` encodes only the active predicates.

---

## 5. Updated Interaction Sequence

### Happy path — date range with existing filters

1. HTTP client sends `GET /api/rentals?status=ACTIVE&from=2026-02-15&to=2026-02-20`.
2. `RentalQueryController` binds `status=ACTIVE`, `from=2026-02-15` (`LocalDate`),
   `to=2026-02-20` (`LocalDate`), and default `Pageable`.
3. Controller builds `FindRentalsQuery(status=ACTIVE, customerId=null, equipmentUid=null,
   from=2026-02-15, to=2026-02-20, pageRequest)` and calls `FindRentalsUseCase.execute`.
4. `FindRentalsService` builds `RentalSearchFilter(status=ACTIVE, customerId=null,
   equipmentUid=null, from=2026-02-15, to=2026-02-20)` and calls
   `RentalRepository.findAll(filter, pageRequest)`.
5. `RentalRepositoryAdapter` calls `filter.toMap()`, which serialises the dates:
    - `"createdFrom"` → `"2026-02-15T00:00:00Z"`
    - `"createdTo"`   → `"2026-02-21T00:00:00Z"` (start of next day, used with `LessThan`)
      Each non-null entry is passed to `SpecificationBuilder.withParam()`; `spec = txnSpec.build()`.
6. `RentalRepositoryAdapter` calls `jpaRepository.findAll(spec, pageable)`. The generated SQL
   applies `WHERE status = 'ACTIVE' AND created_at >= :createdFrom AND created_at < :createdTo`.
7. JPA returns a `Page<RentalJpaEntity>`; adapter maps to `Page<Rental>` via `RentalJpaMapper`.
8. `FindRentalsService` returns `Page<Rental>` to controller.
9. Controller maps to `Page<RentalSummaryResponse>` and returns `HTTP 200`.

### Happy path — no date range (baseline behaviour preserved)

1. HTTP client sends `GET /api/rentals?status=ACTIVE`.
2. Controller builds `FindRentalsQuery` with `from=null`, `to=null`.
3. `FindRentalsService` builds `RentalSearchFilter(status=ACTIVE, from=null, to=null, ...)`.
4. `filter.toMap()` produces null values for `createdFrom`/`createdTo`; `SpecificationBuilder`
   omits those params. Generated SQL applies only `WHERE status = 'ACTIVE'`.
5. Result returned as before; behaviour is identical to the pre-RENTAL-004 baseline.

### Unhappy path — invalid date format

1. HTTP client sends `GET /api/rentals?from=15-02-2026`.
2. Spring MVC fails to deserialize `"15-02-2026"` into `LocalDate` (format mismatch).
3. `HTTP 400` is returned with a `ProblemDetail` error body before any application code runs.
4. No service or repository call is made.

---

## 6. Non-Functional Architecture Decisions

* **Security & Auth:** No new data exposure. The endpoint already returns the full paginated rental
  list; the date range merely restricts the scope of results. No authentication changes required.
* **Scale & Performance:** `SpecificationBuilder` generates parameterised SQL compatible with
  index range scans on `createdAt`. The `@Join(distinct = true)` annotation on `RentalSpec`
  retains the `DISTINCT` guard for the `equipmentUid` join path, preventing row duplication as
  in the existing `findByStatusAndEquipmentUid` JPQL implementation.
