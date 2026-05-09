# User Story: FR-03 — Remove Unused Fields from SearchEquipmentsRequest DTO

## 1. Description

**As a** backend developer reviewing the equipment module  
**I want to** remove the `uid` and `serialNumber` fields from the `SearchEquipmentsRequest` DTO  
**So that** the DTO accurately reflects the actual search parameters accepted by the endpoint and does not carry dead
fields that are never populated by the controller

## 2. Context & Business Rules

* **Trigger:** FR-01 introduces the `q` parameter which supersedes the per-field `uid` and `serialNumber` inputs that
  were planned but never wired into the controller.
* **Rules Enforced:**
    * `SearchEquipmentsRequest` must retain the `status` and `type` fields, which correspond to existing active filter
      parameters.
    * The `uid` and `serialNumber` fields must be removed entirely from the record; they are not referenced by the
      controller or any production code.
    * No new field is added to this DTO as part of this story; the `q` parameter is handled directly via `@RequestParam`
      in the controller (consistent with `status` and `type`).
    * No controller, use case, service, or mapper class references `SearchEquipmentsRequest`; the removal is therefore
      safe and compilation will confirm correctness.

## 3. Non-Functional Requirements (NFRs)

* **Performance:** N/A
* **Security/Compliance:** N/A
* **Usability/Other:** After removal, the DTO must still compile cleanly; any auto-generated code (MapStruct,
  Lombok) must not be affected.

## 4. Acceptance Criteria (BDD)

**Scenario 1: DTO compiles without uid and serialNumber fields**

* **Given** `SearchEquipmentsRequest` is updated to remove `uid` and `serialNumber`
* **When** the project is compiled (`./gradlew :service:compileJava`)
* **Then** compilation succeeds with zero errors related to the DTO

**Scenario 2: No production code references the removed fields**

* **Given** the fields are removed
* **When** a workspace-wide search is performed for `SearchEquipmentsRequest` field access
* **Then** no usage of `.uid()` or `.serialNumber()` on a `SearchEquipmentsRequest` instance is found in production
  source

## 5. Out of Scope

* Deleting `SearchEquipmentsRequest` entirely — it may be used in the future or serves as documentation of available
  query shape.
* Adding the `q` field to `SearchEquipmentsRequest` — the controller handles `q` via `@RequestParam` directly.
* Modifying any other DTO or record.
