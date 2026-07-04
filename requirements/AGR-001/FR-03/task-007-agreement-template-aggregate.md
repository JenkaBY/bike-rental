<task_file_template>

# Task 007: Create the AgreementTemplate aggregate

> **Applied Skill:** `spring-boot-data-ddd` — aggregate root guards its own invariants; lifecycle
> transitions throw domain exceptions. `java-best-practices` — Lombok `@Getter @Builder`, zero inline
> comments, `final`-by-default. Mirrors `rental/domain/model/Rental.java` (static factory
> `createDraft`, mutating domain methods that validate status then flip state and bump `updatedAt`).

## 1. Objective

Implement the `AgreementTemplate` aggregate: the DRAFT factory, `updateContent`, `activate`,
`deactivate`, and `ensureDeletable`, each guarding the strictly linear lifecycle. Depends on Task 004
(`AgreementTemplateStatus`) and Task 005 (the four exceptions).

## 2. File to Modify / Create

* **File Path:** `service/src/main/java/com/github/jenkaby/bikerental/agreement/domain/model/AgreementTemplate.java`
* **Action:** Create New File

## 3. Code Implementation

Create the file with EXACTLY this content:

```java
package com.github.jenkaby.bikerental.agreement.domain.model;

import com.github.jenkaby.bikerental.agreement.domain.exception.AgreementTemplateNotActivatableException;
import com.github.jenkaby.bikerental.agreement.domain.exception.AgreementTemplateNotDeletableException;
import com.github.jenkaby.bikerental.agreement.domain.exception.AgreementTemplateNotEditableException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AgreementTemplate {

    @Setter
    private Long id;

    private Long lockVersion;

    private Integer versionNumber;

    private String title;

    private String content;

    private String contentSha256;

    private AgreementTemplateStatus status;

    private Instant createdAt;
    private Instant updatedAt;
    private Instant activatedAt;
    private Instant deactivatedAt;

    public static AgreementTemplate createDraft(String title, String content) {
        Instant now = Instant.now();
        return AgreementTemplate.builder()
                .title(title)
                .content(content)
                .status(AgreementTemplateStatus.DRAFT)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    public void updateContent(String title, String content) {
        if (this.status != AgreementTemplateStatus.DRAFT) {
            throw new AgreementTemplateNotEditableException(this.status);
        }
        this.title = title;
        this.content = content;
        this.updatedAt = Instant.now();
    }

    public void activate(int versionNumber, String contentSha256, Instant activatedAt) {
        if (this.status != AgreementTemplateStatus.DRAFT) {
            throw new AgreementTemplateNotActivatableException(this.status);
        }
        this.versionNumber = versionNumber;
        this.contentSha256 = contentSha256;
        this.status = AgreementTemplateStatus.ACTIVE;
        this.activatedAt = activatedAt;
        this.updatedAt = activatedAt;
    }

    public void deactivate(Instant deactivatedAt) {
        if (this.status != AgreementTemplateStatus.ACTIVE) {
            throw new AgreementTemplateNotActivatableException(this.status);
        }
        this.status = AgreementTemplateStatus.DEACTIVATED;
        this.deactivatedAt = deactivatedAt;
        this.updatedAt = deactivatedAt;
    }

    public void ensureDeletable() {
        if (this.status != AgreementTemplateStatus.DRAFT) {
            throw new AgreementTemplateNotDeletableException(this.status);
        }
    }
}
```

> `deactivate` throws `AgreementTemplateNotActivatableException` because the only path that
> deactivates a template is the activation flow (Task 010): a non-ACTIVE "current" is an activation
> conflict, and the advice (Task 015) maps that code to 409, which is exactly the desired outcome.

## 4. Validation Steps

Execute the following command. Do NOT run the full application server.

```bash
./gradlew :service:compileJava "-Dspring.profiles.active=test"
```

</task_file_template>
