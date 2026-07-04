<task_file_template>

# Task 013: Create the signature query service

> **Applied Skill:** `spring-boot-best-practices` — read-only application service; one service implementing both query
> use cases; absent PDF surfaces as the shared `ResourceNotFoundException` (→ global 404). Zero inline comments. Depends
> on Tasks 004, 005, 007, 010.

## 1. Objective

Implement `FindRentalSignaturesUseCase` (0..1 summaries) and `GetSignaturePdfUseCase` (PDF bytes or 404) in a single
read-only service.

## 2. File to Modify / Create

* **File Path:** `service/src/main/java/com/github/jenkaby/bikerental/agreement/application/service/SignatureQueryService.java`
* **Action:** Create New File

## 3. Code Implementation

Create the file with EXACTLY this content:

```java
package com.github.jenkaby.bikerental.agreement.application.service;

import com.github.jenkaby.bikerental.agreement.application.usecase.FindRentalSignaturesUseCase;
import com.github.jenkaby.bikerental.agreement.application.usecase.GetSignaturePdfUseCase;
import com.github.jenkaby.bikerental.agreement.domain.model.AgreementSignature;
import com.github.jenkaby.bikerental.agreement.domain.model.AgreementSignatureSummary;
import com.github.jenkaby.bikerental.agreement.domain.repository.AgreementSignatureRepository;
import com.github.jenkaby.bikerental.shared.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
class SignatureQueryService implements FindRentalSignaturesUseCase, GetSignaturePdfUseCase {

    private final AgreementSignatureRepository signatureRepository;

    SignatureQueryService(AgreementSignatureRepository signatureRepository) {
        this.signatureRepository = signatureRepository;
    }

    @Override
    public List<AgreementSignatureSummary> execute(Long rentalId) {
        log.info("Listing signatures for rental {}", rentalId);
        return signatureRepository.findSummaryByRentalId(rentalId)
                .map(List::of)
                .orElse(List.of());
    }

    @Override
    public byte[] execute(Long rentalId, GetSignaturePdfDiscriminator discriminator) {
        return getPdf(rentalId);
    }

    private byte[] getPdf(Long rentalId) {
        log.info("Fetching signature PDF for rental {}", rentalId);
        return signatureRepository.findPdfByRentalId(rentalId)
                .orElseThrow(() -> new ResourceNotFoundException(AgreementSignature.class, rentalId.toString()));
    }

    enum GetSignaturePdfDiscriminator {
        INSTANCE
    }
}
```

> IMPORTANT — the two use-case interfaces each declare `execute(Long)` with the SAME erasure, so one class cannot
> override both with identical signatures. Resolve this by making the class NOT implement `GetSignaturePdfUseCase` here
> and instead splitting into two files. Discard the block above and INSTEAD create the two files exactly as follows.

**File A — `service/src/main/java/com/github/jenkaby/bikerental/agreement/application/service/FindRentalSignaturesService.java`** (Create New File):

```java
package com.github.jenkaby.bikerental.agreement.application.service;

import com.github.jenkaby.bikerental.agreement.application.usecase.FindRentalSignaturesUseCase;
import com.github.jenkaby.bikerental.agreement.domain.model.AgreementSignatureSummary;
import com.github.jenkaby.bikerental.agreement.domain.repository.AgreementSignatureRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
class FindRentalSignaturesService implements FindRentalSignaturesUseCase {

    private final AgreementSignatureRepository signatureRepository;

    FindRentalSignaturesService(AgreementSignatureRepository signatureRepository) {
        this.signatureRepository = signatureRepository;
    }

    @Override
    public List<AgreementSignatureSummary> execute(Long rentalId) {
        log.info("Listing signatures for rental {}", rentalId);
        return signatureRepository.findSummaryByRentalId(rentalId)
                .map(List::of)
                .orElse(List.of());
    }
}
```

**File B — `service/src/main/java/com/github/jenkaby/bikerental/agreement/application/service/GetSignaturePdfService.java`** (Create New File):

```java
package com.github.jenkaby.bikerental.agreement.application.service;

import com.github.jenkaby.bikerental.agreement.application.usecase.GetSignaturePdfUseCase;
import com.github.jenkaby.bikerental.agreement.domain.model.AgreementSignature;
import com.github.jenkaby.bikerental.agreement.domain.repository.AgreementSignatureRepository;
import com.github.jenkaby.bikerental.shared.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(readOnly = true)
class GetSignaturePdfService implements GetSignaturePdfUseCase {

    private final AgreementSignatureRepository signatureRepository;

    GetSignaturePdfService(AgreementSignatureRepository signatureRepository) {
        this.signatureRepository = signatureRepository;
    }

    @Override
    public byte[] execute(Long rentalId) {
        log.info("Fetching signature PDF for rental {}", rentalId);
        return signatureRepository.findPdfByRentalId(rentalId)
                .orElseThrow(() -> new ResourceNotFoundException(AgreementSignature.class, rentalId.toString()));
    }
}
```

> DO NOT create `SignatureQueryService.java`. Create ONLY the two files A and B above. The first code block in section 3
> exists solely to explain WHY a single class is impossible (same-erasure `execute(Long)` clash) — do not write it.

## 4. Validation Steps

Execute the following command. Do NOT run the full application server.

```bash
./gradlew :service:compileJava "-Dspring.profiles.active=test"
```

</task_file_template>
