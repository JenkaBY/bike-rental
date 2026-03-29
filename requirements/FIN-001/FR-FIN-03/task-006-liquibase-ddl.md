# Task 006: Liquibase DDL — Create finance_transactions and finance_transaction_records Tables

> **Applied Skill:** `liquibase` — XML changeset authoring, FK constraints, composite index, master changelog
> registration.

## 1. Objective

Add two new DDL Liquibase changeset files that create the `finance_transactions` and
`finance_transaction_records` tables, then register them in the master changelog immediately after the
`finance_sub_ledgers` changeset.

## 2. Files to Create / Modify

| # | File Path | Action |
|---|-----------|--------|
| 1 | `service/src/main/resources/db/changelog/v1/finance_transactions.create-table.xml` | Create New File |
| 2 | `service/src/main/resources/db/changelog/v1/finance_transaction_records.create-table.xml` | Create New File |
| 3 | `service/src/main/resources/db/changelog/db.changelog-master.xml` | Modify Existing File |

## 3. Code Implementation

### File 1 — `finance_transactions.create-table.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog
        xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
        http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-4.20.xsd">

    <changeSet id="finance_transactions.create-table" author="copilot">
        <preConditions onFail="MARK_RAN">
            <not>
                <tableExists tableName="finance_transactions"/>
            </not>
        </preConditions>

        <createTable tableName="finance_transactions">
            <column name="id" type="UUID">
                <constraints primaryKey="true" nullable="false"/>
            </column>
            <column name="transaction_type" type="varchar(30)">
                <constraints nullable="false"/>
            </column>
            <column name="payment_method" type="varchar(30)">
                <constraints nullable="false"/>
            </column>
            <column name="amount" type="DECIMAL(19,2)">
                <constraints nullable="false"/>
            </column>
            <column name="customer_id" type="UUID">
                <constraints nullable="false"/>
            </column>
            <column name="operator_id" type="varchar(255)">
                <constraints nullable="false"/>
            </column>
            <column name="source_type" type="varchar(30)"/>
            <column name="source_id" type="varchar(255)"/>
            <column name="recorded_at" type="TIMESTAMP WITH TIME ZONE">
                <constraints nullable="false"/>
            </column>
            <column name="idempotency_key" type="UUID">
                <constraints nullable="false"/>
            </column>
        </createTable>

        <addUniqueConstraint
                tableName="finance_transactions"
                columnNames="idempotency_key, customer_id"
                constraintName="uq_finance_transactions_idempotency_key_customer_id"/>

        <sql>
            ALTER TABLE finance_transactions
                ADD CONSTRAINT chk_finance_transactions_amount CHECK (amount > 0);
        </sql>

        <createIndex indexName="idx_finance_transactions_customer_id"
                     tableName="finance_transactions">
            <column name="customer_id"/>
        </createIndex>
    </changeSet>

</databaseChangeLog>
```

### File 2 — `finance_transaction_records.create-table.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog
        xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
        http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-4.20.xsd">

    <changeSet id="finance_transaction_records.create-table" author="copilot">
        <preConditions onFail="MARK_RAN">
            <not>
                <tableExists tableName="finance_transaction_records"/>
            </not>
        </preConditions>

        <createTable tableName="finance_transaction_records">
            <column name="id" type="UUID">
                <constraints primaryKey="true" nullable="false"/>
            </column>
            <column name="transaction_id" type="UUID">
                <constraints nullable="false"/>
            </column>
            <column name="sub_ledger_id" type="UUID">
                <constraints nullable="false"/>
            </column>
            <column name="ledger_type" type="varchar(30)">
                <constraints nullable="false"/>
            </column>
            <column name="direction" type="varchar(10)">
                <constraints nullable="false"/>
            </column>
            <column name="amount" type="DECIMAL(19,2)">
                <constraints nullable="false"/>
            </column>
        </createTable>

        <sql>
            ALTER TABLE finance_transaction_records
                ADD CONSTRAINT chk_finance_transaction_records_amount CHECK (amount > 0);
        </sql>

        <addForeignKeyConstraint
                baseTableName="finance_transaction_records"
                baseColumnNames="transaction_id"
                constraintName="fk_transaction_records_transactions"
                referencedTableName="finance_transactions"
                referencedColumnNames="id"/>

        <createIndex indexName="idx_finance_transaction_records_transaction_id"
                     tableName="finance_transaction_records">
            <column name="transaction_id"/>
        </createIndex>
    </changeSet>

</databaseChangeLog>
```

### Step 3 — Register in master changelog

**File:** `service/src/main/resources/db/changelog/db.changelog-master.xml`

**Location:** After the line `<include relativeToChangelogFile="true" file="v1/finance_sub_ledgers.create-table.xml"/>` 
and before the `<!--    provisioning data -->` comment.

**Add these two lines:**

```xml
    <include relativeToChangelogFile="true" file="v1/finance_transactions.create-table.xml"/>
    <include relativeToChangelogFile="true" file="v1/finance_transaction_records.create-table.xml"/>
```

The relevant section in `db.changelog-master.xml` should look like this after the edit:
```xml
    <include relativeToChangelogFile="true" file="v1/finance_accounts.create-table.xml"/>
    <include relativeToChangelogFile="true" file="v1/finance_sub_ledgers.create-table.xml"/>
    <include relativeToChangelogFile="true" file="v1/finance_transactions.create-table.xml"/>
    <include relativeToChangelogFile="true" file="v1/finance_transaction_records.create-table.xml"/>
    <!--    provisioning data -->
```

## 4. Validation Steps

```bash
./gradlew :service:compileJava
```

Compilation must pass. The Liquibase XML files are not executed during unit compilation; they will be validated
during component tests in Task 011.
