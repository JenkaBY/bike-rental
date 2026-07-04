<task_file_template>

# Task 002: Add PDFBox as a `service` implementation dependency

> **Applied Skill:** none (build configuration). Mirrors the existing `implementation libs.xxx` lines in
> `service/build.gradle`.

## 1. Objective

Put `org.apache.pdfbox:pdfbox` on the `service` module's runtime/compile classpath so the infrastructure renderer
(Task 007) can use the PDFBox API. Depends on Task 001 (catalog entry).

## 2. File to Modify / Create

* **File Path:** `service/build.gradle`
* **Action:** Modify Existing File

## 3. Code Implementation

**Imports Required:**

None (Gradle build file).

**Code to Add/Replace:**

* **Location:** In the `dependencies { ... }` block, add a new line immediately AFTER the existing
  `implementation libs.specification.arg.resolver` line.
* **Snippet:**

```groovy
    implementation libs.pdfbox
```

> Keep the existing indentation (4 spaces). Do NOT alter any other dependency line.

## 4. Validation Steps

Execute the following command. Do NOT run the full application server.

```bash
./gradlew :service:dependencies --configuration compileClasspath -q
```

Confirm `org.apache.pdfbox:pdfbox:3.0.5` appears in the resolved compile classpath.

</task_file_template>
