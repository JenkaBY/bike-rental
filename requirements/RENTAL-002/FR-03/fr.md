# User Story: FR-03 — Create `EquipmentAvailabilityService` (Internal Rental Service)

## 1. Description

**As a** developer in the rental module,
**I want to** have an internal application service that encapsulates the availability look-up logic,
**So that** multiple use cases within the rental module can determine which equipment is occupied without
duplicating repository calls or coupling directly to the persistence layer.

## 2. Context & Business Rules

* **Trigger:** FR-02 must be complete (the repository method must exist) before this story is
  implemented.
* **Rules Enforced:**
    - The service is **internal to the rental module** — it must not be exposed via `RentalFacade` or
      any other public Facade interface.
    - It must not be placed in a Spring Modulith public package; Spring Modulith must not allow other
      modules to inject it directly.
    - Public contract (method signature):
      ```
      Set<Long> getUnavailableIds(Set<Long> equipmentIds)
      ```
    - The method delegates entirely to `RentalEquipmentRepository.findOccupiedEquipmentIds(equipmentIds)`.
    - If `equipmentIds` is empty the method returns an empty set immediately (consistent with FR-02).
    - The service is annotated with `@Service` (or equivalent) and is injectable within the rental module.
    - No caching is applied at this stage (availability data changes frequently).

## 3. Non-Functional Requirements (NFRs)

* **Performance:** The service adds no overhead beyond the repository call; it is a thin delegation layer.
* **Security/Compliance:** N/A — internal service with no external surface.
* **Usability/Other:** Following the hexagonal architecture convention, the service implementation
  lives in `rental/application/service/`. If a port interface is warranted (for testability), it lives
  in `rental/domain/service/`.

## 4. Acceptance Criteria (BDD)

**Scenario 1: Returns occupied IDs from the repository**

* **Given** the repository returns `{1L, 3L}` for the input `{1L, 2L, 3L}`
* **When** `getUnavailableIds(Set.of(1L, 2L, 3L))` is called on the service
* **Then** the service returns `{1L, 3L}`

**Scenario 2: Empty input short-circuits**

* **Given** an empty set is passed
* **When** `getUnavailableIds(Set.of())` is called
* **Then** the service returns an empty set; the repository is NOT called

**Scenario 3: Service is not accessible from outside the rental module**

* **Given** Spring Modulith application context verification is run
* **When** module boundaries are checked
* **Then** no type from `customer`, `equipment`, `finance`, or `tariff` modules can directly reference
  `EquipmentAvailabilityService`

## 5. Out of Scope

* Caching availability results — deferred.
* Exposing availability check via any Facade — availability is a rental-internal concept.
* Time-range filtering — not in scope at this stage.
