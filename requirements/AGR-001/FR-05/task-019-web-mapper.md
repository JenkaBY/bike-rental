<task_file_template>

# Task 019: Create the SignatureWebMapper

> **Applied Skill:** `mapstruct-hexagonal` — a `@Mapper` interface mapping the domain read model
> `AgreementSignatureSummary` to `SignatureSummaryResponse` (mirrors `AgreementTemplateWebMapper`). The `id` field maps
> to `signatureId`. Depends on Task 005, Task 018.

## 1. Objective

Create the web mapper for the signature summary list DTO.

## 2. File to Modify / Create

* **File Path:** `service/src/main/java/com/github/jenkaby/bikerental/agreement/web/mapper/SignatureWebMapper.java`
* **Action:** Create New File

## 3. Code Implementation

Create the file with EXACTLY this content:

```java
package com.github.jenkaby.bikerental.agreement.web.mapper;

import com.github.jenkaby.bikerental.agreement.domain.model.AgreementSignatureSummary;
import com.github.jenkaby.bikerental.agreement.web.query.dto.SignatureSummaryResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper
public interface SignatureWebMapper {

    @Mapping(target = "signatureId", source = "id")
    SignatureSummaryResponse toResponse(AgreementSignatureSummary summary);

    List<SignatureSummaryResponse> toResponses(List<AgreementSignatureSummary> summaries);
}
```

## 4. Validation Steps

Execute the following command. Do NOT run the full application server.

```bash
./gradlew :service:compileJava "-Dspring.profiles.active=test"
```

</task_file_template>
