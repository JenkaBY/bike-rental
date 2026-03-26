# Task 005: Liquibase Data Provisioning — System Account & Sub-Ledger Seed Data

> **Applied Skill:** `liquibase/SKILL.md` — `loadData` with CSV files for initial data provisioning;
`sqlCheck preCondition` for idempotent inserts.

## 1. Objective

Create the CSV seed files and the two Liquibase `loadData` provisioning changesets that insert the singleton System
Account row and its five sub-ledger rows. Both changesets are skipped on subsequent runs via `sqlCheck`
preConditions.

The System Account is assigned the well-known fixed UUID `00000000-0000-0000-0000-000000000001` so that the
sub-ledger provisioning can reference it without a dynamic lookup.

## 2. Files to Create

### File 1 — System Account CSV

* **File Path:** `service/src/main/resources/db/changelog/data/data/finance_accounts.csv`
* **Action:** Create New File

### File 2 — Sub-ledger CSV

* **File Path:** `service/src/main/resources/db/changelog/data/data/finance_sub_ledgers.csv`
* **Action:** Create New File

### File 3 — Account provisioning changeset

* **File Path:** `service/src/main/resources/db/changelog/data/finance_accounts-provisioning.xml`
* **Action:** Create New File

### File 4 — Sub-ledger provisioning changeset

* **File Path:** `service/src/main/resources/db/changelog/data/finance_sub_ledgers-provisioning.xml`
* **Action:** Create New File

---

## 3. Code Implementation

### finance_accounts.csv

**Code to Add/Replace:**

* **Location:** New file — full content.

```csv
id,account_type,customer_id,created_at
00000000-0000-0000-0000-000000000001,SYSTEM,,NOW+0d
```

> **Note:** `customer_id` is intentionally blank (NULL) for the System Account.

---

### finance_sub_ledgers.csv

**Code to Add/Replace:**

* **Location:** New file — full content.  
  Fixed UUIDs `00000000-0000-0001-0000-00000000000{1-5}` are used to ensure idempotent re-runs; `balance` is
  `0.00` (decimal 19,2).

```csv
id,account_id,ledger_type,balance,created_at,updated_at
00000000-0000-0001-0000-000000000001,00000000-0000-0000-0000-000000000001,CASH,0.00,NOW+0d,NOW+0d
00000000-0000-0001-0000-000000000002,00000000-0000-0000-0000-000000000001,CARD_TERMINAL,0.00,NOW+0d,NOW+0d
00000000-0000-0001-0000-000000000003,00000000-0000-0000-0000-000000000001,BANK_TRANSFER,0.00,NOW+0d,NOW+0d
00000000-0000-0001-0000-000000000004,00000000-0000-0000-0000-000000000001,REVENUE,0.00,NOW+0d,NOW+0d
00000000-0000-0001-0000-000000000005,00000000-0000-0000-0000-000000000001,ADJUSTMENT,0.00,NOW+0d,NOW+0d
```

---

### finance_accounts-provisioning.xml

**Code to Add/Replace:**

* **Location:** New file — full changeset body.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
        xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
        http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-4.20.xsd">

    <changeSet id="finance_accounts.provision-data" author="copilot" context="!test">
        <preConditions onFail="MARK_RAN">
            <tableExists tableName="finance_accounts"/>
            <sqlCheck expectedResult="0">SELECT COUNT(*) FROM finance_accounts WHERE account_type = 'SYSTEM'</sqlCheck>
        </preConditions>

        <loadData tableName="finance_accounts"
                  file="data/finance_accounts.csv"
                  relativeToChangelogFile="true">
            <column name="id" type="uuid"/>
            <column name="account_type" type="string"/>
            <column name="customer_id" type="uuid"/>
            <column name="created_at" type="datetime"/>
        </loadData>
    </changeSet>

</databaseChangeLog>
```

---

### finance_sub_ledgers-provisioning.xml

**Code to Add/Replace:**

* **Location:** New file — full changeset body.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
        xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
        http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-4.20.xsd">

    <changeSet id="finance_sub_ledgers.provision-data" author="copilot" context="!test">
        <preConditions onFail="MARK_RAN">
            <tableExists tableName="finance_sub_ledgers"/>
            <sqlCheck expectedResult="0">SELECT COUNT(*) FROM finance_sub_ledgers WHERE account_id =
                '00000000-0000-0000-0000-000000000001'
            </sqlCheck>
        </preConditions>

        <loadData tableName="finance_sub_ledgers"
                  file="data/finance_sub_ledgers.csv"
                  relativeToChangelogFile="true">
            <column name="id" type="uuid"/>
            <column name="account_id" type="uuid"/>
            <column name="ledger_type" type="string"/>
            <column name="balance" type="numeric"/>
            <column name="created_at" type="datetime"/>
            <column name="updated_at" type="datetime"/>
        </loadData>
    </changeSet>

</databaseChangeLog>
```

## 4. Validation Steps

```bash
./gradlew :service:compileJava "-Dspring.profiles.active=test"
```
