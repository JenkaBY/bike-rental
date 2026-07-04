<task_file_template>

# Task 008: Create the AgreementTemplateRepository domain port

> **Applied Skill:** `spring-boot-data-ddd` — the domain declares a repository PORT (interface, no JPA
> types); the infrastructure adapter (Task 018) implements it. `saveNow` is a domain-language contract
> ("persist this state change immediately") hiding the JPA `saveAndFlush` detail. Mirrors
> `rental/domain/repository/RentalRepository.java`.

## 1. Objective

Declare the domain persistence contract the application services depend on. Depends on Task 007
(`AgreementTemplate`) and Task 006 (`AgreementTemplateSummary`).

## 2. File to Modify / Create

* **File Path:** `service/src/main/java/com/github/jenkaby/bikerental/agreement/domain/repository/AgreementTemplateRepository.java`
* **Action:** Create New File

## 3. Code Implementation

Create the file with EXACTLY this content:

```java
package com.github.jenkaby.bikerental.agreement.domain.repository;

import com.github.jenkaby.bikerental.agreement.domain.model.AgreementTemplate;
import com.github.jenkaby.bikerental.agreement.domain.model.AgreementTemplateSummary;

import java.util.List;
import java.util.Optional;

public interface AgreementTemplateRepository {

    AgreementTemplate save(AgreementTemplate template);

    AgreementTemplate saveNow(AgreementTemplate template);

    Optional<AgreementTemplate> findById(Long id);

    Optional<AgreementTemplate> findActive();

    List<AgreementTemplateSummary> findAllSummaries();

    int nextVersionNumber();

    void deleteById(Long id);
}
```

## 4. Validation Steps

Execute the following command. Do NOT run the full application server.

```bash
./gradlew :service:compileJava "-Dspring.profiles.active=test"
```

</task_file_template>
