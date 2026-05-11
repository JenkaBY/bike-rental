# User Story: FR-05 — Expose `conditionSlug` in Equipment REST API Response

## 1. Description

**As an** API consumer (front-end or integration client),
**I want to** receive the physical condition slug of equipment in every equipment response payload,
**So that** I can display or filter equipment by physical condition without making additional calls.

## 2. Context & Business Rules

* **Trigger:** FR-01 (adds `condition_slug` DB column) and FR-02 (adds `Condition conditionSlug` to
  the `Equipment` domain aggregate) must be completed before this story is implemented.
* **Rules Enforced:**
    - `EquipmentResponse` gains a new `String conditionSlug` field serialized as the enum constant
      name (e.g. `"GOOD"`, `"MAINTENANCE"`, `"BROKEN"`, `"DECOMMISSIONED"`).
    - The new field is present in **all** existing equipment endpoints that return `EquipmentResponse`:
        - `GET /api/equipments` (paged list)
        - `GET /api/equipments/{id}`
        - `GET /api/equipments/by-uid/{uid}`
        - `GET /api/equipments/by-serial/{serialNumber}`
        - `POST /api/equipments` (create response)
        - `PUT /api/equipments/{id}` (update response)
    - The field must be annotated with `@Schema` (OpenAPI description and example value).
    - The existing `String condition` field (free-text notes) is NOT removed or renamed — both
      fields coexist in `EquipmentResponse`.
    - The `EquipmentQueryMapper.toResponse(Equipment)` mapping must compile cleanly with
      `unmappedTargetPolicy=ERROR`.

## 3. Non-Functional Requirements (NFRs)

* **Performance:** N/A — no additional DB query; value is already loaded with the domain object.
* **Security/Compliance:** N/A — read-only field, no PII.
* **Usability/Other:** The JSON key name `conditionSlug` must match the Swagger schema field name
  exactly.

## 4. Acceptance Criteria (BDD)

**Scenario 1: Get equipment by ID includes `conditionSlug`**

* **Given** an equipment record exists in the database with `condition_slug = 'GOOD'`
* **When** a `GET /api/equipments/{id}` request is made for that equipment
* **Then** the response body contains `"conditionSlug": "GOOD"`

**Scenario 2: Paged list includes `conditionSlug` for every item**

* **Given** multiple equipment records exist with various `condition_slug` values
* **When** a `GET /api/equipments` request is made
* **Then** each item in the `items` array contains the correct `conditionSlug` value

**Scenario 3: Create equipment response includes `conditionSlug`**

* **Given** a valid create-equipment request is submitted
* **When** a `POST /api/equipments` request is made
* **Then** the `201` response body includes the `conditionSlug` field reflecting the newly persisted
  condition

## 5. Out of Scope

* Adding `conditionSlug` as a request filter parameter on `GET /api/equipments`
  (that is handled by FR-03 via `EquipmentFacade.getEquipmentsByConditions`).
* Renaming or removing the existing `condition` (free-text notes) field.
* Changing how `conditionSlug` is stored or validated in the command layer
  (`POST`/`PUT` request bodies are out of scope for this story).
