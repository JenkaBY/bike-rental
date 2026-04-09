# Task 002: Add Liquibase Index on finance_transactions

> **Applied Skill:** `liquibase` — Add Indexes for Performance Optimization

## 1. Objective

Add a composite index on `(customer_id, recorded_at DESC)` on the `finance_transactions` table to satisfy the
reverse-chronological pagination performance requirement (first page ≤ 2 seconds for up to 10 000 rows).

## 2. File to Modify / Create

* **File Path:** `service/src/main/resources/db/changelog/v1/finance_transactions.create-table.xml`
* **Action:** Modify Existing File

## 3. Code Implementation

**Location:** Inside the existing `<changeSet id="finance_transactions.create-table" ...>` block, directly after the
closing `</createIndex>` tag of the existing `idx_finance_transactions_customer_id` index and before the closing
`</changeSet>` tag.

**Snippet to insert:**

```xml
        <createIndex indexName="idx_finance_transactions_customer_id_recorded_at"
                     tableName="finance_transactions">
            <column name="customer_id"/>
            <column name="recorded_at" descending="true"/>
        </createIndex>
```

**Full resulting `</changeSet>` tail for reference:**

```xml
        <createIndex indexName="idx_finance_transactions_customer_id"
                     tableName="finance_transactions">
            <column name="customer_id"/>
        </createIndex>

        <createIndex indexName="idx_finance_transactions_customer_id_recorded_at"
                     tableName="finance_transactions">
            <column name="customer_id"/>
            <column name="recorded_at" descending="true"/>
        </createIndex>
    </changeSet>
```

> `db.changelog-master.xml` does **not** need to be changed — the existing include entry for
> `v1/finance_transactions.create-table.xml` already covers this file.

## 4. Validation Steps

```bash
./gradlew :service:test "-Dspring.profiles.active=test,docker" --tests BikeRentalApplicationTest
```

The application context must start without Liquibase errors.
