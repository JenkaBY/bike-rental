<task_file_template>

# Task 002: Create the `agreement_templates` Liquibase changelog

> **Applied Skill:** `liquibase` — time columns are ALWAYS `TIMESTAMP WITH TIME ZONE`; one logical
> change per file; changeset id == file name; `author="claude"`; guard with
> `<preConditions onFail="MARK_RAN">`; NO rollback; register new files at the BOTTOM of the master
> changelog. Mirrors `v1/rentals.create-table.xml`.

## 1. Objective

Create the `agreement_templates` table with the single-active-version and unique-version-number
invariants owned by Postgres partial unique indexes plus a status CHECK constraint.

## 2. File to Modify / Create

* **File Path:** `service/src/main/resources/db/changelog/v1/agreement_templates.create-table.xml`
* **Action:** Create New File

## 3. Code Implementation

Create the file with EXACTLY this content:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
        xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
        http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-4.20.xsd">

    <changeSet id="agreement_templates.create-table" author="claude">
        <preConditions onFail="MARK_RAN">
            <not>
                <tableExists tableName="agreement_templates"/>
            </not>
        </preConditions>

        <createTable tableName="agreement_templates">
            <column name="id" type="BIGSERIAL">
                <constraints primaryKey="true" nullable="false"/>
            </column>
            <column name="version_number" type="INTEGER"/>
            <column name="title" type="VARCHAR(255)">
                <constraints nullable="false"/>
            </column>
            <column name="content" type="TEXT">
                <constraints nullable="false"/>
            </column>
            <column name="content_sha256" type="CHAR(64)"/>
            <column name="status" type="VARCHAR(16)">
                <constraints nullable="false"/>
            </column>
            <column name="lock_version" type="BIGINT" defaultValueNumeric="0">
                <constraints nullable="false"/>
            </column>
            <column name="created_at" type="TIMESTAMP WITH TIME ZONE" defaultValueComputed="now()">
                <constraints nullable="false"/>
            </column>
            <column name="updated_at" type="TIMESTAMP WITH TIME ZONE" defaultValueComputed="now()">
                <constraints nullable="false"/>
            </column>
            <column name="activated_at" type="TIMESTAMP WITH TIME ZONE"/>
            <column name="deactivated_at" type="TIMESTAMP WITH TIME ZONE"/>
        </createTable>

        <sql dbms="postgresql">
            ALTER TABLE agreement_templates
                ADD CONSTRAINT ck_agreement_templates_status
                    CHECK (status IN ('DRAFT', 'ACTIVE', 'DEACTIVATED'));

            CREATE UNIQUE INDEX uq_agreement_templates_single_active
                ON agreement_templates ((true))
                WHERE status = 'ACTIVE';

            CREATE UNIQUE INDEX uq_agreement_templates_version_number
                ON agreement_templates (version_number)
                WHERE version_number IS NOT NULL;
        </sql>

    </changeSet>

</databaseChangeLog>
```

> The `uq_agreement_templates_single_active` index is on the constant expression `((true))` so at
> most one row can have `status = 'ACTIVE'`. Do NOT add a `<rollback>` section (project rule).

## 4. Validation Steps

Execute the following command to ensure the changelog is well-formed XML and the project still
compiles. Do NOT run the full application server or apply migrations manually.

```bash
./gradlew :service:compileJava "-Dspring.profiles.active=test"
```

</task_file_template>
