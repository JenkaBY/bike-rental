<task_file_template>

# Task 001: Declare Apache PDFBox in the Gradle version catalog

> **Applied Skill:** none (build configuration). Follows the existing `[versions]` / `[libraries]` declaration
> style already used in `gradle/libs.versions.toml` (e.g. `springdoc`, `archunit`).

## 1. Objective

Add the Apache PDFBox 3.0.5 coordinates to the shared Gradle version catalog so that both `service` (main
implementation) and `component-test` (test-scope, for `PDFTextStripper` assertions) can reference `libs.pdfbox`.

## 2. File to Modify / Create

* **File Path:** `gradle/libs.versions.toml`
* **Action:** Modify Existing File

## 3. Code Implementation

**Imports Required:**

None (TOML file).

**Code to Add/Replace:**

* **Location 1:** In the `[versions]` block, add a new line immediately AFTER the existing `jacoco = "0.8.14"` line.
* **Snippet:**

```toml
pdfbox = "3.0.5"
```

* **Location 2:** In the `[libraries]` block, add a new line immediately AFTER the existing
  `specification-arg-resolver = { module = "net.kaczmarzyk:specification-arg-resolver", version.ref = "specification-arg-resolver" }`
  line.
* **Snippet:**

```toml
pdfbox = { module = "org.apache.pdfbox:pdfbox", version.ref = "pdfbox" }
```

> Do NOT touch the `[bundles]`, `[plugins]`, or any test-only library entries. Only the two lines above are added.

## 4. Validation Steps

Execute the following command. Do NOT run the full application server.

```bash
./gradlew :service:help -q
```

A successful catalog parse (no `Invalid TOML catalog definition` error) confirms the entries are well-formed.

</task_file_template>
