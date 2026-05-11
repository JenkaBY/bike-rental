# Task 001: Add `condition_slug` Column to `equipments` Changeset

> **Applied Skill:** `liquibase/SKILL.md` — Direct modification of existing changeset; `VARCHAR(50) NOT NULL DEFAULT`
> pattern for additive columns.

## 1. Objective

Add the `condition_slug VARCHAR(50) NOT NULL DEFAULT 'GOOD'` column directly inside the existing
`equipments.create-table.xml` Liquibase changeset so that every new `equipments` row tracks the
physical condition of the equipment unit.

## 2. File to Modify / Create

* **File Path:** `service/src/main/resources/db/changelog/v1/equipments.create-table.xml`
* **Action:** Modify Existing File

## 3. Code Implementation

**Imports Required:** N/A (XML file)

**Code to Add/Replace:**

* **Location:** Inside the `<createTable tableName="equipments">` block, immediately after the
  closing `</column>` tag of the `status_slug` column definition (line ~30), and before the
  `<column name="model" .../>` definition.

* **Snippet — replace this block:**

```xml
            <column name="status_slug" type="VARCHAR(50)">
                <constraints nullable="false"/>
            </column>
            <column name="model" type="VARCHAR(200)"/>
```

**With this block:**

```xml
            <column name="status_slug" type="VARCHAR(50)">
                <constraints nullable="false"/>
            </column>
            <column name="condition_slug" type="VARCHAR(50)" defaultValue="GOOD">
                <constraints nullable="false"/>
            </column>
            <column name="model" type="VARCHAR(200)"/>
```

## 4. Validation Steps

```bash
./gradlew :service:compileJava
```

No runtime test possible without a live DB; compile confirms the XML is valid. The column will be
verified structurally in FR-02 when the JPA entity mapping is added and the schema is exercised by
component tests.
