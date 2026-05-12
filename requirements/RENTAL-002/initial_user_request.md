# Initial User Request — RENTAL-002

## Original Request

Add equipment availability querying to the rental module so that:

- The rental module is the single source of truth for operational availability (who has which equipment
  and when).
- A new `GET /rentals/available-equipments` endpoint allows callers to find equipment that is both
  physically fit (GOOD condition) and not currently in an active or assigned rental.
- The existing `BaseRequestedEquipmentValidator` is updated to use the new rental-owned availability
  check instead of reading `status_slug` from the equipment module.

---

## Rental Module Scope

### Database

- Add `UNIQUE INDEX idx_rental_equipments_one_active ON rental_equipments(equipment_id) WHERE status IN
  ('ACTIVE', 'ASSIGNED')` — enforces one active/assigned rental per equipment at the DB level.

### Domain / Application

- Create `RentalEquipmentRepository` port with a method that returns `Set<Long>` of equipment IDs where
  status is ACTIVE or ASSIGNED, filtered to an input set of candidate IDs.
- Create `EquipmentAvailabilityService` (internal to rental module) wrapping the repository query.
- Update `BaseRequestedEquipmentValidator#validateAvailability` to use the new service. Signature
  unchanged (`List<EquipmentInfo>`); IDs are extracted internally.
- Create `GetAvailableForRentEquipmentsUseCase` interface and `GetAvailableForRentEquipmentsService`
  implementation.
  Logic:
    1. Call `EquipmentFacade.getEquipmentsByConditions(Set.of(GOOD), filter)` for physical candidates.
    2. If empty — return immediately.
    3. Extract Long IDs, call `EquipmentAvailabilityService.getUnavailableIds(Set<Long>)`.
    4. Filter out unavailable IDs, return remaining equipment as a page.

### API

- `GET /rentals/available-equipments` with optional query params: `uid`, `model`, `serialNumber`,
  plus standard pagination params. Returns paged list of `EquipmentInfo`.

---

## Design Decisions Confirmed During Analysis

| Decision                           | Resolution                                                                                 |
|------------------------------------|--------------------------------------------------------------------------------------------|
| Repository ID type                 | `Long` (equipment_id DB PK)                                                                |
| Redundant non-unique partial index | Dropped — unique partial index is sufficient                                               |
| `validateAvailability` signature   | Unchanged — extracts IDs internally                                                        |
| Pagination approach                | Best-effort: page size may be smaller than requested due to post-fetch filter              |
| Error for unavailable equipment    | HTTP 409 Conflict; `errorCode: EQUIPMENT_NOT_AVAILABLE`; `unavailableIds` in ProblemDetail |
