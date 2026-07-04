<task_file_template>

# Task 014: Add PDFBox as a `component-test` test dependency

> **Applied Skill:** none (build configuration). Mirrors the existing `testImplementation libs.xxx` lines in
> `component-test/build.gradle`. Depends on Task 001 (catalog entry).

## 1. Objective

Put PDFBox on the `component-test` test classpath so the new step definition (Task 015) can parse the response body
with `PDFTextStripper` and assert extracted text / page count.

## 2. File to Modify / Create

* **File Path:** `component-test/build.gradle`
* **Action:** Modify Existing File

## 3. Code Implementation

**Imports Required:**

None (Gradle build file).

**Code to Add/Replace:**

* **Location:** In the `dependencies { ... }` block, add a new line immediately AFTER the existing
  `testImplementation libs.bundles.cucumber.test` line.
* **Snippet:**

```groovy
    testImplementation libs.pdfbox
```

> Keep the existing indentation (4 spaces). Do NOT alter any other dependency line.

## 4. Validation Steps

Execute the following command. Do NOT run the full application server.

```bash
./gradlew :component-test:dependencies --configuration testCompileClasspath -q
```

Confirm `org.apache.pdfbox:pdfbox:3.0.5` appears in the resolved test compile classpath.

</task_file_template>
