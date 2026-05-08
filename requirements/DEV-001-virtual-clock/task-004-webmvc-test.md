# ~~Task 004~~: Removed

WebMvc tests for `TimeTravelController` are not required.
This task has been removed per product owner decision.

**Next task:** `task-005-component-test.md`

## 1. Objective

Create a WebMvc slice test that exercises all three endpoints of `DevTimeController`:

- `PUT /api/dev/time` — valid payload (200), missing `instant` field (400), non-parseable value (400), missing body (
  400).
- `DELETE /api/dev/time` — 204 returned; idempotent (second call also 204).
- `GET /api/dev/time` — response Content-Type is `text/event-stream`.

`DevClockConfig` is imported into the test slice so the real `SettableClock` bean is available. No `@MockitoBean` is
needed; state changes are exercised by calling the endpoints directly.

## 2. File to Modify / Create

* **File Path:** `service/src/test/java/com/github/jenkaby/bikerental/shared/web/DevTimeControllerTest.java`
* **Action:** Create New File

## 3. Code Implementation

**Imports Required:**

All imports are listed inside the snippet below.

**Code to Add/Replace:**

* **Location:** New file — paste the entire snippet as-is.

```java
package com.github.jenkaby.bikerental.shared.web;

import com.github.jenkaby.bikerental.shared.config.DevClockConfig;
import com.github.jenkaby.bikerental.support.web.ApiTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ApiTest(controllers = DevTimeController.class)
@Import(DevClockConfig.class)
class DevTimeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Nested
    class PutTime {

        @Nested
        class ShouldReturn200 {

            @Test
            void whenInstantIsValidUtc() throws Exception {
                mockMvc.perform(put("/api/dev/time")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"instant\":\"2026-01-01T00:00:00Z\"}"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.instant").value("2026-01-01T00:00:00Z"));
            }

            @Test
            void whenInstantHasPositiveOffset() throws Exception {
                mockMvc.perform(put("/api/dev/time")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"instant\":\"2026-06-15T10:30:00+03:00\"}"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.instant").value("2026-06-15T07:30:00Z"));
            }
        }

        @Nested
        class ShouldReturn400 {

            @Test
            void whenBodyIsAbsent() throws Exception {
                mockMvc.perform(put("/api/dev/time")
                                .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isBadRequest());
            }

            @Test
            void whenInstantFieldIsMissing() throws Exception {
                mockMvc.perform(put("/api/dev/time")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{}"))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.errorCode").value("shared.method_arguments.validation_failed"))
                        .andExpect(jsonPath("$.errors[0].field").value("instant"));
            }

            @ParameterizedTest
            @ValueSource(strings = {
                    "{\"instant\":\"not-a-date\"}",
                    "{\"instant\":\"2026-01-01\"}",
                    "{\"instant\":\"2026-01-01T00:00:00\"}"
            })
            void whenInstantIsNotParseable(String body) throws Exception {
                mockMvc.perform(put("/api/dev/time")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                        .andExpect(status().isBadRequest());
            }
        }
    }

    @Nested
    class DeleteTime {

        @Test
        void shouldReturn204() throws Exception {
            mockMvc.perform(delete("/api/dev/time"))
                    .andExpect(status().isNoContent());
        }

        @Test
        void shouldReturn204WhenCalledTwice() throws Exception {
            mockMvc.perform(delete("/api/dev/time"))
                    .andExpect(status().isNoContent());
            mockMvc.perform(delete("/api/dev/time"))
                    .andExpect(status().isNoContent());
        }
    }

    @Nested
    class GetTime {

        @Test
        void shouldReturnTextEventStreamContentType() throws Exception {
            mockMvc.perform(get("/api/dev/time").accept(MediaType.TEXT_EVENT_STREAM))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM));
        }
    }
}
```

### Key implementation notes for the Junior Agent

1. `@ApiTest(controllers = DevTimeController.class)` activates the `test` profile automatically via the existing
   `@ActiveProfiles("test")` on `@ApiTest`.
2. `@Import(DevClockConfig.class)` is required because `@WebMvcTest` does not scan `@Configuration` classes outside the
   controller slice. Without it, `SettableClock` is not in the context and the controller fails to start.
3. The `test` profile satisfies both `DevClockConfig`'s and `DevTimeController`'s `@Profile({"dev","test"})` conditions.
4. The error code `"shared.method_arguments.validation_failed"` matches `ErrorCodes.METHOD_ARGUMENTS_VALIDATION_FAILED`
   as returned by `CoreExceptionHandlerAdvice.handleError(MethodArgumentNotValidException)`.
5. Sending `{}` with `instant` absent deserialises `Instant` to `null`, triggering `@NotNull` and a
   `MethodArgumentNotValidException` with `errors[0].field = "instant"`.
6. Sending a non-parseable string (e.g., `"not-a-date"`) causes Jackson to throw `HttpMessageNotReadableException`,
   which the global advice maps to 400 — the test only asserts the status code for those cases.
7. `whenInstantHasPositiveOffset` verifies UTC normalisation: Jackson's `InstantDeserializer` converts `+03:00` to the
   equivalent UTC instant before storing.

## 4. Validation Steps

```bash
./gradlew :service:test "-Dspring.profiles.active=test" --tests DevTimeControllerTest
```
