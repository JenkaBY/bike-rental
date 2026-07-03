<task_file_template>

# Task 001: Create Liquibase changelog adding version column to rentals

> **Applied Skill:** `liquibase` — one logical change per file; file/changeset naming
> `{table}.{action}-table_{specific-action}`; `author="claude"`; `<preConditions onFail="MARK_RAN">`
> guard; no rollback section.

## 1. Objective

Add a `version BIGINT NOT NULL DEFAULT 0` column to the `rentals` table so Hibernate can perform
JPA optimistic locking. Existing rows receive `0`.

## 2. File to Modify / Create

* **File Path:** `service/src/main/resources/db/changelog/v1/rentals.update-table_add-version-column.xml`
* **Action:** Create New File

## 3. Code Implementation

**Imports Required:**

```text
N/A (XML file)
```

**Code to Add/Replace:**

* **Location:** The entire content of the new file.
* **Snippet:**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
        xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
        http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-4.20.xsd">

    <changeSet id="rentals.update-table_add-version-column" author="claude">
        <preConditions onFail="MARK_RAN">
            <not>
                <columnExists tableName="rentals" columnName="version"/>
            </not>
        </preConditions>

        <addColumn tableName="rentals">
            <column name="version" type="BIGINT" defaultValueNumeric="0">
                <constraints nullable="false"/>
            </column>
        </addColumn>

    </changeSet>

</databaseChangeLog>
```

## 4. Validation Steps

Execute the following commands to ensure this task was successful. Do NOT run the full application server.

```bash
# No compilation or test run is required for a changelog file. Verify only that the XML is well-formed
# and that the file exists at the exact path above. The include is registered in task-002.
```

</task_file_template>
