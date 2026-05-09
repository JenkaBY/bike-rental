# System Design: FR-01 — Add Free-Text Search Parameter to Equipment List Endpoint

## 1. Architectural Overview

FR-01 extends the existing read path of the `equipment` module by threading a new optional `q` query parameter through
the web, application, and domain-repository layers. No new component is introduced; existing components are extended
with minimal, additive changes.

The `EquipmentQueryController` acquires the new parameter and passes it to the application layer via an updated
`SearchEquipmentsQuery`. The `SearchEquipmentsService` forwards it unchanged to the domain-repository port
`EquipmentRepository.findAll()`, whose signature gains a `searchText` argument. The infrastructure adapter
(`EquipmentRepositoryAdapter`) will translate this value into filter predicates (covered in FR-02). The response
shape, pagination, and error handling are all unchanged.

## 2. Impacted Components

* **`EquipmentQueryController` (API):** Must accept the new optional `q` query parameter on the
  `GET /api/equipments` handler. Must pass its value to the mapper's `toSearchQuery` method. OpenAPI annotation
  on the `q` parameter must be added describing its semantics with an example value.

* **`EquipmentQueryMapper` (Web Query Mapper):** The `toSearchQuery` mapping method must accept a third input
  argument `searchText` (the value of `q`) and map it to the new `searchText` field on
  `SearchEquipmentsQuery`.

* **`SearchEquipmentsUseCase` (Application Use-Case Contract):** The inner record `SearchEquipmentsQuery` must
  gain a new `searchText` field (nullable String). Callers that currently construct a `SearchEquipmentsQuery`
  must be updated to supply the new field.

* **`SearchEquipmentsService` (Application Service):** Must forward `query.searchText()` to the
  `EquipmentRepository.findAll()` call. No business logic is applied here; the value is propagated as-is.

* **`EquipmentRepository` (Domain Port):** The `findAll(String statusSlug, String typeSlug, PageRequest)` method
  signature must be extended to `findAll(String statusSlug, String typeSlug, String searchText, PageRequest)`.
  The domain layer defines no behaviour for this field; it is an opaque filter hint for the infrastructure adapter.

## 3. Abstract Data Schema Changes

No schema changes. This story reads from the existing `equipments` table and uses the already-persisted `uid`,
`serial_number`, and `model` columns as match targets.

## 4. Component Contracts & Payloads

* **Interaction: External Client → `EquipmentQueryController`**
    * **Protocol:** HTTP GET
    * **Payload Changes:** New optional query parameter `q` (String). When absent or blank the text filter is
      suppressed. Existing `status`, `type`, and pagination parameters are unchanged.

* **Interaction: `EquipmentQueryController` → `SearchEquipmentsUseCase`**
    * **Protocol:** In-process method call
    * **Payload Changes:** `SearchEquipmentsQuery` record gains field `searchText` (nullable String).

* **Interaction: `SearchEquipmentsService` → `EquipmentRepository`**
    * **Protocol:** In-process method call (domain port)
    * **Payload Changes:** `findAll()` gains a `searchText` (nullable String) argument positioned before
      `PageRequest`.

## 5. Updated Interaction Sequence

**Happy path — with `q` provided:**

1. External client sends `GET /api/equipments?q=bike&status=available`.
2. `EquipmentQueryController` receives `q="bike"`, `status="available"`, `type=null`, `Pageable`.
3. `EquipmentQueryController` calls `mapper.toSearchQuery("available", null, "bike", pageable)`.
4. `EquipmentQueryMapper` produces
   `SearchEquipmentsQuery(statusSlug="available", typeSlug=null, searchText="bike", pageRequest=…)`.
5. `EquipmentQueryController` calls `searchUseCase.execute(query)`.
6. `SearchEquipmentsService` calls `repository.findAll("available", null, "bike", pageRequest)`.
7. `EquipmentRepositoryAdapter` (FR-02) builds and executes the filtered query and returns a `Page<Equipment>`.
8. `EquipmentQueryController` maps the domain page to `Page<EquipmentResponse>` and returns `200 OK`.

**Happy path — without `q`:**

1. External client sends `GET /api/equipments`.
2. Steps 2–4 produce `SearchEquipmentsQuery(statusSlug=null, typeSlug=null, searchText=null, pageRequest=…)`.
3. Steps 5–8 proceed normally; `searchText=null` causes no text filter to be applied.

**Unhappy path — no matching equipment:**

1. External client sends `GET /api/equipments?q=xyz999`.
2. Flow proceeds as in the happy path.
3. `EquipmentRepositoryAdapter` returns an empty page.
4. `EquipmentQueryController` returns `200 OK` with `content=[]` and `totalElements=0`.

## 6. Non-Functional Architecture Decisions

* **Security & Auth:** `q` is passed to the persistence layer as a typed bind parameter via the Specification API
  (FR-02). No string interpolation into SQL occurs at any layer boundary, preventing SQL injection.
* **Scale & Performance:** The text filter is evaluated in a single SQL query alongside other active predicates. No
  additional round-trips or in-memory filtering occur. Index usage on `uid`, `serial_number`, and `model` columns is
  outside the scope of this story but may be considered separately if query performance degrades under high cardinality.
