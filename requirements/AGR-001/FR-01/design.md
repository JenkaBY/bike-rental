# System Design: FR-01 — Rental Optimistic Locking & Version in API

## 1. Architectural Overview

This story introduces JPA optimistic locking on the `rentals` aggregate root and exposes the lock
counter (`version`) through the rental REST API. No new endpoints, services, or business rules are
added. The version value flows read-only through all layers:

```
rentals.version (DB) → RentalJpaEntity.version (@Version) → Rental.version (domain, pass-through)
→ RentalResponse.version (web DTO)
```

Concurrent-update conflicts surface as `ObjectOptimisticLockingFailureException`, which is already
mapped to `HTTP 409` / `errorCode = shared.resource.optimistic_lock` by the global
`CoreExceptionHandlerAdvice` — no error-handling changes are required.

---

## 2. Impacted Components

* **Liquibase changelog** *(new file)* — `service/src/main/resources/db/changelog/v1/rentals.update-table_add-version-column.xml`:
  adds `version BIGINT NOT NULL DEFAULT 0` to `rentals`, guarded by
  `<preConditions onFail="MARK_RAN"><not><columnExists .../></not></preConditions>`,
  `author="claude"`, changeset id `rentals.update-table_add-version-column`. Registered at the
  bottom of `db.changelog-master.xml`.

* **`RentalJpaEntity`** (`rental/infrastructure/persistence/entity/`):
  gains
  ```java
  @Version
  @Column(name = "version", nullable = false)
  private Long version;
  ```
  ⚠️ Spring Data treats an entity with a `@Version` field as *new* when the version is `null`
  (`persist` instead of `merge`). All existing update flows load the aggregate via
  `RentalRepository.findById → toDomain`, mutate, then `save → toEntity`, so the version value
  round-trips through the domain object and this contract holds. The domain must therefore carry
  the field (next bullet) — otherwise every save of an existing rental would fail.

* **`Rental` (domain aggregate — `rental/domain/model/`):**
  gains a read-only `private Long version;` field (exposed via the class-level `@Getter`,
  included in `@Builder`). ⚠️ The hand-written `RentalBuilder.build()` override calls the private
  all-args constructor explicitly — the new field must be added to that argument list (position
  matching the field declaration order used by Lombok's `@AllArgsConstructor`). No domain method
  touches `version`.

* **`RentalJpaMapper`** (`rental/infrastructure/persistence/mapper/`):
  no code change expected — MapStruct maps `version ↔ version` by name in both directions
  (`unmappedTargetPolicy=ERROR` will fail the build if the mapping is missed, which is the
  desired safety net).

* **`RentalResponse`** (`rental/web/query/dto/`):
  record gains a `Long version` component (placed after `id` for readability).

* **`RentalQueryMapper`** (`rental/web/query/mapper/`):
  no explicit mapping needed — `Rental.version → RentalResponse.version` maps by name.

---

## 3. Abstract Data Schema Changes

* **Table `rentals`:** add column `version BIGINT NOT NULL DEFAULT 0`. Existing rows receive `0`.
  No index changes.

---

## 4. Component Contracts & Payloads

* **Interaction: HTTP Client → rental controllers (all existing endpoints returning `RentalResponse`)**
    * **Protocol:** REST / HTTP (unchanged paths)
    * **Payload Changes:** every `RentalResponse` JSON now contains `"version": <number>`.
    * **Error Response (pre-existing, now reachable for rentals):**
      ```
      HTTP 409 Conflict
      {
        "status": 409,
        "title": "Optimistic lock",
        "detail": "Concurrent update — please retry",
        "correlationId": "<uuid>",
        "errorCode": "shared.resource.optimistic_lock"
      }
      ```

* **Interaction: `RentalRepositoryAdapter` → `RentalJpaRepository`**
    * **Protocol:** In-process / JPA
    * **Payload Changes:** `save()` now performs a versioned UPDATE (`... WHERE id = ? AND version = ?`).
      A lost race throws `ObjectOptimisticLockingFailureException` out of the adapter.

---

## 5. Updated Interaction Sequence

### Happy path — draft update increments version

1. Client sends `PUT /api/rentals/{id}` with draft changes.
2. `CreateOrUpdateDraftRentalService` loads the rental (`version = N` round-trips into the domain).
3. Mutation + `rentalRepository.save(rental)` → Hibernate issues `UPDATE ... SET version = N+1 WHERE id = ? AND version = N`.
4. Response `RentalResponse.version = N+1`.

### Unhappy path — concurrent modification

1. Transactions A and B both load rental with `version = N`.
2. A commits first → row now has `version = N+1`.
3. B's UPDATE matches zero rows → Hibernate throws `ObjectOptimisticLockingFailureException`.
4. `CoreExceptionHandlerAdvice.handleOptimisticLockException` returns `HTTP 409`
   `shared.resource.optimistic_lock`. No partial state persists (transaction rolled back).

---

## 6. Non-Functional Architecture Decisions

* **Backward compatibility:** additive JSON field + defaulted DB column — safe to merge and deploy
  independently; existing clients and rows are unaffected.
* **Consistency:** optimistic locking is the foundation for the signing fencing token (FR-02/FR-05);
  the version is deliberately *not* accepted as input on any existing endpoint in this story.
* **Testing strategy:** business behavior (version exposure, increment) verified via Cucumber
  component tests on existing rental features; no unit tests on domain/service classes (project
  rule). Concurrency conflict path relies on the pre-existing global handler and Hibernate
  machinery; a dedicated component-test scenario is not required for merge.
