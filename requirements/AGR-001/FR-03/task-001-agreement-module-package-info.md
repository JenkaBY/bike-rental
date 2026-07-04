<task_file_template>

# Task 001: Declare the Agreement Spring Modulith module

> **Applied Skill:** `spring-boot-modulith` — every bounded context is a Spring Modulith
> `@ApplicationModule` declared in `package-info.java` with an explicit `allowedDependencies`
> allowlist. `java-best-practices` — zero inline comments. Mirrors `customer/package-info.java`.

## 1. Objective

Create the new `agreement` module root package and declare it as a Spring Modulith application
module allowed to depend on `shared`, `customer`, and `rental` (customer/rental are declared now so
FR-04/FR-05 need no module reconfiguration; unused declarations are legal).

## 2. File to Modify / Create

* **File Path:** `service/src/main/java/com/github/jenkaby/bikerental/agreement/package-info.java`
* **Action:** Create New File

## 3. Code Implementation

Create the file with EXACTLY this content:

```java
@org.springframework.modulith.ApplicationModule(
        displayName = "Agreement Module",
        allowedDependencies = {"shared", "customer", "rental"})
package com.github.jenkaby.bikerental.agreement;
```

> This single file bootstraps the module. Do NOT add any other type to this package in this task —
> all agreement code lives in sub-packages (`domain`, `application`, `infrastructure`, `web`).

## 4. Validation Steps

Execute the following command to ensure this task was successful. Do NOT run the full application server.

```bash
./gradlew :service:compileJava "-Dspring.profiles.active=test"
```

</task_file_template>
