# Task 001: Add Unique Partial Index to `rental-equipments.create-table.xml`

> **Applied Skill:** `d:\Projects\private\bikerent\.github\skills\liquibase\SKILL.md` — Raw `<sql>` block pattern for
> partial indexes not natively supported by Liquibase XML DSL; index added inside the existing changeset per FR-01
> direct-modification approach.

## 1. Objective

Add a `UNIQUE` partial index `idx_rental_equipments_one_active` on `rental_equipments(equipment_id)` filtered to
`WHERE status IN ('ACTIVE', 'ASSIGNED')` by inserting a raw `<sql>` block into the **existing** changeset in
`rental-equipments.create-table.xml`. This enforces at the database level that a given piece of equipment can be in at
most one `ACTIVE` or `ASSIGNED` rental simultaneously.

## 2. File to Modify / Create

* **File Path:** `service/src/main/resources/db/changelog/v1/rental-equipments.create-table.xml`
* **Action:** Modify Existing File

## 3. Code Implementation

**Imports Required:** N/A — XML file, no imports.

**Code to Add/Replace:**

* **Location:** Inside the `<changeSet id="rental-equipments.create-table">` element, immediately **after** the closing
  `</addForeignKeyConstraint>` tag and **before** the closing `</changeSet>` tag.
* **Snippet:**

```xml
        <sql>
            CREATE UNIQUE INDEX idx_rental_equipments_one_active
                ON rental_equipments (equipment_id)
                WHERE status IN ('ACTIVE', 'ASSIGNED');
        </sql>
```

**Resulting file section (full changeset after modification):**

```xml
    <changeSet id="rental-equipments.create-table" author="bikerental">
        <preConditions onFail="MARK_RAN">
            <not>
                <tableExists tableName="rental_equipments"/>
            </not>
        </preConditions>

        <createTable tableName="rental_equipments">
            <column name="id" type="BIGSERIAL">
                <constraints primaryKey="true" nullable="false"/>
            </column>
            <column name="rental_id" type="BIGINT">
                <constraints nullable="false"/>
            </column>
            <column name="equipment_id" type="BIGINT"/>
            <column name="equipment_uid" type="VARCHAR(100)"/>
            <column name="equipment_type_slug" type="VARCHAR(100)">
                <constraints nullable="false"/>
            </column>
            <column name="tariff_id" type="BIGINT"/>
            <column name="status" type="VARCHAR(20)">
                <constraints nullable="false"/>
            </column>
            <column name="started_at" type="TIMESTAMP WITH TIME ZONE"/>
            <column name="expected_return_at" type="TIMESTAMP WITH TIME ZONE"/>
            <column name="actual_return_at" type="TIMESTAMP WITH TIME ZONE"/>
            <column name="estimated_cost" type="DECIMAL(10,2)"/>
            <column name="final_cost" type="DECIMAL(10,2)"/>
            <column name="created_at" type="TIMESTAMP WITH TIME ZONE">
                <constraints nullable="false"/>
            </column>
            <column name="updated_at" type="TIMESTAMP WITH TIME ZONE"/>
        </createTable>

        <createIndex tableName="rental_equipments" indexName="idx_rent_equip_rental_id">
            <column name="rental_id"/>
        </createIndex>

        <createIndex tableName="rental_equipments" indexName="idx_rent_equip_equipment_uid">
            <column name="equipment_uid"/>
        </createIndex>

        <addForeignKeyConstraint baseTableName="rental_equipments" baseColumnNames="rental_id"
                                 referencedTableName="rentals" referencedColumnNames="id"
                                 constraintName="fk_rent_equip_rental_id"/>

        <sql>
            CREATE UNIQUE INDEX idx_rental_equipments_one_active
                ON rental_equipments (equipment_id)
                WHERE status IN ('ACTIVE', 'ASSIGNED');
        </sql>

    </changeSet>
```

> **Key rules enforced:**
> - Index name is exactly `idx_rental_equipments_one_active`.
> - The `WHERE` clause covers only `'ACTIVE'` and `'ASSIGNED'` — `'RETURNED'` rows are not constrained.
> - `UNIQUE` is specified so PostgreSQL rejects any `INSERT`/`UPDATE` that would create a second in-flight row for the
    > same `equipment_id`.
> - No new changeset file is created; no rollback section is added (per FR-01 and Liquibase skill best practices).
> - `<sql>` is used because Liquibase XML DSL has no native partial-index element.

## 4. Validation Steps

Execute the following commands to ensure this task was successful. Do NOT run the full application server.

```bash
# 1. Compile the project to confirm no XML parse errors surface during classpath scanning
./gradlew :service:compileJava

# 2. Run unit + WebMVC tests (Liquibase is disabled in the test profile, so this validates compilation only)
./gradlew :service:test "-Dspring.profiles.active=test"
```
