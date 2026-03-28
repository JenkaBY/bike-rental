# Task 004: Liquibase Changeset — Alter `finance_transactions` Table

> **Applied Skill:** `liquibase` — `addColumn` workflow, `dropNotNullConstraint` for existing column, file naming
> convention, master changelog include ordering rule.

## 1. Objective

Apply two structural DDL changes to the `finance_transactions` table:

1. Drop the `NOT NULL` constraint on `payment_method` so that `ADJUSTMENT` transaction rows can omit it.
2. Add a new nullable `reason` column (VARCHAR 1000) that stores the mandatory adjustment explanation.

## 2. File to Modify / Create

**Create** the new changeset file:

* **File Path:**
  `service/src/main/resources/db/changelog/v1/finance_transactions.update-table_add-reason-nullable-payment-method.xml`
* **Action:** Create New File

**Modify** the master changelog to include the new changeset last in the DDL section:

* **File Path:** `service/src/main/resources/db/changelog/db.changelog-master.xml`
* **Action:** Modify Existing File

## 3. Code Implementation

### 3a. New changeset file

**Snippet** — full file content:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
        xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
        http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-4.20.xsd">

    <changeSet id="finance_transactions.update-table_nullable-payment-method"
               author="copilot">
        <preConditions onFail="MARK_RAN">
            <tableExists tableName="finance_transactions"/>
        </preConditions>

        <dropNotNullConstraint tableName="finance_transactions"
                               columnName="payment_method"
                               columnDataType="VARCHAR(30)"/>
    </changeSet>

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

### 3b. Register in `db.changelog-master.xml`

* **Location:** Inside `db.changelog-master.xml`, add the new include immediately after the existing
  `finance_transaction_records.create-table.xml` include and before the `<!--  provisioning data -->` comment.

Replace:

```xml
    <include relativeToChangelogFile="true" file="v1/finance_transaction_records.create-table.xml"/>
    <!--    provisioning data -->
```

With:

```xml
    <include relativeToChangelogFile="true" file="v1/finance_transaction_records.create-table.xml"/>
    <include relativeToChangelogFile="true"
             file="v1/finance_transactions.update-table_add-reason-nullable-payment-method.xml"/>
    <!--    provisioning data -->
```

## 4. Validation Steps

```bash
./gradlew :service:compileJava
```

To also verify Liquibase parses the new changeset without errors, run the component tests (requires Docker/DB up):

```bash
./gradlew :component-test:test "-Dspring.profiles.active=test,docker"
```
