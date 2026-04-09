# Task 001: Add specification-arg-resolver Dependency

> **Applied Skill:** `spring-boot-data-ddd` — Repositories & Queries (Specification pattern for dynamic filtering)

## 1. Objective

Add the `net.kaczmarzyk:specification-arg-resolver` library to the Gradle version catalog and to the `service` module
dependencies so that the subsequent tasks can use `@Spec` annotations on controller method parameters.

## 2. File to Modify / Create

### 2a.

* **File Path:** `gradle/libs.versions.toml`
* **Action:** Modify Existing File

### 2b.

* **File Path:** `service/build.gradle`
* **Action:** Modify Existing File

## 3. Code Implementation

### 2a — `gradle/libs.versions.toml`

**Location:** In the `[versions]` block, after the line `uuidV7Generator = '6.1.1'`

**Snippet:**

```toml
specification-arg-resolver = '4.0.0'
```

**Location:** In the `[libraries]` block, after the line that declares `uuid-v7-generator`

**Snippet:**

```toml
specification-arg-resolver = { module = "net.kaczmarzyk:specification-arg-resolver", version.ref = "specification-arg-resolver" }
```

---

### 2b — `service/build.gradle`

**Location:** In the `dependencies { }` block, directly after the line `implementation libs.uuid.v7.generator`

**Snippet:**

```groovy
implementation libs.specification.arg.resolver
```

## 4. Validation Steps

```bash
./gradlew :service:dependencies --configuration compileClasspath | findstr "specification"
```

Expected: a line containing `net.kaczmarzyk:specification-arg-resolver:4.0.0` in the output.
