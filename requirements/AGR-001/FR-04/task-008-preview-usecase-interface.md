<task_file_template>

# Task 008: Create the `PreviewAgreementPdfUseCase` interface (+ command record)

> **Applied Skill:** project use-case convention (interface in `application/usecase/`, implementation in
> `application/service/`) · `.claude/rules/java-style.md` (records for commands). Mirrors
> `CreateAgreementTemplateUseCase` which declares a nested `CreateAgreementTemplateCommand` record.

## 1. Objective

Declare the application contract for producing a preview PDF from an admin-supplied title + content, returning the raw
bytes. The command is a nested record on the interface, matching the existing agreement use-case style.

## 2. File to Modify / Create

* **File Path:** `service/src/main/java/com/github/jenkaby/bikerental/agreement/application/usecase/PreviewAgreementPdfUseCase.java`
* **Action:** Create New File

## 3. Code Implementation

Create the file with EXACTLY this content:

```java
package com.github.jenkaby.bikerental.agreement.application.usecase;

public interface PreviewAgreementPdfUseCase {

    byte[] execute(PreviewAgreementPdfCommand command);

    record PreviewAgreementPdfCommand(String title, String content) {
    }
}
```

## 4. Validation Steps

Execute the following command. Do NOT run the full application server.

```bash
./gradlew :service:compileJava "-Dspring.profiles.active=test"
```

</task_file_template>
