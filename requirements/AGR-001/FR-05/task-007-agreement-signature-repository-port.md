<task_file_template>

# Task 007: Create the AgreementSignatureRepository port

> **Applied Skill:** `spring-boot-data-ddd` — the domain port is an interface in `domain/repository/` returning domain
> types and `Optional` for possibly-absent values (mirrors `AgreementTemplateRepository`). Depends on Task 004, Task 005.

## 1. Objective

Create the outbound repository port the application service uses to persist and read signatures without knowing about JPA.

## 2. File to Modify / Create

* **File Path:** `service/src/main/java/com/github/jenkaby/bikerental/agreement/domain/repository/AgreementSignatureRepository.java`
* **Action:** Create New File

## 3. Code Implementation

Create the file with EXACTLY this content:

```java
package com.github.jenkaby.bikerental.agreement.domain.repository;

import com.github.jenkaby.bikerental.agreement.domain.model.AgreementSignature;
import com.github.jenkaby.bikerental.agreement.domain.model.AgreementSignatureSummary;

import java.util.Optional;

public interface AgreementSignatureRepository {

    AgreementSignature save(AgreementSignature signature);

    boolean existsByRentalId(Long rentalId);

    Optional<AgreementSignatureSummary> findSummaryByRentalId(Long rentalId);

    Optional<byte[]> findPdfByRentalId(Long rentalId);
}
```

## 4. Validation Steps

Execute the following command. Do NOT run the full application server.

```bash
./gradlew :service:compileJava "-Dspring.profiles.active=test"
```

</task_file_template>
