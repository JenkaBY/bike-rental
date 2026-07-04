<task_file_template>

# Task 031: Full verification of the signing feature

> **Applied Skill:** `spring-boot-modulith` — module boundaries must stay green (`ApplicationModules.verify()` runs in
> the existing `ModulithBoundariesTest`); the agreement module's `allowedDependencies = {"shared", "customer", "rental"}`
> already permit the new cross-module calls. This task runs the scoped unit tests, the component tests, and the modulith
> boundary test together. Depends on ALL previous tasks (001–030).

## 1. Objective

Confirm the whole FR-05 slice compiles, the new WebMvc tests pass, module boundaries stay valid, and the signing
component-test feature passes end-to-end. No new file is produced.

## 2. File to Modify / Create

* **File Path:** (none — verification only)
* **Action:** Modify Existing File

## 3. Code Implementation

No code changes. This task only runs the verification commands below and confirms they succeed.

## 4. Validation Steps

Assume the DB is already up. Execute, in order. Do NOT run the full application server.

```bash
./gradlew :service:test "-Dspring.profiles.active=test" --tests RentalSignatureCommandControllerTest --tests RentalSignatureQueryControllerTest
./gradlew :service:test "-Dspring.profiles.active=test" --tests ModulithBoundariesTest
./gradlew :component-test:test "-Dspring.profiles.active=test"
```

> If `ModulithBoundariesTest` is not the exact class name, run the module-verification test present under
> `service/src/test/java/.../ModulithBoundariesTest.java` or the equivalent `ApplicationModules.verify()` test; do not
> add new dependencies to `agreement/package-info.java` (it already allows `shared`, `customer`, `rental`).

</task_file_template>
