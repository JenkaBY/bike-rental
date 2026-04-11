# User Story: FR-FIN-15 - Remove Deprecated Prepayment APIs

## 1. Description

**As a** developer maintaining the codebase
**I want to** remove all deprecated prepayment APIs and their dependants
**So that** the codebase no longer carries dead code, the module boundary is clean, and no client can accidentally
call the old payment flow

## 2. Context & Business Rules

* **Trigger:** FR-FIN-14 has been fully implemented and all call sites of the deprecated APIs have been replaced
  with the new `holdFunds` / `hasHold` integration.
* **Prerequisite:** FR-FIN-14 must be complete before this story is started.
* **Rules Enforced:**
    * All items listed below must be removed; no stubs or empty implementations may remain.
    * Any component test, unit test, or Cucumber step that exercises the old prepayment flow must be deleted or
      rewritten to cover the equivalent scenario via the new rental-creation hold path.
    * The `POST /api/payments` endpoint (served by `PaymentCommandController`) must be removed; calling it after
      this story is applied must result in `404 Not Found`.

### Artefacts to Delete

#### Finance module — `FinanceFacade` interface and `FinanceFacadeImpl`

| Artefact                                             | Type             |
|------------------------------------------------------|------------------|
| `FinanceFacade.recordPrepayment(...)`                | Interface method |
| `FinanceFacade.recordAdditionalPayment(...)`         | Interface method |
| `FinanceFacade.hasPrepayment(Long rentalId)`         | Interface method |
| `FinanceFacade.getPrepayment(Long rentalId)`         | Interface method |
| `FinanceFacade.getPayments(Long rentalId)`           | Interface method |
| Corresponding implementations in `FinanceFacadeImpl` | Class methods    |

#### Finance module — web layer

| Artefact                                 | Type               |
|------------------------------------------|--------------------|
| `PaymentCommandController`               | REST controller    |
| `RecordPaymentRequest`                   | DTO record         |
| `RecordPaymentResponse`                  | DTO record         |
| `PaymentCommandMapper`                   | MapStruct mapper   |
| `RecordPaymentUseCase`                   | Use-case interface |
| Implementation of `RecordPaymentUseCase` | Service class      |

#### Rental module — application layer

| Artefact                  | Type               |
|---------------------------|--------------------|
| `RecordPrepaymentService` | Service class      |
| `RecordPrepaymentUseCase` | Use-case interface |

#### Rental module — domain exceptions

| Artefact                          | Type            |
|-----------------------------------|-----------------|
| `PrepaymentRequiredException`     | Exception class |
| `InsufficientPrepaymentException` | Exception class |

#### Rental module — web error handling

| Artefact                                                               | Type                       |
|------------------------------------------------------------------------|----------------------------|
| `handlePrepaymentRequired` handler in `RentalRestControllerAdvice`     | `@ExceptionHandler` method |
| `handleInsufficientPrepayment` handler in `RentalRestControllerAdvice` | `@ExceptionHandler` method |

#### Rental module — web layer (DTOs)

| Artefact                  | Type       |
|---------------------------|------------|
| `RecordPrepaymentRequest` | DTO record |
| `PrepaymentResponse`      | DTO record |

#### Component tests

| Artefact                                                            | Type                 |
|---------------------------------------------------------------------|----------------------|
| `RecordPrepaymentRequestTransformer`                                | Cucumber transformer |
| Cucumber steps referencing `prepayment` in `RentalWebSteps`         | Step definitions     |
| Any `.feature` file scenario that calls the old prepayment endpoint | Cucumber feature     |

## 3. Non-Functional Requirements (NFRs)

* **Performance:** N/A — deletion story.
* **Security/Compliance:** Removing the endpoint reduces the API attack surface. Ensure no other module holds a
  reference to any deleted class; the build must succeed with zero compilation warnings related to removed symbols.
* **Usability/Other:** After removal, the API documentation (OpenAPI) must no longer list the `POST /api/payments`
  path.

## 4. Acceptance Criteria (BDD)

**Scenario 1: Deprecated endpoint removed**

* **Given** the application is started after FR-FIN-15 is applied
* **When** a client calls `POST /api/payments`
* **Then** the response is `404 Not Found`

**Scenario 2: Build is clean**

* **Given** FR-FIN-15 changes are applied
* **When** `./gradlew build` is executed
* **Then** the build succeeds with no compilation errors
* **And** all existing tests pass

**Scenario 3: No deprecated method references remain**

* **Given** FR-FIN-15 changes are applied
* **When** the codebase is searched for `hasPrepayment`, `recordPrepayment`, `recordAdditionalPayment`,
  `PrepaymentRequiredException`, `InsufficientPrepaymentException`
* **Then** zero matches are found outside of version-control history

**Scenario 4: Legacy Liquibase changelog files are physically removed**

* **Given** FR-FIN-15 changes are applied
* **Then** `v1/payments.create-table.xml` is deleted from the project
* **And** its `<include>` entry is removed from `db.changelog-master.xml`
* **And** the message keys `error.rental.prepayment.required` and `error.rental.prepayment.insufficient`
  are removed from `messages.properties` and `messages_ru.properties`

## 5. Out of Scope

* Any new functionality. This is a pure deletion / cleanup story.
* Changes to the new `holdFunds` or `hasHold` implementations (covered by FR-FIN-14).
* Database migration to remove legacy `prepayment` data (addressed separately if historical data cleanup is needed).
