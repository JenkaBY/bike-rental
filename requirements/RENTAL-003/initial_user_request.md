# Initial User Request — RENTAL-003

## Original Request

Introduce lifecycle management for rentals via a new dedicated endpoint. The key points are:

- Introduce a new `PATCH /api/rentals/{rentalId}/lifecycles` endpoint that accepts
  `{ "status": "ACTIVE | CANCELLED" }` and routes to a dedicated use case per status.
- Create separate use cases per allowed lifecycle status: `ActivateRentalUseCase` (DRAFT → ACTIVE)
  and `CancelRentalUseCase` (DRAFT|ACTIVE → CANCELLED).
- Add request validation: `status` must be one of exactly `ACTIVE` or `CANCELLED`.
- Make `RentalStatus` enum the owner of the state machine — it validates all allowed transitions.
- Extract the activation logic from `UpdateRentalService` into the new `ActivateRentalUseCase`.
- Remove `/status` field handling from the JSON Patch flow in `UpdateRentalService`; all status
  transitions now go through the lifecycle endpoint only.
- Default rental status at creation remains `DRAFT` (unchanged).

## Clarifications Captured During Analysis

| Topic                            | Decision                                                                                      |
|----------------------------------|-----------------------------------------------------------------------------------------------|
| COMPLETED via lifecycle          | Removed from scope — existing `POST /api/rentals/return` flow handles COMPLETED and DEBT      |
| Allowed lifecycle statuses       | `ACTIVE` and `CANCELLED` only (COMPLETED, DRAFT, DEBT excluded from lifecycle endpoint)       |
| CANCELLED from ACTIVE (hold)     | Release the hold via `FinanceFacade` (refund)                                                 |
| UpdateRentalService status patch | Remove `/status` from JSON Patch entirely; lifecycle endpoint is the single path              |
| Event on CANCELLED               | Create a new `RentalCancelled` domain event (record implementing `BikeRentalEvent`)           |
| Hold placement                   | Moved from `CreateRentalService` to `ActivateRentalUseCase`; DRAFT rentals must have no holds |
| Hold on cancel (ACTIVE)          | `FinanceFacade.releaseHold` returns the held amount to the customer's account                 |
| DEBT status                      | Unchanged — set only by `ReturnEquipmentService` when settlement exceeds prepayment           |
| State transitions                | DRAFT→ACTIVE, DRAFT→CANCELLED, ACTIVE→CANCELLED (all others invalid)                          |
