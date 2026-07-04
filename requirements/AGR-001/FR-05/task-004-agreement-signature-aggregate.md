<task_file_template>

# Task 004: Create the AgreementSignature aggregate

> **Applied Skill:** `java-best-practices` / `spring-boot-data-ddd` — immutable record-of-fact aggregate; `@Getter`
> `@Builder` with a private all-args constructor (mirrors `AgreementTemplate`); a single static `create(...)` factory;
> NO mutators. Zero inline comments. Depends on Task 003.

## 1. Objective

Create the `AgreementSignature` aggregate whose fields mirror the `agreement_signatures` table. It is created once via
`AgreementSignature.create(...)` and never mutated.

## 2. File to Modify / Create

* **File Path:** `service/src/main/java/com/github/jenkaby/bikerental/agreement/domain/model/AgreementSignature.java`
* **Action:** Create New File

## 3. Code Implementation

Create the file with EXACTLY this content:

```java
package com.github.jenkaby.bikerental.agreement.domain.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AgreementSignature {

    private Long id;

    private Long templateId;

    private Long rentalId;

    private java.util.UUID customerId;

    private String operatorId;

    private SigningSnapshot signingSnapshot;

    private byte[] pdfDocument;

    private String pdfSha256;

    private String templateContentSha256;

    private byte[] signatureImage;

    private Instant signedAt;

    private String ipAddress;

    private String userAgent;

    public static AgreementSignature create(Long templateId,
                                            Long rentalId,
                                            java.util.UUID customerId,
                                            String operatorId,
                                            SigningSnapshot signingSnapshot,
                                            byte[] pdfDocument,
                                            String pdfSha256,
                                            String templateContentSha256,
                                            byte[] signatureImage,
                                            Instant signedAt,
                                            String ipAddress,
                                            String userAgent) {
        return AgreementSignature.builder()
                .templateId(templateId)
                .rentalId(rentalId)
                .customerId(customerId)
                .operatorId(operatorId)
                .signingSnapshot(signingSnapshot)
                .pdfDocument(pdfDocument)
                .pdfSha256(pdfSha256)
                .templateContentSha256(templateContentSha256)
                .signatureImage(signatureImage)
                .signedAt(signedAt)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .build();
    }
}
```

## 4. Validation Steps

Execute the following command. Do NOT run the full application server.

```bash
./gradlew :service:compileJava "-Dspring.profiles.active=test"
```

</task_file_template>
