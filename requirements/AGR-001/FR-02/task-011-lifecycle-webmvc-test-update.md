<task_file_template>

# Task 011: Update RentalCommandControllerTest for the widened LifecycleStatus enum

> **Applied Skill:** `spring-mvc-controller-test` / `junit-best-practices` — `@ApiTest` slice, `@Nested` per
> HTTP outcome, AssertJ-free MockMvc `andExpect`, request-validation negatives belong here (not in component
> tests). See [.claude/rules/unit-tests.md](../../../.claude/rules/unit-tests.md).

## 1. Objective

`DRAFT` and `AWAITING_SIGNATURE` are now valid enum values. The existing test `whenStatusIsDraft()` asserts a
`400` for `DRAFT`, which is no longer correct — move `DRAFT` and `AWAITING_SIGNATURE` into the `200` group and
keep only genuinely-unknown values (e.g. `COMPLETED`, `FOO`) in the `400` group.

## 2. File to Modify / Create

* **File Path:** `service/src/test/java/com/github/jenkaby/bikerental/rental/web/command/RentalCommandControllerTest.java`
* **Action:** Modify Existing File

## 3. Code Implementation

**Imports Required:** No new imports (the test already uses `LifecycleStatus`, `RentalLifecycleRequest`,
`MediaType`, `Rental`, `RentalResponse`, `RentalLifecycleUseCase`, static `patch`, `status`, `given`, `any`,
`mock`).

**Code to Add/Replace:**

### Change 3.1 — Add two 200 cases inside `UpdateLifecycle.ShouldReturn200`

* **Location:** Inside `class ShouldReturn200 {` (the nested class under `class UpdateLifecycle`), directly
  AFTER the existing `whenStatusIsCancelled()` test method and BEFORE the closing `}` of `ShouldReturn200`.
* **Snippet:**

```java
            @Test
            @DisplayName("when status is AWAITING_SIGNATURE")
            void whenStatusIsAwaitingSignature() throws Exception {
                var rental = mock(Rental.class);
                given(rentalLifecycleUseCase.execute(any(RentalLifecycleUseCase.RentalLifecycleCommand.class)))
                        .willReturn(rental);
                given(queryMapper.toResponse(any(Rental.class))).willReturn(mock(RentalResponse.class));

                mockMvc.perform(patch(LIFECYCLE_URL, 1L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(
                                        new RentalLifecycleRequest(LifecycleStatus.AWAITING_SIGNATURE, "op-1"))))
                        .andExpect(status().isOk());
            }

            @Test
            @DisplayName("when status is DRAFT")
            void whenStatusIsDraft() throws Exception {
                var rental = mock(Rental.class);
                given(rentalLifecycleUseCase.execute(any(RentalLifecycleUseCase.RentalLifecycleCommand.class)))
                        .willReturn(rental);
                given(queryMapper.toResponse(any(Rental.class))).willReturn(mock(RentalResponse.class));

                mockMvc.perform(patch(LIFECYCLE_URL, 1L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(
                                        new RentalLifecycleRequest(LifecycleStatus.DRAFT, "op-1"))))
                        .andExpect(status().isOk());
            }
```

### Change 3.2 — Remove the now-obsolete `whenStatusIsDraft()` test from the `BadRequest` group

* **Location:** Inside `class BadRequest {` (nested under `class UpdateLifecycle`), delete the ENTIRE existing
  method block that reads exactly:

```java
            @Test
            @DisplayName("when status is DRAFT")
            void whenStatusIsDraft() throws Exception {
                mockMvc.perform(patch(LIFECYCLE_URL, 1L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"status\": \"DRAFT\", \"operatorId\": \"op-1\"}"))
                        .andExpect(status().isBadRequest());
            }
```

> Do NOT remove `whenStatusIsInvalidEnumValue()` (asserting 400 for `COMPLETED`) — `COMPLETED` remains an
> invalid `LifecycleStatus`, so that negative case is still correct and must stay. Do NOT touch the `NotFound`
> nested class.

## 4. Validation Steps

Execute the following command to ensure this task was successful. Do NOT run the full application server.

```bash
./gradlew :service:test "-Dspring.profiles.active=test" --tests RentalCommandControllerTest
```

</task_file_template>
