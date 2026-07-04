<task_file_template>

# Task 005: Create the AgreementSignatureSummary read model record

> **Applied Skill:** `java-best-practices` — record for an immutable read-model DTO. Mirrors
> `agreement/domain/model/AgreementTemplateSummary` (a domain read model built by a JPQL constructor expression).

## 1. Objective

Create the domain read model `AgreementSignatureSummary` exposing only the list/summary fields for a signature —
never the binary columns.

## 2. File to Modify / Create

* **File Path:** `service/src/main/java/com/github/jenkaby/bikerental/agreement/domain/model/AgreementSignatureSummary.java`
* **Action:** Create New File

## 3. Code Implementation

Create the file with EXACTLY this content:

```java
package com.github.jenkaby.bikerental.agreement.domain.model;

import java.time.Instant;

public record AgreementSignatureSummary(
        Long id,
        Long templateId,
        Integer templateVersionNumber,
        Instant signedAt) {
}
```

## 4. Validation Steps

Execute the following command. Do NOT run the full application server.

```bash
./gradlew :service:compileJava "-Dspring.profiles.active=test"
```

</task_file_template>
