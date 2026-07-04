<task_file_template>

# Task 025: Create the SignAgreementRequestTransformer

> **Applied Skill:** `spring-boot-java-cucumber` — DataTable-to-request conversion lives in a `transformer/` class named
> `{Domain}RequestTransformer` with a `@DataTableType` method (mirrors `AgreementTemplateRequestTransformer`); step
> classes NEVER convert DataTables. The feature table supplies only `rentalVersion`/`templateId`/`operatorId`; the valid
> base64 1x1 PNG is a hard-coded constant here so feature files stay free of long literals. Depends on Task 018.

## 1. Objective

Create the transformer that builds a `SignAgreementRequest` from a feature DataTable row, injecting a fixed valid
base64-encoded 1x1 PNG for `signaturePng`.

## 2. File to Modify / Create

* **File Path:** `component-test/src/test/java/com/github/jenkaby/bikerental/componenttest/transformer/SignAgreementRequestTransformer.java`
* **Action:** Create New File

## 3. Code Implementation

Create the file with EXACTLY this content:

```java
package com.github.jenkaby.bikerental.componenttest.transformer;

import com.github.jenkaby.bikerental.agreement.web.command.dto.SignAgreementRequest;
import io.cucumber.java.DataTableType;

import java.util.Map;

public class SignAgreementRequestTransformer {

    public static final String VALID_SIGNATURE_PNG_BASE64 =
            "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==";

    @DataTableType
    public SignAgreementRequest signAgreementRequest(Map<String, String> entry) {
        return new SignAgreementRequest(
                VALID_SIGNATURE_PNG_BASE64,
                DataTableHelper.toLong(entry, "rentalVersion"),
                DataTableHelper.toLong(entry, "templateId"),
                DataTableHelper.getStringOrNull(entry, "operatorId"));
    }
}
```

## 4. Validation Steps

Execute the following command. Do NOT run the full application server.

```bash
./gradlew :component-test:compileTestJava "-Dspring.profiles.active=test"
```

</task_file_template>
