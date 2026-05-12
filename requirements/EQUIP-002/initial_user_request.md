# Initial User Request — EQUIP-002

## Original Request

Refactor the `equipment` module to remove rental-related statuses from the `status_slug` column.
After this change:

- `equipment` module owns only **physical condition** of equipment.
- `rental` module owns **operational availability** (who has what and when).
- Dependency direction is strictly one-way: **rental → equipment**.
  The equipment module must have zero knowledge of the rental module.

---

## Equipment Module Scope

### Database

- Add column `condition_slug VARCHAR(50) NOT NULL DEFAULT 'GOOD'` to the `equipments` table.
  Valid values: GOOD, MAINTENANCE, BROKEN, DECOMMISSIONED.
- Do NOT drop `status_slug` column — it stays for now as-is.

### Domain

- Create `Condition` enum in the `shared` module with values:
  GOOD, MAINTENANCE, BROKEN, DECOMMISSIONED.
- Map `condition_slug` to the `Condition` enum in the `Equipment` entity.

### Application

- Comment out (do not delete) all rental lifecycle event listeners in the equipment module.
  Add a TODO comment explaining they are disabled during this refactoring phase.
- Create a new method on the existing equipment Facade:
  `List<EquipmentInfo> getEquipmentsByConditions(Set<Condition> conditions, EquipmentSearchFilter filter)`
  Filter parameters: uid, model, serialNumber (all optional, applied as OR).

### API

- No new endpoints in the equipment module.

---

## Design Decisions Confirmed During Analysis

| Decision                                       | Resolution                                     |
|------------------------------------------------|------------------------------------------------|
| Facade return type                             | Reuse existing `EquipmentInfo` record          |
| `Condition` location                           | `shared` module                                |
| `EquipmentSearchFilter` semantics              | OR across uid / model / serialNumber           |
| `status_slug` staleness after listener disable | Accepted as transitional state                 |
| `EquipmentInfo.isAvailable()`                  | Left as-is; removal deferred to follow-up task |
