# Task 001: Remove V1 Liquibase Changelogs

> **Applied Skill:** `liquibase` — FR-TR-03 acceptance criterion 5 requires physical deletion of
`v1/tariffs.create-table.xml`
> and `data/tariffs-provisioning.xml` and removal of their `<include>` entries from
> `db.changelog-master.xml`.

## 1. Objective

Delete the two V1 tariff Liquibase changeset files and remove their `<include>` references from the master
changelog. After this task the Liquibase migration no longer creates or seeds the legacy `tariffs` table.

## 2. Files to Modify / Delete

| Action          | File Path                                                               |
|-----------------|-------------------------------------------------------------------------|
| **Delete file** | `service/src/main/resources/db/changelog/v1/tariffs.create-table.xml`   |
| **Delete file** | `service/src/main/resources/db/changelog/data/tariffs-provisioning.xml` |
| **Modify file** | `service/src/main/resources/db/changelog/db.changelog-master.xml`       |

## 3. Implementation

### 3a. Delete the two changeset files

Physically remove the following files from the repository:

```
service/src/main/resources/db/changelog/v1/tariffs.create-table.xml
service/src/main/resources/db/changelog/data/tariffs-provisioning.xml
```

Do **not** touch any `tariffs_v2` changeset files — they must remain.

### 3b. Remove `<include>` entries from `db.changelog-master.xml`

**File:** `service/src/main/resources/db/changelog/db.changelog-master.xml`
**Action:** Remove (delete lines)

Remove the following two lines from the file (exact text):

```xml
    <include relativeToChangelogFile="true" file="v1/tariffs.create-table.xml"/>
```

```xml
    <include relativeToChangelogFile="true" file="data/tariffs-provisioning.xml"/>
```

The exact resulting file after both removals (for reference — the order of the remaining lines must not change):

```xml
<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
        xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-3.5.xsd">

    <!-- add changelogs files here -->
    <include relativeToChangelogFile="true" file="v1/event_publication.create-table.xml"/>
    <include relativeToChangelogFile="true" file="v1/event_publication_archive.create-table.xml"/>
    <include relativeToChangelogFile="true" file="v1/customers.create-table.xml"/>

    <include relativeToChangelogFile="true" file="v1/equipment_types.create-table.xml"/>
    <include relativeToChangelogFile="true" file="v1/equipment_statuses.create-table.xml"/>
    <include relativeToChangelogFile="true" file="v1/equipments.create-table.xml"/>
    <include relativeToChangelogFile="true" file="v1/equipment_status_transition_rules.create-table.xml"/>
    <include relativeToChangelogFile="true" file="v1/rentals.create-table.xml"/>
    <include relativeToChangelogFile="true" file="v1/rental-equipments.create-table.xml"/>
    <include relativeToChangelogFile="true" file="v1/tariffs_v2.create-table.xml"/>
    <include relativeToChangelogFile="true" file="v1/finance_accounts.create-table.xml"/>
    <include relativeToChangelogFile="true" file="v1/finance_sub_ledgers.create-table.xml"/>
    <include relativeToChangelogFile="true" file="v1/finance_transactions.create-table.xml"/>
    <include relativeToChangelogFile="true" file="v1/finance_transaction_records.create-table.xml"/>
    <!--    provisioning data -->
    <include relativeToChangelogFile="true" file="data/equipment_types-provisioning.xml"/>
    <include relativeToChangelogFile="true" file="data/equipment_statuses-provisioning.xml"/>
    <include relativeToChangelogFile="true" file="data/equipment_status_transition_rules-provisioning.xml"/>
    <include relativeToChangelogFile="true" file="data/tariffs_v2-provisioning.xml"/>
    <include relativeToChangelogFile="true" file="data/finance_accounts-provisioning.xml"/>
    <include relativeToChangelogFile="true" file="data/finance_sub_ledgers-provisioning.xml"/>

</databaseChangeLog>
```

> **Constraint:** The `v1/tariffs_v2.create-table.xml` and `data/tariffs_v2-provisioning.xml` entries
> **must remain** — they belong to the V2 tariff schema which is out of scope for this story.

## 4. Validation Steps

```bash
./gradlew :service:compileJava
```

Expected: BUILD SUCCESSFUL — no compilation errors. The changelog XML change has no Java compilation
impact; this step merely confirms no accidental Java file was altered.
