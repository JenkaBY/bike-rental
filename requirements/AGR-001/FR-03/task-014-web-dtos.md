<task_file_template>

# Task 014: Create the agreement web DTO records

> **Applied Skill:** `java-best-practices` — records for DTOs; `error-responses` rule — bean-validation
> `@NotBlank`/`@Size` codes flow through `BaseValidationErrorMapper` automatically (no handler needed).
> Mirrors `customer/web/command/dto/CustomerRequest.java` + `.../query/dto/CustomerResponse.java`.

## 1. Objective

Define the request DTO (with validation) plus the full and summary response DTOs. Depends on nothing
else in this FR.

## 2. Files to Modify / Create

Create THREE new files.

## 3. Code Implementation

### File 1: `AgreementTemplateRequest.java`

* **File Path:** `service/src/main/java/com/github/jenkaby/bikerental/agreement/web/command/dto/AgreementTemplateRequest.java`

```java
package com.github.jenkaby.bikerental.agreement.web.command.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AgreementTemplateRequest(
        @NotBlank
        @Size(max = 255)
        String title,

        @NotBlank
        String content
) {
}
```

### File 2: `AgreementTemplateResponse.java`

* **File Path:** `service/src/main/java/com/github/jenkaby/bikerental/agreement/web/query/dto/AgreementTemplateResponse.java`

```java
package com.github.jenkaby.bikerental.agreement.web.query.dto;

import com.github.jenkaby.bikerental.agreement.domain.model.AgreementTemplateStatus;

import java.time.Instant;

public record AgreementTemplateResponse(
        Long id,
        Integer versionNumber,
        String title,
        String content,
        AgreementTemplateStatus status,
        Instant createdAt,
        Instant activatedAt,
        Instant deactivatedAt
) {
}
```

### File 3: `AgreementTemplateSummaryResponse.java`

* **File Path:** `service/src/main/java/com/github/jenkaby/bikerental/agreement/web/query/dto/AgreementTemplateSummaryResponse.java`

```java
package com.github.jenkaby.bikerental.agreement.web.query.dto;

import com.github.jenkaby.bikerental.agreement.domain.model.AgreementTemplateStatus;

import java.time.Instant;

public record AgreementTemplateSummaryResponse(
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

> `AgreementTemplateSummaryResponse` deliberately has NO `content` field — the list endpoint never
> exposes the heavy TEXT column.

## 4. Validation Steps

Execute the following command. Do NOT run the full application server.

```bash
./gradlew :service:compileJava "-Dspring.profiles.active=test"
```

</task_file_template>
