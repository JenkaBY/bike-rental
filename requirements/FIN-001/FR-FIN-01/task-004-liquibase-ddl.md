# Task 004: Liquibase DDL Changesets — finance_accounts & finance_sub_ledgers Tables

> **Applied Skill:** `liquibase/SKILL.md` — One logical change per file; `preConditions onFail="MARK_RAN"`;
`TIMESTAMP WITH TIME ZONE` for audit columns; index creation in the same changeset as table creation; unique constraint
> for idempotency guarantee.

## 1. Objective

Create two Liquibase DDL changesets that define the `finance_accounts` and `finance_sub_ledgers` tables. Register
both in `db.changelog-master.xml` before the existing provisioning data section. The `finance_sub_ledgers` changeset
must also add the FK constraint, the `account_id` index, and the unique constraint on `(account_id, ledger_type)`.

## 2. Files to Create / Modify

### File 1 — DDL for finance_accounts

* **File Path:** `service/src/main/resources/db/changelog/v1/finance_accounts.create-table.xml`
* **Action:** Create New File if does not exist

### File 2 — DDL for finance_sub_ledgers

* **File Path:** `service/src/main/resources/db/changelog/v1/finance_sub_ledgers.create-table.xml`
* **Action:** Create New File if does not exist

### File 3 — Master changelog

* **File Path:** `service/src/main/resources/db/changelog/db.changelog-master.xml`
* **Action:** Modify Existing File

---

## 3. Code Implementation

### finance_accounts.create-table.xml

**Code to Add/Replace:**

* **Location:** New file — full changeset body.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog
        xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
        http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-4.20.xsd">

    <changeSet id="finance_accounts.create-table" author="copilot">
        <preConditions onFail="MARK_RAN">
            <not>
                <tableExists tableName="finance_accounts"/>
            </not>
        </preConditions>

        <createTable tableName="finance_accounts">
            <column name="id" type="UUID">
                <constraints primaryKey="true" nullable="false"/>
            </column>
            <column name="account_type" type="varchar(20)">
                <constraints nullable="false"/>
            </column>
            <column name="customer_id" type="UUID"/>
            <column name="created_at" type="TIMESTAMP WITH TIME ZONE">
                <constraints nullable="false"/>
            </column>
        </createTable>
    </changeSet>

</databaseChangeLog>
```

---

### finance_sub_ledgers.create-table.xml

**Code to Add/Replace:**

* **Location:** New file — full changeset body.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog
        xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
        http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-4.20.xsd">

    <changeSet id="finance_sub_ledgers.create-table" author="copilot">
        <preConditions onFail="MARK_RAN">
            <not>
                <tableExists tableName="finance_sub_ledgers"/>
            </not>
        </preConditions>

        <createTable tableName="finance_sub_ledgers">
            <column name="id" type="UUID">
                <constraints primaryKey="true" nullable="false"/>
            </column>
            <column name="account_id" type="UUID">
                <constraints nullable="false"/>
            </column>
            <column name="ledger_type" type="varchar(30)">
                <constraints nullable="false"/>
            </column>
            <column name="balance" type="decimal(19,2)">
                <constraints nullable="false"/>
            </column>
            <column name="created_at" type="TIMESTAMP WITH TIME ZONE">
                <constraints nullable="false"/>
            </column>
            <column name="updated_at" type="TIMESTAMP WITH TIME ZONE">
              <constraints nullable="true"/>
            </column>
        </createTable>

        <addForeignKeyConstraint
                baseTableName="finance_sub_ledgers"
                baseColumnNames="account_id"
                constraintName="fk_finance_sub_ledgers_finance_accounts"
                referencedTableName="finance_accounts"
                referencedColumnNames="id"/>

        <createIndex tableName="finance_sub_ledgers" indexName="idx_finance_sub_ledgers_account_id">
            <column name="account_id"/>
        </createIndex>

        <addUniqueConstraint
                tableName="finance_sub_ledgers"
                columnNames="account_id,ledger_type"
                constraintName="uq_finance_sub_ledgers_account_ledger"/>
    </changeSet>

</databaseChangeLog>
```

---

### db.changelog-master.xml — add two DDL includes and two provisioning includes

**Code to Add/Replace:**

* **Location:** In `db.changelog-master.xml`, add the two DDL `<include>` lines immediately **after** the existing
  `v1/tariffs_v2.create-table.xml` include, and the two provisioning `<include>` lines at the **bottom** of the
  provisioning data section, after `data/tariffs_v2-provisioning.xml`. The resulting block looks like:

```xml

<include relativeToChangelogFile="true" file="v1/tariffs_v2.create-table.xml"/>
<include relativeToChangelogFile="true" file="v1/finance_accounts.create-table.xml"/>
<include relativeToChangelogFile="true" file="v1/finance_sub_ledgers.create-table.xml"/>
        <!--    provisioning data -->
<include relativeToChangelogFile="true" file="data/equipment_types-provisioning.xml"/>
<include relativeToChangelogFile="true" file="data/equipment_statuses-provisioning.xml"/>
<include relativeToChangelogFile="true" file="data/equipment_status_transition_rules-provisioning.xml"/>
<include relativeToChangelogFile="true" file="data/tariffs-provisioning.xml"/>
<include relativeToChangelogFile="true" file="data/tariffs_v2-provisioning.xml"/>
<include relativeToChangelogFile="true" file="data/finance_accounts-provisioning.xml"/>
<include relativeToChangelogFile="true" file="data/finance_sub_ledgers-provisioning.xml"/>
```

## 4. Validation Steps

```bash
./gradlew :service:test "-Dspring.profiles.active=test" --tests BikeRentalApplicationTest
```
