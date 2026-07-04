<task_file_template>

# Task 024: Add agreement_signatures to the component-test truncate list

> **Applied Skill:** `spring-boot-java-cucumber` — between-scenario cleanup order respects FK constraints. Because
> `agreement_signatures.template_id` references `agreement_templates(id)`, the signatures table MUST be truncated BEFORE
> `agreement_templates`.

## 1. Objective

Insert `"agreement_signatures"` into `TABLE_TO_TRUNCATE` immediately before `"agreement_templates"` so scenario cleanup
does not fail on the FK.

## 2. File to Modify / Create

* **File Path:** `component-test/src/test/java/com/github/jenkaby/bikerental/componenttest/steps/common/hook/DbSteps.java`
* **Action:** Modify Existing File

## 3. Code Implementation

**Location:** In the `TABLE_TO_TRUNCATE` list. Find these two consecutive lines:

```java
            "rentals",
            "agreement_templates",
```

Replace them with EXACTLY:

```java
            "rentals",
            "agreement_signatures",
            "agreement_templates",
```

## 4. Validation Steps

Execute the following command. Do NOT run the full application server.

```bash
./gradlew :component-test:compileTestJava "-Dspring.profiles.active=test"
```

</task_file_template>
