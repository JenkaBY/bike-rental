<task_file_template>

# Task 002: Register the version-column changelog in db.changelog-master.xml

> **Applied Skill:** `liquibase` — new changelog files are registered at the **bottom** of
> `db.changelog-master.xml`, organized by version folder (v1).

## 1. Objective

Wire the changelog created in task-001 into the master changelog so Liquibase applies it.

## 2. File to Modify / Create

* **File Path:** `service/src/main/resources/db/changelog/db.changelog-master.xml`
* **Action:** Modify Existing File

## 3. Code Implementation

**Imports Required:**

```text
N/A (XML file)
```

**Code to Add/Replace:**

* **Location:** Add a new `<include>` line as the **last** include element inside `<databaseChangeLog>`,
  immediately AFTER this existing line:

  ```xml
      <include relativeToChangelogFile="true" file="v1/oauth2_authorization_consent.create-table.xml"/>
  ```

  and BEFORE the closing `</databaseChangeLog>` tag.
* **Snippet:**

```xml
    <include relativeToChangelogFile="true" file="v1/rentals.update-table_add-version-column.xml"/>
```

The bottom of the file must then read:

```xml
    <include relativeToChangelogFile="true" file="v1/oauth2_authorization_consent.create-table.xml"/>

    <include relativeToChangelogFile="true" file="v1/rentals.update-table_add-version-column.xml"/>

</databaseChangeLog>
```

## 4. Validation Steps

Execute the following commands to ensure this task was successful. Do NOT run the full application server.

```bash
# No compilation or test run is required for a changelog registration. Verify only that the new
# <include> line is the last include in db.changelog-master.xml and the XML is well-formed.
```

</task_file_template>
