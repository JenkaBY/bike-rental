<task_file_template>

# Task 007: Fix RentalCommandControllerTest for the removed LifecycleStatus.ACTIVE constant

> **Applied Skill:** `unit-tests.md` / `spring-mvc-controller-test` — request-validation negative cases belong in
> WebMvc tests; `whenStatusIsActive()` becomes a 400 bad-request case (mirrors the existing
> `whenStatusIsInvalidEnumValue()` pattern: raw JSON body, no `LifecycleStatus` enum reference, since the enum no
> longer has an `ACTIVE` constant to reference). AssertJ/Given-When-Then conventions already followed by the
> surrounding tests are preserved. Depends on Task 005 (`LifecycleStatus.ACTIVE` must not exist for this task to make
> sense) and Task 006 (same file area, no functional overlap but keep sequencing).

## 1. Objective

Fix the two test methods in `RentalCommandControllerTest` that reference the now-deleted `LifecycleStatus.ACTIVE`
constant (which would otherwise fail to compile), covering fr.md's "Scenario 1: Lifecycle endpoint rejects ACTIVE"
acceptance criterion.

## 2. File to Modify / Create

* **File Path:** `service/src/test/java/com/github/jenkaby/bikerental/rental/web/command/RentalCommandControllerTest.java`
* **Action:** Modify Existing File

## 3. Code Implementation

**Imports Required:**

None — no import changes needed.

### Edit A — delete `whenStatusIsActive()` from the `ShouldReturn200` nested class

* **Location:** Inside `UpdateLifecycle.ShouldReturn200`, the first test method (currently around line 1424).

Delete this entire test method (including its `@Test`/`@DisplayName` annotations):

```java
            @Test
            @DisplayName("when status is ACTIVE")
            void whenStatusIsActive() throws Exception {
                var rental = mock(Rental.class);
                given(rentalLifecycleUseCase.execute(any(RentalLifecycleUseCase.RentalLifecycleCommand.class)))
                        .willReturn(rental);
                given(queryMapper.toResponse(any(Rental.class))).willReturn(mock(RentalResponse.class));

                mockMvc.perform(patch(LIFECYCLE_URL, 1L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(
                                        new RentalLifecycleRequest(LifecycleStatus.ACTIVE, "op-1"))))
                        .andExpect(status().isOk());
            }

```

Leave the remaining tests in `ShouldReturn200` (`whenStatusIsCancelled`, `whenStatusIsAwaitingSignature`,
`whenStatusIsDraft`) exactly as they are.

### Edit B — add a new `whenStatusIsActive()` test to the `BadRequest` nested class

* **Location:** Inside `UpdateLifecycle.BadRequest`, immediately after the existing
  `whenStatusIsInvalidEnumValue()` test method (which currently ends the class body before its closing brace).

Add this new test method right after `whenStatusIsInvalidEnumValue()` (before the closing `}` of the `BadRequest`
class), following the exact same pattern (raw JSON body, no `RentalLifecycleRequest`/`LifecycleStatus` usage):

```java

            @Test
            @DisplayName("when status is ACTIVE")
            void whenStatusIsActive() throws Exception {
                mockMvc.perform(patch(LIFECYCLE_URL, 1L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"status\": \"ACTIVE\", \"operatorId\": \"op-1\"}"))
                        .andExpect(status().isBadRequest());
            }
```

### Edit C — fix `whenRentalDoesNotExist()` in the `NotFound` nested class

* **Location:** Inside `UpdateLifecycle.NotFound`, the `whenRentalDoesNotExist()` test method (currently around line
  1525).

Replace this exact line:

```java
                mockMvc.perform(patch(LIFECYCLE_URL, 99L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(
                                        new RentalLifecycleRequest(LifecycleStatus.ACTIVE, "op-1"))))
                        .andExpect(status().isNotFound());
```

with:

```java
                mockMvc.perform(patch(LIFECYCLE_URL, 99L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(
                                        new RentalLifecycleRequest(LifecycleStatus.CANCELLED, "op-1"))))
                        .andExpect(status().isNotFound());
```

Do NOT touch `whenPatchRequestUsesValidStatusValues()` (the JSON-Patch `/status` path test) — it uses the string
literal `"ACTIVE"` inside a `RentalPatchOperation` value field, not the `LifecycleStatus` enum, so it still compiles
and is still correct as-is.

## 4. Validation Steps

Execute the following command. Do NOT run the full application server.

```bash
./gradlew :service:test "-Dspring.profiles.active=test" --tests RentalCommandControllerTest
```

</task_file_template>
