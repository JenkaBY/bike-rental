# User Story: FR-1 — Flat fee tariff: calendar-day billing rule

## 1. Description

**As a** billing system computing flat fee rental costs  
**I want to** count the number of distinct calendar dates spanned by a rental period (using the server's configured
timezone)  
**So that** customers who rent overnight are charged correctly for every calendar day the equipment was in their
possession

---

## 2. Context & Business Rules

* **Trigger:** Any rental cost calculation that involves a `FLAT_FEE` tariff.
* **Current behaviour (bug):** The number of billable days is calculated as
  `ceil(durationMinutes / 1440)`. A 12-hour overnight rental (8 PM → 8 AM) produces `ceil(720 / 1440) = 1 day`,
  which is incorrect because the equipment was held across two calendar dates.
* **Correct rule:** Count the number of **distinct calendar dates** in the half-open interval
  `[startAt.toLocalDate(timezone), returnAt.toLocalDate(timezone)]` (inclusive on both ends).
* **Minimum 1 day:** Any rental with a zero or negative duration is still charged 1 day (existing guard).
* **Timezone:** Calendar date boundaries are evaluated using the server's configured timezone (not UTC, not a
  client-supplied timezone).
* **Scope:** This rule applies **only** to the `FLAT_FEE` pricing type. All other pricing types (`FLAT_HOURLY`,
  `DAILY`, `DEGRESSIVE_HOURLY`, `SPECIAL`) are unaffected.

---

## 3. Non-Functional Requirements (NFRs)

* **Performance:** No additional I/O required; computation is in-memory.
* **Security/Compliance:** N/A
* **Usability/Other:** The calculation breakdown message returned in the response must reflect the correct day count.

---

## 4. Acceptance Criteria (BDD)

**Scenario 1: Overnight rental spanning two calendar dates**

* **Given** a `FLAT_FEE` tariff with an issuance fee of 10.00
* **When** rental `startAt` is June 1 at 20:00 and `returnAt` is June 2 at 08:00 (same timezone)
* **Then** the calculated cost is 20.00 (2 days × 10.00)
* **And** the breakdown reports 2 billable days

**Scenario 2: Same-day rental**

* **Given** a `FLAT_FEE` tariff with an issuance fee of 10.00
* **When** rental `startAt` is June 1 at 08:00 and `returnAt` is June 1 at 18:00
* **Then** the calculated cost is 10.00 (1 day × 10.00)
* **And** the breakdown reports 1 billable day

**Scenario 3: Multi-day rental spanning three calendar dates**

* **Given** a `FLAT_FEE` tariff with an issuance fee of 10.00
* **When** rental `startAt` is June 1 at 20:00 and `returnAt` is June 3 at 20:00
* **Then** the calculated cost is 30.00 (3 days × 10.00: June 1, June 2, June 3)
* **And** the breakdown reports 3 billable days

**Scenario 4: Zero or negative duration guard**

* **Given** a `FLAT_FEE` tariff
* **When** the computed duration is zero or negative (e.g. `returnAt` equals `startAt`)
* **Then** the calculated cost is 1 × issuanceFee (minimum 1 day)

---

## 5. Out of Scope

* Changes to `FLAT_HOURLY`, `DAILY`, `DEGRESSIVE_HOURLY`, or `SPECIAL` tariff cost calculation logic.
* Introduction of a customer-supplied or branch-level timezone override.
* Retroactive correction of already-persisted costs on existing rentals.
