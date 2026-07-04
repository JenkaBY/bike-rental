<task_file_template>

# Task 027: Create the SignatureSummaryResponseTransformer

> **Applied Skill:** `spring-boot-java-cucumber` — a `transformer/` `@DataTableType` producing the expected response
> record for the `the signature list contains` Then-step; only the columns present in a scenario are populated (others
> null, matched leniently by the step). Depends on Task 018.

## 1. Objective

Create the transformer that converts expected-response DataTable rows into `SignatureSummaryResponse` for assertion.

## 2. File to Modify / Create

* **File Path:** `component-test/src/test/java/com/github/jenkaby/bikerental/componenttest/transformer/SignatureSummaryResponseTransformer.java`
* **Action:** Create New File

## 3. Code Implementation

Create the file with EXACTLY this content:

```java
package com.github.jenkaby.bikerental.componenttest.transformer;

import com.github.jenkaby.bikerental.agreement.web.query.dto.SignatureSummaryResponse;
import io.cucumber.java.DataTableType;

import java.util.Map;

public class SignatureSummaryResponseTransformer {

    @DataTableType
    public SignatureSummaryResponse signatureSummaryResponse(Map<String, String> entry) {
        return new SignatureSummaryResponse(
                DataTableHelper.toLong(entry, "signatureId"),
                DataTableHelper.toLong(entry, "templateId"),
                DataTableHelper.toInt(entry, "templateVersionNumber"),
                null);
    }
}
```

## 4. Validation Steps

Execute the following command. Do NOT run the full application server.

```bash
./gradlew :component-test:compileTestJava "-Dspring.profiles.active=test"
```

</task_file_template>
