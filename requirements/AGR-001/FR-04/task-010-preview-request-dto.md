<task_file_template>

# Task 010: Create the `AgreementPdfPreviewRequest` web DTO

> **Applied Skill:** `.claude/rules/java-style.md` (records for DTOs) · `.claude/rules/error-responses.md` (Jakarta
> constraints flow through `BaseValidationErrorMapper` automatically — no handler changes). Mirrors the existing
> `AgreementTemplateRequest` record.

## 1. Objective

Declare the request body for the preview endpoint: a validated `title` (`@NotBlank`, `@Size(max = 255)`) and
`content` (`@NotBlank`).

## 2. File to Modify / Create

* **File Path:** `service/src/main/java/com/github/jenkaby/bikerental/agreement/web/command/dto/AgreementPdfPreviewRequest.java`
* **Action:** Create New File

## 3. Code Implementation

Create the file with EXACTLY this content:

```java
package com.github.jenkaby.bikerental.agreement.web.command.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AgreementPdfPreviewRequest(
        @NotBlank
        @Size(max = 255)
        String title,

        @NotBlank
        String content
) {
}
```

## 4. Validation Steps

Execute the following command. Do NOT run the full application server.

```bash
./gradlew :service:compileJava "-Dspring.profiles.active=test"
```

</task_file_template>
