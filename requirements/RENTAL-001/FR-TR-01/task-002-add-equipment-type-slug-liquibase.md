# Task 002: Add `equipment_type_slug` Column to `rental_equipments` Table

> **Applied Skill:** `.github/skills/liquibase/SKILL.md` — Add columns workflow, naming conventions, preconditions

## 1. Objective

Add a new non-nullable `equipment_type_slug` column to the `rental_equipments` table. This stores the equipment
type identifier that TariffV2 requires to select the correct tariff pool, both during creation (FR-TR-01) and at
settlement time (FR-TR-02).

## 2. File to Modify / Create

* **File Path:** `service/src/main/resources/db/changelog/v1/rental-equipments.create-table.xml`
* **Action:** Modify Existing File

## 3. Code Implementation

**Imports Required:** N/A (XML file)

**Code to Add/Replace:**

* **Location:** Inside the existing `<createTable tableName="rental_equipments">` block, directly **after**
  the `<column name="updated_at" .../>` column (the last column currently defined).

Existing end of the `<createTable>` block:

```xml
            <column name="updated_at" type="TIMESTAMP WITH TIME ZONE"/>
        </createTable>
```

Replace with:

```xml
            <column name="updated_at" type="TIMESTAMP WITH TIME ZONE"/>
            <column name="equipment_type_slug" type="VARCHAR(100)">
                <constraints nullable="false"/>
            </column>
        </createTable>
```

No changes to `db.changelog-master.xml` are needed — `v1/rental-equipments.create-table.xml` is already included.

## 4. Validation Steps

```bash
./gradlew :service:compileJava
```
