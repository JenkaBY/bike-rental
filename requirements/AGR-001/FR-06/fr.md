# User Story: FR-06 - Activation Only Through Agreement Signing (Breaking Change)

## 1. Description

**As a** business owner
**I want** rentals to become ACTIVE exclusively through the agreement signing flow
**So that** no rental can start without a signed agreement on record

## 2. Context & Business Rules

* **Trigger:** Deployment of this final AGR-001 increment (after FR-01..FR-05 are in production
  and the frontend has switched to the signing flow).
* **Rules Enforced:**
    * The domain transition `DRAFT → ACTIVE` is removed; the only path to `ACTIVE` is
      `AWAITING_SIGNATURE → ACTIVE` performed by `RentalSigningFacade.completeSigning` (agreement
      module, FR-05).
    * `PATCH /api/rentals/{id}/lifecycles` no longer accepts `status = ACTIVE` (request
      validation rejects it).
    * The legacy direct-activation code path (`ActivateRentalService`, hold-at-activation) is
      removed; funds are held exclusively at `DRAFT → AWAITING_SIGNATURE` (FR-02).
    * The deprecated JSON-Patch update path must no longer be able to activate a rental.
    * All existing behavior downstream of ACTIVE (return, debt, cancel with hold release) is
      unchanged.

## 3. Non-Functional Requirements (NFRs)

* **Performance:** N/A.
* **Security/Compliance:** closes the compliance gap of unsigned active rentals.
* **Usability/Other:** BREAKING API change — coordinated with the frontend; shipped last.

## 4. Acceptance Criteria (BDD)

**Scenario 1: Lifecycle endpoint rejects ACTIVE**

* **Given** a ready draft rental
* **When** the operator PATCHes lifecycles with `status = ACTIVE`
* **Then** the response is `400` (request validation) and the rental remains `DRAFT` with no hold

**Scenario 2: Signing remains the working path**

* **Given** a draft rental moved to `AWAITING_SIGNATURE` and an ACTIVE agreement template
* **When** the customer signs (FR-05 flow)
* **Then** the rental becomes `ACTIVE` and downstream flows (return, cancel with hold release)
  behave as before

**Scenario 3: Existing post-activation features stay green**

* **Given** the component-test suite
* **When** all rental scenarios that previously activated via lifecycles are migrated to the
  signing flow (shared steps)
* **Then** the whole suite passes

## 5. Out of Scope

* Any frontend changes.
* Migration of historical data (existing ACTIVE rentals without signatures stay valid).
* Removal of the deprecated JSON-Patch endpoint itself (only its activation capability is neutralized).
