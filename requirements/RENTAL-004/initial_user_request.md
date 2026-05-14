# Initial User Request — RENTAL-004

## Original Request

Extend the Search Rentals endpoint with 2 additional optional request params:

- `from={date}`
- `to={date}`

The params will be used as AND filter to existing parameters and allows to narrow the list of
search result to the specified period. The target search field in the rental table is `createdAt`.

- If `from` exists it should be translated to the beginning of the day 00:00.
  Example: `2026-02-15` → `2026-02-15T00:00:00`
- If `to` exists it should be translated to the beginning of the day 00:00.
  Example: `2026-02-15` → `2026-02-15T23:59:59`

## Clarifications Captured During Analysis

| Topic                        | Decision                                                                                         |
|------------------------------|--------------------------------------------------------------------------------------------------|
| Timezone for date conversion | UTC — input dates are treated as UTC calendar days                                               |
| Date input format            | ISO-8601 date only: `yyyy-MM-dd` (e.g. `2026-02-15`)                                             |
| Target field                 | `createdAt` column (stored as `Instant`, UTC)                                                    |
| `from` boundary              | Inclusive start of UTC day: `{date}T00:00:00Z`                                                   |
| `to` boundary                | Inclusive end of UTC day: `{date}T23:59:59Z`                                                     |
| Filter combination           | AND with all existing filters (`status`, `customerId`, `equipmentUid`) — applies to all combos   |
| Repository dispatch strategy | Migrate to a unified dynamic query to avoid combinatorial explosion of per-combo overloads       |
| Validation                   | `from` must not be after `to`; violation returns HTTP 400 with `CONSTRAINT_VIOLATION` error code |
| Component tests              | Happy-path scenarios (with and without date range) must be covered by component tests            |
