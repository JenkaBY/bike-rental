# User Story: FR-03 - Agreement Template Management (Admin)

## 1. Description

**As an** administrator
**I want to** create, edit, activate, list and delete versions of the rental agreement text
**So that** exactly one agreed-upon agreement version is active at any time and every future signature can reference the precise text edition that was signed

## 2. Context & Business Rules

* **Trigger:** Admin uses the agreement management UI (`/api/agreements/**`).
* **Rules Enforced:**
    * Template lifecycle is strictly linear: `DRAFT → ACTIVE → DEACTIVATED`; no backward
      transitions, no reactivation ("republishing" = copy text into a new DRAFT).
    * Only DRAFT templates may be edited (`title`, `content`) or deleted.
    * At most one ACTIVE template exists — enforced by a partial unique DB index.
    * Deactivation happens ONLY as a side effect of activating another version; there is no
      standalone deactivate operation. After the first activation an active version always exists.
    * `versionNumber` is assigned at activation as `max + 1` (drafts have none);
      `contentSha256` is fixed at activation.
    * An active version is immutable; activating a new version does NOT require re-signing
      existing signatures (they pin `templateId` + `templateContentSha256`).
    * Concurrent activation by two admins: exactly one wins; the loser gets `409`.

## 3. Non-Functional Requirements (NFRs)

* **Performance:** list endpoint must not fetch `content` (TEXT) — lightweight projection.
* **Security/Compliance:** endpoints permitAll (project-wide approach as of today).
* **Usability/Other:** error responses follow the project `ProblemDetail` convention
  (`correlationId`, `errorCode`).

## 4. Acceptance Criteria (BDD)

**Scenario 1: Create and activate the first template**

* **Given** no templates exist
* **When** admin POSTs `{title, content}` and then PATCHes `/{id}/activate`
* **Then** the template becomes `ACTIVE` with `versionNumber = 1`, `activatedAt` set and
  `contentSha256` computed, and `GET /api/agreements/active` returns it with content

**Scenario 2: Activation archives the previous active version**

* **Given** an ACTIVE template v1 and a DRAFT template
* **When** admin activates the DRAFT
* **Then** the DRAFT becomes `ACTIVE` with `versionNumber = 2`, the former active becomes
  `DEACTIVATED` with `deactivatedAt` set, and no unique-index violation occurs

**Scenario 3: Editing a non-draft template is rejected**

* **Given** an ACTIVE (or DEACTIVATED) template
* **When** admin PATCHes `{title, content}` on it
* **Then** the response is `409` and the template is unchanged

**Scenario 4: Deleting a non-draft template is rejected**

* **Given** an ACTIVE template
* **When** admin DELETEs it
* **Then** the response is `409`

**Scenario 5: Activating a non-draft template is rejected**

* **Given** a DEACTIVATED template
* **When** admin PATCHes `/{id}/activate`
* **Then** the response is `409`

**Scenario 6: No active template**

* **Given** only DRAFT templates exist
* **When** the client calls `GET /api/agreements/active`
* **Then** the response is `404`

## 5. Out of Scope

* PDF rendering and preview (FR-04).
* Signatures (FR-05).
* Any rental module changes.
