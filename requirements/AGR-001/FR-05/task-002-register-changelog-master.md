<task_file_template>

# Task 002: Register agreement_signatures changelog in the master changelog

> **Applied Skill:** `liquibase` — new changelog files are registered at the **bottom** of `db.changelog-master.xml`,
> organized by version folder. Depends on Task 001.

## 1. Objective

Wire the new `v1/agreement_signatures.create-table.xml` into the master changelog so Liquibase applies it.

## 2. File to Modify / Create

* **File Path:** `service/src/main/resources/db/changelog/db.changelog-master.xml`
* **Action:** Modify Existing File

## 3. Code Implementation

**Location:** Immediately AFTER the existing agreement templates include line, inside the `<!-- agreement module -->`
block. Find this exact block near the bottom of the file:

```xml
    <!--    agreement module -->
    <include relativeToChangelogFile="true" file="v1/agreement_templates.create-table.xml"/>

</databaseChangeLog>
```

Replace it with EXACTLY:

```xml
    <!--    agreement module -->
    <include relativeToChangelogFile="true" file="v1/agreement_templates.create-table.xml"/>
    <include relativeToChangelogFile="true" file="v1/agreement_signatures.create-table.xml"/>

</databaseChangeLog>
```

## 4. Validation Steps

Execute the following command. Do NOT run the full application server.

```bash
./gradlew :service:compileJava "-Dspring.profiles.active=test"
```

</task_file_template>
