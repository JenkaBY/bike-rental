<task_file_template>

# Task 024: Create the agreement component-test seeding infrastructure

> **Applied Skill:** `spring-boot-java-cucumber` — DataTable → entity conversion lives in a
> `{Domain}Transformer` with a `@DataTableType` method (NEVER convert inside a step class); DB seeding
> goes through an `InsertableRepositoryImpl` subclass driven by `JpaEntityInserter`; step classes are
> Spring-managed and auto-discovered by the Cucumber glue scan of `com.github.jenkaby.bikerental`.
> Mirrors `InsertableRentalRepository`, `RentalJpaEntityTransformer`, and `RentalDbSteps`.

## 1. Objective

Provide the three test-side helpers scenarios need: an Insertable repository for
`AgreementTemplateJpaEntity`, a DataTable transformer that builds seeded entities (with
`lockVersion = 0`), and a step class that seeds templates and extracts the created template id into
`requestedObjectId`. Depends on Task 019 (entity) and Task 004 (status enum).

## 2. Files to Modify / Create

Create THREE new files.

## 3. Code Implementation

### File 1: `InsertableAgreementTemplateRepository.java`

* **File Path:** `component-test/src/test/java/com/github/jenkaby/bikerental/componenttest/config/db/repository/InsertableAgreementTemplateRepository.java`

```java
package com.github.jenkaby.bikerental.componenttest.config.db.repository;

import com.github.jenkaby.bikerental.agreement.infrastructure.persistence.entity.AgreementTemplateJpaEntity;
import org.springframework.stereotype.Repository;

@Repository
public class InsertableAgreementTemplateRepository extends InsertableRepositoryImpl<AgreementTemplateJpaEntity, Long> {

    public InsertableAgreementTemplateRepository(JpaEntityInserter entityInserter) {
        super(entityInserter);
    }
}
```

### File 2: `AgreementTemplateJpaEntityTransformer.java`

* **File Path:** `component-test/src/test/java/com/github/jenkaby/bikerental/componenttest/transformer/AgreementTemplateJpaEntityTransformer.java`

```java
package com.github.jenkaby.bikerental.componenttest.transformer;

import com.github.jenkaby.bikerental.agreement.domain.model.AgreementTemplateStatus;
import com.github.jenkaby.bikerental.agreement.infrastructure.persistence.entity.AgreementTemplateJpaEntity;
import io.cucumber.java.DataTableType;

import java.util.Map;

public class AgreementTemplateJpaEntityTransformer {

    @DataTableType
    public AgreementTemplateJpaEntity transform(Map<String, String> entry) {
        var statusString = DataTableHelper.getStringOrNull(entry, "status");
        var status = statusString != null ? AgreementTemplateStatus.valueOf(statusString) : null;
        return AgreementTemplateJpaEntity.builder()
                .id(DataTableHelper.toLong(entry, "id"))
                .lockVersion(0L)
                .versionNumber(DataTableHelper.toInt(entry, "versionNumber"))
                .title(DataTableHelper.getStringOrNull(entry, "title"))
                .content(DataTableHelper.getStringOrNull(entry, "content"))
                .contentSha256(DataTableHelper.getStringOrNull(entry, "contentSha256"))
                .status(status)
                .createdAt(DataTableHelper.parseLocalDateTimeToInstant(entry, "createdAt"))
                .updatedAt(DataTableHelper.parseLocalDateTimeToInstant(entry, "updatedAt"))
                .activatedAt(DataTableHelper.parseLocalDateTimeToInstant(entry, "activatedAt"))
                .deactivatedAt(DataTableHelper.parseLocalDateTimeToInstant(entry, "deactivatedAt"))
                .build();
    }
}
```

### File 3: `AgreementTemplateSteps.java`

* **File Path:** `component-test/src/test/java/com/github/jenkaby/bikerental/componenttest/steps/agreement/AgreementTemplateSteps.java`

```java
package com.github.jenkaby.bikerental.componenttest.steps.agreement;

import com.github.jenkaby.bikerental.agreement.infrastructure.persistence.entity.AgreementTemplateJpaEntity;
import com.github.jenkaby.bikerental.componenttest.config.db.repository.InsertableAgreementTemplateRepository;
import com.github.jenkaby.bikerental.componenttest.context.ScenarioContext;
import com.jayway.jsonpath.JsonPath;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class AgreementTemplateSteps {

    private final InsertableAgreementTemplateRepository agreementTemplateRepository;
    private final ScenarioContext scenarioContext;

    @Given("agreement templates exist in the database with the following data")
    public void agreementTemplatesExistInTheDatabase(List<AgreementTemplateJpaEntity> templates) {
        log.debug("Seeding agreement templates: {}", templates);
        agreementTemplateRepository.insertAll(templates);
    }

    @Given("a single agreement template exists in the database with the following data")
    public void aSingleAgreementTemplateExistsInTheDatabase(AgreementTemplateJpaEntity template) {
        log.debug("Seeding agreement template: {}", template);
        var inserted = agreementTemplateRepository.insert(template);
        scenarioContext.setRequestedObjectId(inserted.getId().toString());
    }

    @When("the created agreement template id is stored as 'requestedObjectId'")
    public void theCreatedAgreementTemplateIdIsStoredAsRequestedObjectId() {
        var id = JsonPath.parse(scenarioContext.getStringResponseBody()).read("$.id").toString();
        scenarioContext.setRequestedObjectId(id);
    }
}
```

> `lockVersion = 0L` is set explicitly because `JpaEntityInserter` inserts EVERY entity field via
> reflection — a null `lock_version` would violate the NOT NULL column. Any DataTable column omitted
> from a scenario row parses to `null` via `DataTableHelper`, which is fine for nullable columns
> (`versionNumber`, `activatedAt`, `deactivatedAt`, `contentSha256`).

## 4. Validation Steps

Execute the following command to ensure the test sources compile. Do NOT run the full suite yet (the
feature file arrives in Task 025).

```bash
./gradlew :component-test:compileTestJava "-Dspring.profiles.active=test"
```

</task_file_template>
