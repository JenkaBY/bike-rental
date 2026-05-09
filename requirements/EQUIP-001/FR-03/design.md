# System Design: FR-03 — Remove Unused Fields from SearchEquipmentsRequest DTO

## 1. Architectural Overview

FR-03 is a localised cleanup within the web query layer of the `equipment` module. The `SearchEquipmentsRequest`
record was created with `uid` and `serialNumber` fields that anticipated a per-field search approach that was never
wired into the controller. With FR-01 introducing the unified `q` parameter, these fields are definitively dead code.

Removing them keeps the DTO an accurate, self-documenting contract. Because no production component outside the
class definition references these two fields, the change is mechanical and carries no behavioural risk.

## 2. Impacted Components

* **`SearchEquipmentsRequest` (Web Query DTO):** The `uid` and `serialNumber` record components must be removed.
  The record retains `status` and `type`. The resulting record accurately represents the two categorical filter
  parameters accepted by the endpoint.

## 3. Abstract Data Schema Changes

None. This story touches only an in-memory data transfer object; no database schema, JPA entity, or Liquibase
changeset is affected.

## 4. Component Contracts & Payloads

* **Interaction: internal — `SearchEquipmentsRequest` definition**
    * **Protocol:** N/A (DTO, not a network boundary)
    * **Payload Changes:** Fields `uid` (String) and `serialNumber` (String) removed from the record. Fields
      `status` (String) and `type` (String) retained unchanged. No consumer of this type in production code is
      affected; the type is not referenced by any controller, use-case, service, or mapper method.

## 5. Updated Interaction Sequence

No runtime interaction sequence changes. `SearchEquipmentsRequest` is not instantiated or referenced by any
production call path at the time of this change.

**Compile-time verification flow:**

1. `uid` and `serialNumber` record components are deleted from `SearchEquipmentsRequest`.
2. Gradle compiles the `service` module.
3. The compiler confirms zero usages of the removed components in production source.
4. All existing unit and component tests continue to pass unchanged.

## 6. Non-Functional Architecture Decisions

* **Security & Auth:** N/A — no network boundary or persistence interaction is involved.
* **Scale & Performance:** N/A — the change is a pure compile-time cleanup with no runtime footprint.
