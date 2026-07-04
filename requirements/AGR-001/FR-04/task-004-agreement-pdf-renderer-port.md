<task_file_template>

# Task 004: Create the `AgreementPdfRenderer` domain port interface

> **Applied Skill:** `spring-boot-modulith` / project hexagonal convention — domain ports are interfaces under
> `domain/service/`; the application layer depends on the port, the infrastructure layer provides the implementation
> (the application layer may not depend on infrastructure per `ModulithBoundariesTest`).

## 1. Objective

Define the reusable rendering contract: given `AgreementPdfData`, produce the PDF as a `byte[]`. FR-04 (preview) and
FR-05 (signing) both depend on this single port. Depends on Task 003 (`AgreementPdfData`).

## 2. File to Modify / Create

* **File Path:** `service/src/main/java/com/github/jenkaby/bikerental/agreement/domain/service/AgreementPdfRenderer.java`
* **Action:** Create New File

## 3. Code Implementation

Create the file with EXACTLY this content:

```java
package com.github.jenkaby.bikerental.agreement.domain.service;

import com.github.jenkaby.bikerental.agreement.domain.model.AgreementPdfData;

public interface AgreementPdfRenderer {

    byte[] render(AgreementPdfData data);
}
```

> This is a pure port. Do NOT annotate it with `@Component`. Do NOT reference PDFBox or any infrastructure type here.

## 4. Validation Steps

Execute the following command. Do NOT run the full application server.

```bash
./gradlew :service:compileJava "-Dspring.profiles.active=test"
```

</task_file_template>
