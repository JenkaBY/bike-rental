<task_file_template>

# Task 015: Create the AgreementTemplateWebMapper

> **Applied Skill:** `mapstruct-hexagonal` — MapStruct interface in the `web` layer maps domain ↔ DTO;
> `uses = {InstantMapper.class}` for `Instant` fields (the project's MapStruct component model is
> `spring` with constructor injection, and `unmappedTargetPolicy=ERROR`, so EVERY target field must be
> mapped). Mirrors `customer/web/mapper/CustomerWebMapper.java` + `rental/.../RentalJpaMapper.java`
> (`uses = {... InstantMapper.class}`).

## 1. Objective

Map the `AgreementTemplate` aggregate to `AgreementTemplateResponse`, and the
`AgreementTemplateSummary` read model to `AgreementTemplateSummaryResponse`. Depends on Task 007,
Task 006, Task 014.

## 2. File to Modify / Create

* **File Path:** `service/src/main/java/com/github/jenkaby/bikerental/agreement/web/mapper/AgreementTemplateWebMapper.java`
* **Action:** Create New File

## 3. Code Implementation

Create the file with EXACTLY this content:

```java
package com.github.jenkaby.bikerental.agreement.web.mapper;

import com.github.jenkaby.bikerental.agreement.domain.model.AgreementTemplate;
import com.github.jenkaby.bikerental.agreement.domain.model.AgreementTemplateSummary;
import com.github.jenkaby.bikerental.agreement.web.query.dto.AgreementTemplateResponse;
import com.github.jenkaby.bikerental.agreement.web.query.dto.AgreementTemplateSummaryResponse;
import com.github.jenkaby.bikerental.shared.mapper.InstantMapper;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(uses = {InstantMapper.class})
public interface AgreementTemplateWebMapper {

    AgreementTemplateResponse toResponse(AgreementTemplate template);

    AgreementTemplateSummaryResponse toSummaryResponse(AgreementTemplateSummary summary);

    List<AgreementTemplateSummaryResponse> toSummaryResponses(List<AgreementTemplateSummary> summaries);
}
```

> Both response records carry `Instant` audit fields (`createdAt`, `activatedAt`, `deactivatedAt`)
> whose source fields are also `Instant`, so MapStruct maps them directly; `InstantMapper` is listed
> for consistency with the project convention. All response record components have identically named
> source properties, satisfying `unmappedTargetPolicy=ERROR`.

## 4. Validation Steps

Execute the following command — MapStruct generates the implementation at compile time, so a
successful compile proves every target field is mapped.

```bash
./gradlew :service:compileJava "-Dspring.profiles.active=test"
```

</task_file_template>
