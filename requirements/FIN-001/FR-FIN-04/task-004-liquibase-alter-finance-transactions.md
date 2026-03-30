# Task 004: Liquibase Changeset — Alter `finance_transactions` Table

> **Applied Skill:** `liquibase` — `addColumn` workflow, modifying an existing changeset file.

## 1. Objective

Apply one structural DDL change to the `finance_transactions` table:

1. Add a new nullable `reason` column (VARCHAR 1000) that stores the mandatory adjustment explanation.

`payment_method` is stored as `varchar(30)` (not a PostgreSQL enum), so `INTERNAL_TRANSFER` is simply a new
application-level string value. The column remains `NOT NULL`; no DDL change is required for it.

## 2. File to Modify

**Modify** the existing changeset file to remove the stale `dropNotNullConstraint` changeSet and keep only
the `addColumn` changeSet for `reason`:

* **File Path:**
  `service/src/main/resources/db/changelog/v1/finance_transactions.update-table_add-reason-nullable-payment-method.xml`
* **Action:** Modify Existing File

The master changelog (`service/src/main/resources/db/changelog/db.changelog-master.xml`) already includes
this file — **no change required there**.

## 3. Code Implementation

### 3a. Updated changeset file

The file currently contains two `<changeSet>` blocks. **Delete the first one** (id:
`finance_transactions.update-table_nullable-payment-method`) entirely — it drops the NOT NULL constraint on
`payment_method`, which is no longer required. **Keep the second one** unchanged.

**Snippet** — full file content after edit:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
        xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
        http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-4.20.xsd">

    <changeSet id="finance_transactions.update-table_add-reason-column"
               author="copilot">
        <preConditions onFail="MARK_RAN">
            <not>
                <columnExists tableName="finance_transactions" columnName="reason"/>
            </not>
        </preConditions>

        <addColumn tableName="finance_transactions">
            <column name="reason" type="VARCHAR(1000)">
                <constraints nullable="true"/>
            </column>
        </addColumn>
    </changeSet>

</databaseChangeLog>
```

> **Note:** `db.changelog-master.xml` already includes this file — no change needed there.

## 4. Validation Steps

```bash
./gradlew :service:compileJava
```

To also verify Liquibase parses the new changeset without errors, run the component tests (requires Docker/DB up):

```bash
./gradlew :component-test:test "-Dspring.profiles.active=test,docker"
```
