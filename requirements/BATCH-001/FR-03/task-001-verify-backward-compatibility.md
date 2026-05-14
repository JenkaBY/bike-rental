# Task 001: Verify Backward Compatibility of Existing Endpoints

> **Applied Skill:** N/A — verification-only task; no code changes are made; all implementation is
> delivered additively in FR-01 and FR-02.

## 1. Objective

Confirm that the additive changes introduced by FR-01 and FR-02 have not altered the behaviour of any
pre-existing handler in `EquipmentQueryController` or `CustomerQueryController`. This task produces no
code changes — it exists solely to run the full controller test suites and confirm a green build.

**Prerequisites:** All tasks in FR-01 (tasks 001–003) and FR-02 (tasks 001–007) must be complete and
compiling before executing this task.

## 2. File to Modify / Create

* **File Path:** N/A — no files are created or modified.
* **Action:** Verification Only

## 3. Code Implementation

**Imports Required:** N/A

**Code to Add/Replace:** None.

**What to verify manually before running tests:**

1. Open `EquipmentQueryController` and confirm that the following methods are **byte-for-byte identical**
   to their pre-FR-01 state (i.e., only the new `getBatchEquipments` method and the updated constructor
   were added):
    * `getEquipmentById`
    * `getEquipmentByUid`
    * `getEquipmentBySerial`
    * `searchEquipments`

2. Open `CustomerQueryController` and confirm that the following methods are **byte-for-byte identical**
   to their pre-FR-02 state:
    * `getAll`
    * `getById`

## 4. Validation Steps

Run the full controller test suite for both modules:

```bash
./gradlew :service:test "-Dspring.profiles.active=test" --tests EquipmentQueryControllerTest
```

```bash
./gradlew :service:test "-Dspring.profiles.active=test" --tests CustomerQueryControllerTest
```

Both commands must exit with `BUILD SUCCESSFUL` and zero test failures. Any failure in a pre-existing
test (i.e., a test that existed before this feature branch) constitutes a backward-compatibility regression
and must be investigated and fixed before this task can be considered complete.
