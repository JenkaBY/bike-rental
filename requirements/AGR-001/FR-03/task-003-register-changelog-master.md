<task_file_template>

# Task 003: Register the agreement changelog in the master changelog

> **Applied Skill:** `liquibase` — register new files at the **bottom** of `db.changelog-master.xml`.

## 1. Objective

Wire the new `agreement_templates.create-table.xml` (from Task 002) into the Liquibase master
changelog so it runs during migration.

## 2. File to Modify / Create

* **File Path:** `service/src/main/resources/db/changelog/db.changelog-master.xml`
* **Action:** Modify Existing File

## 3. Code Implementation

**Location:** Immediately AFTER the existing last `<include ...>` line
(`<include relativeToChangelogFile="true" file="v1/rentals.update-table_add-version-column.xml"/>`)
and BEFORE the closing `</databaseChangeLog>` tag.

**Snippet to add:**

```xml
    <!--    agreement module -->
    <include relativeToChangelogFile="true" file="v1/agreement_templates.create-table.xml"/>
```

The bottom of the file must read exactly:

```xml
    <include relativeToChangelogFile="true" file="v1/rentals.update-table_add-version-column.xml"/>

    <!--    agreement module -->
    <include relativeToChangelogFile="true" file="v1/agreement_templates.create-table.xml"/>

</databaseChangeLog>
```

## 4. Validation Steps

Execute the following command to ensure the XML is well-formed and the project still compiles. Do
NOT run the full application server.

```bash
./gradlew :service:compileJava "-Dspring.profiles.active=test"
```

</task_file_template>
