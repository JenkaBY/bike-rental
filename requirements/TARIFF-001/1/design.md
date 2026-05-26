# System Design: FR-1 — Flat Fee Tariff: Calendar-Day Billing Rule

## 1. Architectural Overview

The current flat fee cost calculation lives entirely within `FlatFeeTariffV2`, a domain model in the tariff module's
domain layer. Its `calculateCost(Duration)` method derives billable days using `ceil(minutes / 1440)`, which
undercounts overnight rentals that span a date boundary. Because the bug is confined to a single domain calculation
method, this FR is a targeted domain-layer change that does not alter any API contract, data schema, or cross-module
interaction.

The fix requires the cost calculation to know the concrete start and end datetimes of the rental period, not just
their difference. This means the `TariffV2` abstract cost calculation signature must be extended to carry
temporal-period context so that `FlatFeeTariffV2` can compute distinct calendar dates. The server's configured
timezone is injected via an existing infrastructure mechanism (e.g., the application-level `Clock` bean) and flows
into the domain without introducing any new I/O or cross-module dependency.

---

## 2. Impacted Components

* **`FlatFeeTariffV2` (Flat Fee Domain Model):** Must replace its `ceil(minutes / 1440)` day-count algorithm with a
  calendar-date span computation. Given `startAt` and `returnAt` datetimes evaluated in the server's configured
  timezone, it counts the number of distinct dates in the closed interval `[startDate, returnDate]`. The minimum
  one-day guard is preserved.

* **`TariffV2` (Abstract Tariff Domain Model):** Must expose a new cost calculation contract that accepts a
  temporal period — concretely, a start datetime and an end datetime — alongside (or instead of) a raw `Duration`.
  All other concrete subclasses (`FlatHourlyTariffV2`, `DailyTariffV2`, `DegressiveHourlyTariffV2`,
  `SpecialTariffV2`) implement this new signature using the existing `Duration`-based logic unchanged; they compute
  duration internally from the two datetimes.

* **`RentalCostCalculationService` (V1 Calculation Application Service):** Must pass `startAt` and `returnAt`
  timestamps to `TariffV2.calculateCost(...)` instead of the raw `Duration`. The V1 command carries a
  `rentalDate` (LocalDate) but not a precise `startAt` time; for V1 backward compatibility, `startAt` may be
  synthesised as midnight of `rentalDate` and `returnAt` as `startAt + billedDuration`. This ensures V1 callers
  continue to operate correctly while the domain model gains the richer signature.

---

## 3. Abstract Data Schema Changes

No database schema changes required. All computation is in-memory at calculation time. No new persistent entities,
attributes, or relations are introduced.

---

## 4. Component Contracts & Payloads

* **Interaction: `RentalCostCalculationService` → `TariffV2.calculateCost`**
    * **Protocol:** In-process method call (domain model invocation)
    * **Payload Changes:** The method signature changes from `calculateCost(Duration)` to
      `calculateCost(LocalDateTime startAt, LocalDateTime returnAt, ZoneId timezone)`. Duration is derived
      internally by each subclass from these two datetimes. `FlatFeeTariffV2` additionally uses `startAt`,
      `returnAt`, and `timezone` to count distinct calendar dates.

* **Interaction: `TariffV2FacadeImpl` / `ReturnEquipmentService` → `RentalCostCalculationService`**
    * **Protocol:** In-process method call via `RentalCostCalculationUseCase`
    * **Payload Changes:** `RentalCostCalculationCommand` gains an optional `startAt` (`LocalDateTime`) field used
      to construct the temporal period for tariff calculation. When absent (e.g., legacy callers), it is synthesised
      from `rentalDate` at midnight.

* **External HTTP API (`POST /api/tariffs/calculate`):** Contract is unchanged. The fix is transparent to all
  existing API consumers. The response breakdown message will reflect the corrected day count.

---

## 5. Updated Interaction Sequence

**Happy path — overnight flat fee rental (e.g., 8 PM → 8 AM)**

1. `TariffV2CalculationController` receives `POST /api/tariffs/calculate` with `CostCalculationRequest` (existing
   contract unchanged).
2. `BatchCalculationMapper` converts the request to `RentalCostCalculationCommand`. No new fields in V1 request.
3. `TariffV2FacadeImpl` delegates to `RentalCostCalculationUseCase.execute(command)`.
4. `RentalCostCalculationService` computes `billedDuration` per the existing forgiveness logic.
5. For each equipment item, it constructs a temporal period:
    * `startAt` = `command.rentalDate()` at midnight (synthesised for V1); `returnAt` = `startAt + billedDuration`.
6. `RentalCostCalculationService` calls `tariff.calculateCost(startAt, returnAt, serverTimezone)`.
7. `FlatFeeTariffV2.calculateCost(...)` evaluates `startAt.toLocalDate(timezone)` and `returnAt.toLocalDate(timezone)`;
   counts distinct dates; returns cost = `issuanceFee × distinctDays`.
8. The breakdown message records the corrected day count.
9. `RentalCostCalculationService` aggregates per-item results and returns `RentalCostCalculationResult`.
10. `TariffV2CalculationController` returns HTTP 200 with the updated `CostCalculationResponse`.

**Edge case — zero or negative duration**

* Steps 1–5 as above.
* `FlatFeeTariffV2` detects `startAt == returnAt` (or `returnAt` before `startAt`) and returns minimum 1 day charge.

---

## 6. Non-Functional Architecture Decisions

* **Security & Auth:** No change. The endpoint is currently open (no SecurityFilterChain configured). The fix
  introduces no new data exposure.
* **Scale & Performance:** The new calculation is O(1) — a simple subtraction of two `LocalDate` values plus 1.
  No additional database queries, no caching changes required.
