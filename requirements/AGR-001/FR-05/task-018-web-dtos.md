<task_file_template>

# Task 018: Create the signing web DTOs

> **Applied Skill:** `java-best-practices` / `error-responses` — records for request/response DTOs; Jakarta validation
> annotations on the request (validation negatives are covered by the WebMvc test, Task 026). `Instant` response fields
> need no `@DateTimeFormat` (that rule applies to request temporal fields only; these are responses). Zero inline
> comments.

## 1. Objective

Create the four web DTOs: the sign request and the two response records, plus the summary response.

## 2. File to Modify / Create

Create THREE new files, each with EXACTLY the content below.

## 3. Code Implementation

**File 1 — `service/src/main/java/com/github/jenkaby/bikerental/agreement/web/command/dto/SignAgreementRequest.java`** (Create New File):

```java
package com.github.jenkaby.bikerental.agreement.web.command.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record SignAgreementRequest(
        @NotBlank
        String signaturePng,

        @NotNull
        @PositiveOrZero
        Long rentalVersion,

        @NotNull
        @Positive
        Long templateId,

        @NotBlank
        String operatorId
) {
}
```

**File 2 — `service/src/main/java/com/github/jenkaby/bikerental/agreement/web/command/dto/SignatureCreatedResponse.java`** (Create New File):

```java
package com.github.jenkaby.bikerental.agreement.web.command.dto;

import java.time.Instant;

public record SignatureCreatedResponse(Long signatureId, Instant signedAt) {
}
```

**File 3 — `service/src/main/java/com/github/jenkaby/bikerental/agreement/web/query/dto/SignatureSummaryResponse.java`** (Create New File):

```java
package com.github.jenkaby.bikerental.agreement.web.query.dto;

import java.time.Instant;

public record SignatureSummaryResponse(
        Long signatureId,
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
