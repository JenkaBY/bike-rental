# System Design: FR-FIN-15 - Remove Deprecated Prepayment APIs

## 1. Architectural Overview

This story is a **pure deletion and cleanup** operation. Now that FR-FIN-14 has replaced the prepayment model with an
atomic hold-at-creation flow (`holdFunds` / `hasHold`), all artefacts that existed solely to support the old
prepayment path are dead code. Removing them shrinks the public API surface, eliminates the ambiguity between the old
and new finance flows, and enforces the correct module boundary where the Finance module is consumed only through the
hold-based `FinanceFacade` contract.

The net topological change is: the `POST /api/payments` endpoint and its entire backing stack — use-case interface,
service, DTOs, mapper, and the corresponding `FinanceFacade` methods — are removed. In the Rental module, the
`RecordPrepayment` use-case + service, its domain exceptions, the prepayment error-handling methods in the controller
advice, and the related Cucumber test artefacts are all deleted. The Liquibase changelog file that created the legacy
`payments` table schema (`v1/payments.create-table.xml`) is removed, and its include entry is dropped from the master
changelog. Localisation keys for the two removed error conditions are purged from all `messages*.properties` files.
No new components, data schema changes, or new contracts are introduced.

---

## 2. Impacted Components

* **`PaymentCommandController` (API — Finance module web layer):**
  Remove entirely. The `POST /api/payments` endpoint is no longer a valid operation; calls to this path must return
  `404 Not Found` after deletion.

* **`RecordPaymentUseCase` (Application contract — Finance module):**
  Remove the use-case interface. It is exclusively consumed by `PaymentCommandController`, which is also being removed.

* **`RecordPaymentService` (Service — Finance module — implementation of `RecordPaymentUseCase`):**
  Remove entirely. With the use-case interface gone, the implementation has no contract to fulfil.

* **`PaymentCommandMapper` (Mapper — Finance module web layer):**
  Remove entirely. The DTO-to-domain mapping for `RecordPaymentRequest` / `RecordPaymentResponse` has no remaining
  caller.

* **`RecordPaymentRequest` / `RecordPaymentResponse` (DTOs — Finance module web layer):**
  Remove both DTO records. They are exclusively consumed by the components being removed in this story.

* **`FinanceFacadeImpl` (Service — Finance module cross-module facade):**
  Remove the following methods and their interface declarations in `FinanceFacade`:
    - `recordPrepayment(...)`
    - `recordAdditionalPayment(...)`
    - `hasPrepayment(Long rentalId)`
    - `getPrepayment(Long rentalId)`
    - `getPayments(Long rentalId)`
      The facade retains only the methods introduced in FR-FIN-14 (`holdFunds`, `hasHold`, `captureHold`, `releaseHold`,
      settlement operations) and any query methods not listed above.

* **`RecordPrepaymentUseCase` (Application contract — Rental module):**
  Remove the use-case interface. Its sole purpose was to back the rental prepayment endpoint, which is now handled by
  the hold flow.

* **`RecordPrepaymentService` (Service — Rental module):**
  Remove entirely. All prepayment validation and delegation logic is dead code post FR-FIN-14.

* **`RentalCommandController` (API — Rental module):**
  Remove the `POST /api/rentals/{id}/prepayments` endpoint and its handler method. Remove the injection of
  `RecordPrepaymentUseCase`. No other endpoint on this controller is affected.

* **`PrepaymentRequiredException` / `InsufficientPrepaymentException` (Domain exceptions — Rental module):**
  Remove both exception classes. They were never re-thrown after FR-FIN-14 replaced the prepayment guard with
  `hasHold`.

* **`RentalRestControllerAdvice` (Error handler — Rental module web error layer):**
  Remove the `handlePrepaymentRequired` and `handleInsufficientPrepayment` `@ExceptionHandler` methods. With the
  exceptions gone, the handlers have no exception type to reference.

* **`RecordPrepaymentRequest` / `PrepaymentResponse` (DTOs — Rental module web layer):**
  Remove both DTO records. They are exclusively consumed by the prepayment endpoint being removed.

* **`RecordPrepaymentRequestTransformer` (Cucumber transformer — component-test module):**
  Remove the transformer class. It exists solely to support the deprecated prepayment step definitions.

* **`RentalWebSteps` (Cucumber step definitions — component-test module):**
  Remove all step-definition methods that reference the prepayment flow. Any scenario in `.feature` files that
  exercises `POST /api/rentals/{id}/prepayments` or the old payment recording endpoint must be deleted.

* **Liquibase changelog (`v1/payments.create-table.xml`):**
  Delete the file and remove its `<include>` entry from `db.changelog-master.xml`. The `payments` table used by
  the legacy finance flow is no longer required. Any initial-data changesets for that table must also be removed.

* **Localisation files (`messages.properties`, `messages_ru.properties`):**
  Remove the keys `error.rental.prepayment.required` and `error.rental.prepayment.insufficient` from both files.

---

## 3. Abstract Data Schema Changes

No new entities, attributes, or relationships are added.

* **Entity: `Payment` (legacy):**
  The `payments` table backing the old prepayment/additional-payment persistence model is not dropped at the
  database level in this story (historical data migration is out of scope). However, the Liquibase changelog file
  that *creates* the table (`v1/payments.create-table.xml`) is removed from the migration chain to prevent it from
  running in newly provisioned environments.

---

## 4. Component Contracts & Payloads

* **Interaction: `External HTTP Clients` → `PaymentCommandController` (removed)**
    * **Protocol:** REST
    * **Payload Changes:** This interaction is entirely removed. `POST /api/payments` will return `404 Not Found`
      because no handler mapping exists for it. The OpenAPI documentation must no longer list this path.

* **Interaction: `RentalCommandController` → `RecordPrepaymentUseCase` (removed)**
    * **Protocol:** In-process method call
    * **Payload Changes:** Removed. `POST /api/rentals/{id}/prepayments` endpoint and its `RecordPrepaymentRequest`
      payload are deleted.

* **Interaction: `RecordPrepaymentService` → `FinanceFacade.recordPrepayment` (removed)**
    * **Protocol:** In-process synchronous call (cross-module facade)
    * **Payload Changes:** Removed. `recordPrepayment(...)` is no longer a contract method on `FinanceFacade`.

* **Interaction: `ReturnEquipmentService` → `FinanceFacade.getPrepayment` (removed)**
    * **Protocol:** In-process synchronous call (cross-module facade)
    * **Payload Changes:** Removed. `ReturnEquipmentService` must be updated to use the new hold-based settlement
      path introduced by FR-FIN-14 instead of fetching a legacy prepayment record.

* **Interaction: `ReturnEquipmentService` → `FinanceFacade.recordAdditionalPayment` (removed)**
    * **Protocol:** In-process synchronous call (cross-module facade)
    * **Payload Changes:** Removed. The additional-payment recording operation against the old `Payment` model is
      deleted; settlement is now handled via `captureHold` / `releaseHold` operations defined in FR-FIN-14.

* **Interaction: `UpdateRentalService` → `FinanceFacade.hasPrepayment` (removed)**
    * **Protocol:** In-process synchronous call (cross-module facade)
    * **Payload Changes:** Removed. The guard was already replaced by `hasHold(rentalId)` in FR-FIN-14. The dead
      `hasPrepayment` call site is removed.

---

## 5. Updated Interaction Sequence

### Happy Path: `POST /api/payments` — endpoint removed

1. `External Client` calls `POST /api/payments`.
2. Spring MVC dispatcher finds no handler mapping for this path.
3. `404 Not Found` is returned to the client.
4. No application logic executes.

### Happy Path: Rental activation guard (after removal)

1. `RentalCommandController` receives `PATCH /api/rentals/{id}` with `status = ACTIVE`.
2. `UpdateRentalService` calls `FinanceFacade.hasHold(rentalId)` (introduced in FR-FIN-14).
3. If `hasHold` returns `false`, `HoldRequiredException` is thrown → `409 Conflict` response.
4. If `hasHold` returns `true`, rental transitions to `ACTIVE` and the response is `200 OK`.
5. All references to `hasPrepayment` and `PrepaymentRequiredException` are absent from the codebase.

### Happy Path: Equipment return (after removal)

1. `RentalCommandController` receives `POST /api/rentals/return`.
2. `ReturnEquipmentService` loads the `Rental` aggregate.
3. `ReturnEquipmentService` calls `TariffFacade.calculateRentalCost(...)` for final cost.
4. `ReturnEquipmentService` calls `FinanceFacade.captureHold(...)` / `FinanceFacade.releaseHold(...)` as defined by
   FR-FIN-14 — no calls to the removed `getPrepayment` or `recordAdditionalPayment` methods.
5. `ReturnEquipmentService` persists the updated `Rental` and publishes `RentalCompleted`.

### Build Verification

1. Developer runs `./gradlew build`.
2. The compiler resolves zero references to removed classes or methods.
3. All tests pass; no test exercises the `POST /api/payments` endpoint or any deleted Cucumber step.

---

## 6. Non-Functional Architecture Decisions

* **Security & Auth:**
  Removing `POST /api/payments` reduces the externally reachable endpoint surface by one command endpoint. No
  authentication configuration changes are required (auth is not yet active in the system), but the endpoint is
  simply unregistered. Removing dead exception-handler methods eliminates potential for unexpected HTTP response
  leakage from stale error-mapping logic.

* **Scale & Performance:**
  No performance considerations apply — this is a deletion story. Removing unused classes marginally reduces the
  size of the compiled application binary and the Spring application context.

* **Database Consistency:**
  The `payments` table is not dropped at the database level in this story. The Liquibase changelog file is removed
  so that the table will not be created in fresh environments provisioned after this story is applied. Environments
  that already ran the legacy changelog retain the table; a future data-cleanup story can drop it. This approach
  avoids a destructive migration and keeps rollback risk low.

* **Observability:**
  With the prepayment error keys removed from `messages.properties` and `messages_ru.properties`, any accidental
  future reference to those keys will produce a missing-key warning rather than a silently incorrect message — a
  useful regression signal.
