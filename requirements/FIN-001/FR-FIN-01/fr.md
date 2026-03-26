# User Story: FR-FIN-01 - System Account Provisioning

## 1. Description

**As a** system operator
**I want to** have a System Account with all required sub-ledgers automatically provisioned at application startup
**So that** every financial transaction has a valid system-side account to record against from day one

## 2. Context & Business Rules

* **Trigger:** Application startup sequence.
* **Rules Enforced:**
    * Exactly one System Account must exist at all times; provisioning is idempotent — re-running startup must not
      create a duplicate.
    * The System Account must own five sub-ledgers upon provisioning: `CASH` (Asset), `CARD_TERMINAL` (Asset),
      `BANK_TRANSFER` (Asset), `REVENUE` (Income), and `ADJUSTMENT` (Control).
    * The System Account has no customer owner and must never be linked to any customer identity.
    * All sub-ledger balances start at zero.
    * If the System Account already exists, startup must complete without error and without modifying existing data.

## 3. Non-Functional Requirements (NFRs)

* **Performance:** Provisioning must complete within the normal Spring context startup time; no user-visible latency
  impact.
* **Security/Compliance:** The System Account and its sub-ledgers must not be accessible via any customer-facing API.
  All provisioning activity must be logged for audit purposes.
* **Usability/Other:** Provisioning failure must prevent application startup with a clear error message to the operator.

## 4. Acceptance Criteria (BDD)

**Scenario 1: First-time startup — System Account is created**

* **Given** no System Account exists in the database
* **When** the application starts
* **Then** exactly one System Account is created
* **And** it owns sub-ledgers: `CASH`, `CARD_TERMINAL`, `BANK_TRANSFER`, `REVENUE`, and `ADJUSTMENT`
* **And** all sub-ledger balances equal zero

**Scenario 2: Subsequent startup — idempotent provisioning**

* **Given** a System Account with all sub-ledgers already exists
* **When** the application starts again
* **Then** no duplicate System Account or sub-ledger is created
* **And** existing balances are unchanged
* **And** the application starts successfully

**Scenario 3: Provisioning failure halts startup**

* **Given** the database is unavailable during provisioning
* **When** the application tries to start
* **Then** the application fails to start with a descriptive error message

## 5. Out of Scope

* Manual creation or modification of the System Account via an API.
* Multiple system accounts (e.g., per-branch or per-region accounts).
* Adding or removing sub-ledger types at runtime.
