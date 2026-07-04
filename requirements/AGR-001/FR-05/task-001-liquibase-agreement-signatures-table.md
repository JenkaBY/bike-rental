<task_file_template>

# Task 001: Liquibase changelog for agreement_signatures table

> **Applied Skill:** `liquibase` — one logical change per file; `author="claude"`; `<preConditions onFail="MARK_RAN">`
> guarding with `<not><tableExists.../></not>`; time columns `TIMESTAMP WITH TIME ZONE`; PK/UNIQUE/NOT NULL declared at
> creation; indexes and FK named per convention (`idx_{table}_{column}`, `fk_{source}_{target}`, `uq_{table}_{column}`);
> no rollback section. Mirrors `v1/agreement_templates.create-table.xml`.

## 1. Objective

Create the `agreement_signatures` table storing the immutable signing record: template reference (+ FK to
`agreement_templates`), a unique `rental_id`, the JSONB signing snapshot, the PDF/PNG bytes, the two sha256 hashes,
signing metadata (operator, ip, user agent), and `signed_at`. NO FK to rentals (cross-module).

## 2. File to Modify / Create

* **File Path:** `service/src/main/resources/db/changelog/v1/agreement_signatures.create-table.xml`
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

    <changeSet id="agreement_signatures.create-table" author="claude">
        <preConditions onFail="MARK_RAN">
            <not>
                <tableExists tableName="agreement_signatures"/>
            </not>
        </preConditions>

        <createTable tableName="agreement_signatures">
            <column name="id" type="BIGSERIAL">
                <constraints primaryKey="true" nullable="false"/>
            </column>
            <column name="template_id" type="BIGINT">
                <constraints nullable="false"
                             foreignKeyName="fk_agreement_signatures_agreement_templates"
                             references="agreement_templates(id)"/>
            </column>
            <column name="rental_id" type="BIGINT">
                <constraints nullable="false" unique="true"
                             uniqueConstraintName="uq_agreement_signatures_rental_id"/>
            </column>
            <column name="customer_id" type="UUID">
                <constraints nullable="false"/>
            </column>
            <column name="operator_id" type="VARCHAR(100)">
                <constraints nullable="false"/>
            </column>
            <column name="signing_snapshot" type="JSONB">
                <constraints nullable="false"/>
            </column>
            <column name="pdf_document" type="BYTEA">
                <constraints nullable="false"/>
            </column>
            <column name="pdf_sha256" type="CHAR(64)">
                <constraints nullable="false"/>
            </column>
            <column name="template_content_sha256" type="CHAR(64)">
                <constraints nullable="false"/>
            </column>
            <column name="signature_image" type="BYTEA">
                <constraints nullable="false"/>
            </column>
            <column name="signed_at" type="TIMESTAMP WITH TIME ZONE">
                <constraints nullable="false"/>
            </column>
            <column name="ip_address" type="VARCHAR(45)"/>
            <column name="user_agent" type="TEXT"/>
        </createTable>

        <createIndex tableName="agreement_signatures" indexName="idx_agreement_signatures_customer_id">
            <column name="customer_id"/>
        </createIndex>

        <createIndex tableName="agreement_signatures" indexName="idx_agreement_signatures_template_id">
            <column name="template_id"/>
        </createIndex>

    </changeSet>

</databaseChangeLog>
```

## 4. Validation Steps

Execute the following command. Do NOT run the full application server.

```bash
./gradlew :service:compileJava "-Dspring.profiles.active=test"
```

</task_file_template>
