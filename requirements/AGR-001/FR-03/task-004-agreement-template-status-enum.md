<task_file_template>

# Task 004: Create the AgreementTemplateStatus enum

> **Applied Skill:** `java-best-practices` — zero inline comments; enums for closed value sets.
> `spring-boot-modulith` — domain model lives in `domain/model`, no framework imports.

## 1. Objective

Define the closed set of template lifecycle states. Transitions are linear
(`DRAFT → ACTIVE → DEACTIVATED`) and guarded inside the aggregate, so no transition map is needed.

## 2. File to Modify / Create

* **File Path:** `service/src/main/java/com/github/jenkaby/bikerental/agreement/domain/model/AgreementTemplateStatus.java`
* **Action:** Create New File

## 3. Code Implementation

Create the file with EXACTLY this content:

```java
package com.github.jenkaby.bikerental.agreement.domain.model;

public enum AgreementTemplateStatus {
    DRAFT,
    ACTIVE,
    DEACTIVATED
}
```

## 4. Validation Steps

Execute the following command. Do NOT run the full application server.

```bash
./gradlew :service:compileJava "-Dspring.profiles.active=test"
```

</task_file_template>
