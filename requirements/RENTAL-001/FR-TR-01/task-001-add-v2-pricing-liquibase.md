# Task 001: Add V2 Pricing Columns to `rentals` Table

> **Applied Skill:** `.github/skills/liquibase/SKILL.md` — Add columns workflow, naming conventions, preconditions

## 1. Objective

Add three new nullable columns to the `rentals` table that store the V2 pricing overrides (`special_tariff_id`,
`special_price`, `discount_percent`) so the `Rental` aggregate can persist them at creation and read them back at
settlement time without operator re-entry.

## 2. File to Modify / Create

* **File Path:** `service/src/main/resources/db/changelog/v1/rentals.create-table.xml`
* **Action:** Modify Existing File

## 3. Code Implementation

**Imports Required:** N/A (XML file)

**Code to Add/Replace:**

* **Location:** Inside the existing `<createTable tableName="rentals">` block, directly **after** the
  `<column name="updated_at" .../>` column (the last column currently defined).

Existing end of the `<createTable>` block:

```xml
            <column name="updated_at" type="TIMESTAMP WITH TIME ZONE"/>
        </createTable>
```

Replace with:

```xml
            <column name="updated_at" type="TIMESTAMP WITH TIME ZONE"/>
            <column name="special_tariff_id" type="BIGINT"/>
            <column name="special_price" type="NUMERIC(19,4)"/>
            <column name="discount_percent" type="SMALLINT"/>
        </createTable>
```

No changes to `db.changelog-master.xml` are needed — `v1/rentals.create-table.xml` is already included.

## 4. Validation Steps

```bash
./gradlew :service:compileJava
```
