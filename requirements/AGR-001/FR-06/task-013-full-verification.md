<task_file_template>

# Task 013: Full verification of the FR-06 activation-only-through-signing change

> **Applied Skill:** `spring-boot-modulith` — module boundaries must stay green (`ApplicationModules.verify()` in the
> existing modulith boundary test); this FR touches no `package-info.java` / cross-module dependency declarations, so
> no new allowed-dependency entries are expected. This task runs the full scoped unit-test suite and the full
> component-test suite together as the final gate. Depends on ALL previous tasks (001–012).

## 1. Objective

Confirm the whole FR-06 slice compiles, the WebMvc lifecycle tests pass, module boundaries stay valid, and the
migrated/moved component-test scenarios pass end-to-end (fr.md Scenario 3: "the whole suite passes"). No new file is
produced.

## 2. File to Modify / Create

* **File Path:** (none — verification only)
* **Action:** Modify Existing File

## 3. Code Implementation

No code changes. This task only runs the verification commands below and confirms they succeed.

## 4. Validation Steps

Assume the DB is already up. Execute, in order. Do NOT run the full application server.

```bash
./gradlew :service:test "-Dspring.profiles.active=test" --tests RentalCommandControllerTest
./gradlew :service:test "-Dspring.profiles.active=test"
./gradlew :component-test:test "-Dspring.profiles.active=test"
```

> If a modulith boundary test class exists under `service/src/test/java/.../ModulithBoundariesTest.java` (or an
> equivalently named `ApplicationModules.verify()` test), it is already covered by the second command
> (`:service:test` runs the full suite); do not add new dependencies to any `package-info.java` — this FR introduces
> no new cross-module calls, only deletions and doc/test updates.

</task_file_template>
