<task_file_template>

# Task 010: Create the signing use-case interfaces

> **Applied Skill:** `spring-boot-best-practices` / `java-best-practices` — application-layer contracts are interfaces
> under `application/usecase/`; commands and results are nested records (mirrors
> `ActivateAgreementTemplateUseCase` and `CreateAgreementTemplateUseCase`). Zero inline comments. Depends on Task 005.

## 1. Objective

Create the three application use-case interfaces for signing (command), listing (query), and PDF download (query), plus
the `SignAgreementCommand` / `SignAgreementResult` records nested in the command use case.

## 2. File to Modify / Create

Create THREE new files, each with EXACTLY the content below.

## 3. Code Implementation

**File 1 — `service/src/main/java/com/github/jenkaby/bikerental/agreement/application/usecase/SignAgreementUseCase.java`** (Create New File):

```java
package com.github.jenkaby.bikerental.agreement.application.usecase;

import java.time.Instant;

public interface SignAgreementUseCase {

    SignAgreementResult execute(SignAgreementCommand command);

    record SignAgreementCommand(
            Long rentalId,
            String signaturePngBase64,
            Long rentalVersion,
            Long templateId,
            String operatorId,
            String ipAddress,
            String userAgent) {
    }

    record SignAgreementResult(Long signatureId, Instant signedAt) {
    }
}
```

**File 2 — `service/src/main/java/com/github/jenkaby/bikerental/agreement/application/usecase/FindRentalSignaturesUseCase.java`** (Create New File):

```java
package com.github.jenkaby.bikerental.agreement.application.usecase;

import com.github.jenkaby.bikerental.agreement.domain.model.AgreementSignatureSummary;

import java.util.List;

public interface FindRentalSignaturesUseCase {

    List<AgreementSignatureSummary> execute(Long rentalId);
}
```

**File 3 — `service/src/main/java/com/github/jenkaby/bikerental/agreement/application/usecase/GetSignaturePdfUseCase.java`** (Create New File):

```java
package com.github.jenkaby.bikerental.agreement.application.usecase;

public interface GetSignaturePdfUseCase {

    byte[] execute(Long rentalId);
}
```

## 4. Validation Steps

Execute the following command. Do NOT run the full application server.

```bash
./gradlew :service:compileJava "-Dspring.profiles.active=test"
```

</task_file_template>
