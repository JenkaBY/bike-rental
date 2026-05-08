# User Story: FR-002 - System-backed clock implementation (shared)

## 1. Description

**As a** platform operator / developer
**I want to** have a system-backed implementation of the virtual clock available
**So that** in production and non-dev/test environments all modules observe true system time and behaviour remains
correct.

## 2. Context & Business Rules

* **Trigger:** The application starts in any profile other than `dev` or `test`.
* **Rules Enforced:**
    * The system-backed provider returns the real current instant obtained from the underlying operating system/JVM.
    * No dev-only controller or mutable clock behavior must be present in non-dev/test profiles.

## 3. Non-Functional Requirements (NFRs)

* **Performance:** Minimal overhead compared to a direct call to system time.
* **Security/Compliance:** N/A.
* **Usability/Other:** Seamless swap between system and dev implementations depending on active profile.

## 4. Acceptance Criteria (BDD)

**Scenario 1: System provider returned in production**

* **Given** the app runs under a non-dev/test profile
* **When** a module obtains the virtual time provider
* **Then** calls to the provider return real system time values

**Scenario 2: No dev controller present**

* **Given** the app runs under `prod` profile
* **When** the application context loads
* **Then** the dev-time controller and mutable clock beans are not instantiated

## 5. Out of Scope

* Exact bean registration mechanism and annotations — implementation detail.
