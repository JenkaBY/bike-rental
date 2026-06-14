# Initial User Request — TARIFF-001

## Original Request

> I want to create a new rental calculations PUT endpoint (currently I've started its implementation
> `/api/tariffs/calucations`).
>
> What's wrong with the current `/calculate`:
>
> - **`flat fee` tariff calculates costs incorrectly when rental takes more than 1 day.** For a customer who takes
    > equipment (bike and helmet) in the evening and returns it the next day's morning. In this scenario the helmet cost
    > is calculated incorrectly because it is calculated only for one day but actually it should take into consideration
    > 2 days — the day of rent plus the next day.
>
> - **Calculations happen only against equipment type; however the rental allows partial returns of equipment.** This
    > leads to incorrectly calculated cost for the whole rental if it contains one type of equipment and items were
    > returned separately. So I want to pass `equipmentId` and `returnedAt` along with equipment type.
>
> The controller should follow the same pattern as it's done for the `/calculate` endpoint, in other words it should
> invoke the Facade for calculation to be able to invoke calculation directly from the rental module.

## Clarifications Gathered

| Question                                    | Answer                                                                                    |
|---------------------------------------------|-------------------------------------------------------------------------------------------|
| Timezone for calendar-day boundary          | Server/configured timezone                                                                |
| Global vs per-item planned duration         | Global planned duration applies to all items (current behaviour)                          |
| Tariff selection date                       | Global `startAt` (datetime) replaces `rentalDate`; selection uses `startAt.toLocalDate()` |
| Include `equipmentId` in response breakdown | Yes                                                                                       |
| HTTP method & URL                           | `PUT /api/tariffs/calculations` (fix typo, keep PUT)                                      |
| Discount and special tariff in V2           | Keep same as V1 (global discount + special tariff supported)                              |
| DAILY tariff                                | Confirmed 24h-block model (not calendar-day); no fix needed                               |

## Approved Requirement Plan

- **FR-1** — Flat fee tariff: calendar-day billing fix
- **FR-2** — `PUT /api/tariffs/calculations`: per-equipment V2 cost calculation endpoint
