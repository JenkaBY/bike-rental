<task_file_template>

# Task 006: Create the AgreementTemplateSummary domain read model

> **Applied Skill:** `java-best-practices` — records for immutable data carriers; zero inline
> comments. `spring-boot-data-ddd` — lightweight read-model projection that deliberately EXCLUDES the
> heavy `content` TEXT column (NFR: list endpoint must not fetch `content`).

## 1. Objective

Define the summary read model returned by the catalog list endpoint — every field EXCEPT `content`.

## 2. File to Modify / Create

* **File Path:** `service/src/main/java/com/github/jenkaby/bikerental/agreement/domain/model/AgreementTemplateSummary.java`
* **Action:** Create New File

## 3. Code Implementation

Create the file with EXACTLY this content:

```java
package com.github.jenkaby.bikerental.agreement.domain.model;

import java.time.Instant;

public record AgreementTemplateSummary(
        Long id,
        Integer versionNumber,
        String title,
        AgreementTemplateStatus status,
        Instant createdAt,
        Instant activatedAt,
        Instant deactivatedAt
) {
}
```

> `versionNumber`, `activatedAt`, and `deactivatedAt` are boxed/nullable on purpose — a DRAFT has
> none of them.

## 4. Validation Steps

Execute the following command. Do NOT run the full application server.

```bash
./gradlew :service:compileJava "-Dspring.profiles.active=test"
```

</task_file_template>
