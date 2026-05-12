# Task 004: WebMvc Tests for Lifecycle Endpoint

> **Applied Skill:** `spring-mvc-controller-test/SKILL.md` — `@ApiTest`, `@MockitoBean`,
> `@Nested` per method, parameterized bad-request tests

## 1. Objective

Add `@Nested class UpdateLifecycle` to `RentalCommandControllerTest` covering: happy paths
for ACTIVE and CANCELLED, all validation error cases (null status, missing status, invalid enum
value), and a 404 path.

## 2. File to Modify / Create

* **File Path:**
  `service/src/test/java/com/github/jenkaby/bikerental/rental/web/command/RentalCommandControllerTest.java`
* **Action:** Modify Existing File

## 3. Code Implementation

**Imports Required:**

Add to the existing import block (only those not already present):

```java
import com.github.jenkaby.bikerental.rental.application.usecase.RentalLifecycleUseCase;
import com.github.jenkaby.bikerental.rental.web.command.dto.RentalLifecycleRequest;
import com.github.jenkaby.bikerental.rental.web.command.dto.LifecycleStatus;
import com.github.jenkaby.bikerental.shared.exception.ResourceNotFoundException;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
```

> `patch`, `MockMvc`, `ObjectMapper`, `MockitoBean`, `given`, `mock`, `status()`,
> `jsonPath` are already imported from the existing test class.

**Step 1 — Add `@MockitoBean` for `RentalLifecycleUseCase`.**

* **Location:** After the existing `@MockitoBean private RentalQueryMapper queryMapper;` field.

```java
    @MockitoBean
    private RentalLifecycleUseCase rentalLifecycleUseCase;
```

**Step 2 — Add the `@Nested` test class.**

* **Location:** After the last existing `@Nested` class inside `RentalCommandControllerTest`,
  before the closing `}` of the outer class.

```java
    @Nested
    @DisplayName("PATCH /api/rentals/{rentalId}/lifecycles")
    class UpdateLifecycle {

        private static final String LIFECYCLE_URL = "/api/rentals/{rentalId}/lifecycles";

        @Nested
        @DisplayName("Should return 200 OK")
        class ShouldReturn200 {

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

            @Test
            @DisplayName("when status is CANCELLED")
            void whenStatusIsCancelled() throws Exception {
                var rental = mock(Rental.class);
                given(rentalLifecycleUseCase.execute(any(RentalLifecycleUseCase.RentalLifecycleCommand.class)))
                        .willReturn(rental);
                given(queryMapper.toResponse(any(Rental.class))).willReturn(mock(RentalResponse.class));

                mockMvc.perform(patch(LIFECYCLE_URL, 1L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(
                                        new RentalLifecycleRequest(LifecycleStatus.CANCELLED, null))))
                        .andExpect(status().isOk());
            }
        }

        @Nested
        @DisplayName("Should return 400 Bad Request")
        class BadRequest {

            @Test
            @DisplayName("when status is null")
            void whenStatusIsNull() throws Exception {
                mockMvc.perform(patch(LIFECYCLE_URL, 1L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"status\": null}"))
                        .andExpect(status().isBadRequest());
            }

            @Test
            @DisplayName("when status field is missing")
            void whenStatusFieldIsMissing() throws Exception {
                mockMvc.perform(patch(LIFECYCLE_URL, 1L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{}"))
                        .andExpect(status().isBadRequest());
            }

            @Test
            @DisplayName("when status is an invalid enum value")
            void whenStatusIsInvalidEnumValue() throws Exception {
                mockMvc.perform(patch(LIFECYCLE_URL, 1L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"status\": \"COMPLETED\"}"))
                        .andExpect(status().isBadRequest());
            }

            @Test
            @DisplayName("when status is DRAFT")
            void whenStatusIsDraft() throws Exception {
                mockMvc.perform(patch(LIFECYCLE_URL, 1L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"status\": \"DRAFT\"}"))
                        .andExpect(status().isBadRequest());
            }
        }

        @Nested
        @DisplayName("Should return 404 Not Found")
        class NotFound {

            @Test
            @DisplayName("when rental does not exist")
            void whenRentalDoesNotExist() throws Exception {
                given(rentalLifecycleUseCase.execute(any(RentalLifecycleUseCase.RentalLifecycleCommand.class)))
                        .willThrow(new ResourceNotFoundException(Rental.class, "99"));

                mockMvc.perform(patch(LIFECYCLE_URL, 99L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(
                                        new RentalLifecycleRequest(LifecycleStatus.ACTIVE, "op-1"))))
                        .andExpect(status().isNotFound());
            }
        }
    }
```

## 4. Validation Steps

```bash
./gradlew :service:test "-Dspring.profiles.active=test" --tests RentalCommandControllerTest
```
