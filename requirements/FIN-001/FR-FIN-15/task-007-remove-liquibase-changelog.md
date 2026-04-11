# Task 007: Remove Liquibase Payments Changelog

> **Applied Skill:** `liquibase` — Liquibase changelog management conventions.

## 1. Objective

Delete the `v1/payments.create-table.xml` Liquibase changelog file and remove its `<include>` entry from the master
changelog. This prevents the legacy `payments` table from being created in any freshly provisioned environment after
this story is applied. Existing environments that have already executed the changelog retain the table; a future
data-cleanup story will drop it if needed.

---

## 2. Files to Modify / Create

* **File Path:** `service/src/main/resources/db/changelog/v1/payments.create-table.xml`
* **Action:** Delete file entirely.

---

* **File Path:** `service/src/main/resources/db/changelog/db.changelog-master.xml`
* **Action:** Modify existing file — remove one `<include>` element.

---

## 3. Code Implementation

### 3.1 Delete `v1/payments.create-table.xml`

Delete the file. No replacement is needed.

---

### 3.2 Modify `db.changelog-master.xml` — remove the payments include entry

**Location:** `service/src/main/resources/db/changelog/db.changelog-master.xml`

Find and remove the following line (it appears between the `v1/tariffs.create-table.xml` and
`v1/equipment_status_transition_rules.create-table.xml` includes):

```xml
    <include relativeToChangelogFile="true" file="v1/payments.create-table.xml"/>
```

After the removal the surrounding block should read:

```xml
    <include relativeToChangelogFile="true" file="v1/tariffs.create-table.xml"/>
    <include relativeToChangelogFile="true" file="v1/equipment_status_transition_rules.create-table.xml"/>
```

---

## 4. Validation Steps

```bash
./gradlew :service:compileJava "-Dspring.profiles.active=test"
```

Expected result: `BUILD SUCCESSFUL`. Liquibase does not execute during compilation; this step verifies the project
continues to compile with the updated resource files.
