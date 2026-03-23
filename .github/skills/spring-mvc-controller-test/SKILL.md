---
name: spring-mvc-controller-test
description: 'Write Spring Boot Web MVC controller unit tests following the project pattern. Use when asked to "write tests for controller", "add controller test", "cover controller with tests", "write BadRequest tests", "add validation tests for endpoint", or when a new REST controller needs to be tested. Covers @ApiTest setup, @MockitoBean declarations, @Nested structure per method, parameterized bad-request tests for path variables and request body fields, and error message naming conventions.'
---

# Spring MVC Controller Test

A skill for writing `@WebMvcTest`-based controller tests that follow the established project pattern.
Every new REST controller must have a corresponding test class in `src/test/java/.../web/` covering
input validation (path variables and request body).

## When to Use This Skill

- A new `@RestController` was created and needs test coverage
- Someone asks to "write tests for `<ControllerName>`"
- Validation annotations were added or changed on path variables or request body fields
- A new endpoint was added to an existing controller

---

## Prerequisites

Before writing tests, verify the following files exist in the **same module** as the controller:

| File                  | Location                                              |
|-----------------------|-------------------------------------------------------|
| `@ApiTest` annotation | `src/test/java/.../web/support/ApiTest.java`          |
| `TestingAppConfig`    | `src/test/java/.../web/support/TestingAppConfig.java` |

If they are missing, copy them from the `organizational-hierarchy` module and adjust the package.

### `ApiTest.java` template

```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ActiveProfiles("test")
@WebMvcTest
@Import({SecurityConfig.class, TestingAppConfig.class})
public @interface ApiTest {
    @AliasFor(annotation = WebMvcTest.class, attribute = "controllers")
    Class<?>[] controllers();
}
```

### `TestingAppConfig.java` template

```java
@TestConfiguration
public class TestingAppConfig {
    @TestConfiguration
    public static class MvcConfig implements MockMvcBuilderCustomizer {
        @Override
        public void customize(ConfigurableMockMvcBuilder<?> builder) {
            builder.apiVersionInserter(ApiVersionInserter.useHeader("X-API-Version"))
                    .alwaysDo(MockMvcResultHandlers.print());
        }
    }
}
```

---

## Step 1 — Analyse the Controller

Open the controller class and collect for each method:

1. **Method name** (camelCase, e.g. `getGroups`) — used in path-variable error messages.
2. **HTTP verb** (`GET`, `POST`, `PUT`, `DELETE`) — used in `MockMvcRequestBuilders`.
3. **URL template** — copy the path string, including `{pathVar}` placeholders.
4. **Path variables** — their order (0-based index) and validation annotations.
5. **Request body** (if present) — the record/class, all field annotations, including nested records.

### Validator → Error message reference

| Annotation on **path variable** | Input that triggers it          | `$.detail` contains                                   |
|---------------------------------|---------------------------------|-------------------------------------------------------|
| `@VersionableSlug`              | value with `#` (e.g. `"slug#"`) | `{methodName}.arg{N}: must not contain '#' character` |
| `@VersionableSlug`              | blank / whitespace              | `{methodName}.arg{N}: must not be blank`              |
| `@Version`                      | value with `#` (e.g. `"ver#"`)  | `{methodName}.arg{N}: must not contain '#' character` |
| `@Version`                      | blank / whitespace              | `{methodName}.arg{N}: must not be blank`              |
| `@Slug`                         | blank / whitespace              | `{methodName}.arg{N}: must not be blank`              |
| `@NotBlank`                     | blank / whitespace              | `{methodName}.arg{N}: must not be blank`              |

> **`{N}`** is the 0-based index of the annotated parameter in the method signature, counting **all** parameters
> including `@RequestBody`.

| Annotation on **request body field** | Input that triggers it                | `$.detail` contains                                     |
|--------------------------------------|---------------------------------------|---------------------------------------------------------|
| `@Slug`                              | null, blank, empty                    | `{fieldName}: must not be blank`                        |
| `@NotBlank`                          | null, blank, empty                    | `{fieldName}: must not be blank`                        |
| `@NotNull`                           | null                                  | `{fieldName}: must not be null`                         |
| `@NullOrNotBlank`                    | empty string `""` or whitespace `" "` | `{fieldName}: cannot be an empty string if provided.`   |
| `@NotNull` on list element           | null inside list                      | `{listField}[{index}]: must not be null`                |
| `@NotBlank` on nested record field   | null, blank                           | `{listField}[{index}].{nestedField}: must not be blank` |

---

## Step 2 — Create the Test Class Skeleton

Place the test class next to the controller in `src/test/java/.../web/`.
Name it `<ControllerSimpleName>Test.java`.

```java

@ApiTest(controllers = MyController.class)
class MyControllerTest {

    private static final String MY_API = "/api/my-resource/{pathVar}"; // copy from controller

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // Declare ALL constructor-injected dependencies of the controller as @MockitoBean
    @MockitoBean
    private MyService myService;

    @MockitoBean
    private MyWebMapper myWebMapper;

    // Add one @Nested class per controller method (Step 3)
}
```

**Rules for `@MockitoBean`:**

- Mock every `private final` field injected via `@RequiredArgsConstructor` or constructor.
- Do **not** mock `MockMvc` or `ObjectMapper` — they are auto-configured.

---

## Step 3 — Add a `@Nested` Class per Controller Method

For each controller method, add:

```java
@Nested
class MethodName {          // exact controller method name, PascalCase

    @Nested
    class BadRequest {

        // static Stream<Arguments> factories go here (Step 4)

        // @ParameterizedTest methods go here (Step 5)

        // plain @Test methods go here (Step 6)
    }
}
```

---

## Step 4 — Write `Arguments` Factories

### 4a. Path variable factory

Name the factory `invalidPaths()` (or `invalid<FieldName>s()` if testing a single variable).
Each `Arguments.of(...)` tuple must end with the **full expected error substring**.

```java
public static Stream<Arguments> invalidPaths() {
    return Stream.of(
            // @VersionableSlug: test '#' and blank
            Arguments.of("slug#", "1.0", "methodName.arg0: must not contain '#' character"),
            Arguments.of(" ",     "1.0", "methodName.arg0: must not be blank"),
            // @Version: test '#' and blank
            Arguments.of("slug",  "#",   "methodName.arg1: must not contain '#' character"),
            Arguments.of("slug",  " ",   "methodName.arg1: must not be blank"),
            // @Slug (plain): test blank only
            Arguments.of("slug",  "1.0", " ", "methodName.arg2: must not be blank")
    );
}
```

> Replace `methodName` with the actual Java method name and `arg0`, `arg1`... with the 0-based index
> of the annotated parameter in the method signature.

### 4b. Request body field factory

Create one factory per **group of related fields** (slug, displayName, nested list, list element):

```java
// --- slug field ---
public static Stream<Arguments> invalidSlugRequests() {
    return Stream.of(
            Arguments.of(new MyRequest(null,  "other"), "slug: must not be blank"),
            Arguments.of(new MyRequest(" ",   "other"), "slug: must not be blank"),
            Arguments.of(new MyRequest("",    "other"), "slug: must not be blank")
    );
}

// --- @NullOrNotBlank field ---
public static Stream<Arguments> invalidDisplayNameRequests() {
    return Stream.of(
            Arguments.of(new MyRequest("slug", ""),  "displayName: cannot be an empty string if provided."),
            Arguments.of(new MyRequest("slug", " "), "displayName: cannot be an empty string if provided.")
    );
}

// --- @NotNull List field variants ---
public static Stream<Arguments> invalidMembershipsListRequests() {
    return Stream.of(
            Arguments.of("null",     "memberships: must not be null"),
            Arguments.of("withNull", "memberships[1]: must not be null")
    );
}

// --- nested record field ---
public static Stream<Arguments> invalidMembershipRequests() {
    return Stream.of(
            Arguments.of(List.of(new Item(null, "role")), "memberships[0].personReference: must not be blank"),
            Arguments.of(List.of(new Item(" ",  "role")), "memberships[0].personReference: must not be blank"),
            Arguments.of(List.of(new Item("ref", null)), "memberships[0].membershipRoleReference: must not be blank")
    );
}
```

---

## Step 5 — Write `@ParameterizedTest` Methods

### 5a. Path variable test

```java
@MethodSource("invalidPaths")
@ParameterizedTest
void whenPathIsInvalid(String pathVar1, String pathVar2, String expectedError) throws Exception {
    mockMvc.perform(MockMvcRequestBuilders.get(MY_API, pathVar1, pathVar2))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.title").value("Bad Request"))
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.detail", containsString(expectedError)));
}
```

### 5b. Request body field test

```java
@MethodSource("invalidSlugRequests")
@ParameterizedTest
void whenSlugIsInvalid(MyRequest request, String expectedError) throws Exception {
    mockMvc.perform(MockMvcRequestBuilders.put(MY_API, "valid-slug", "1.0")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.title").value("Bad Request"))
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.detail", containsString(expectedError)));
}
```

### 5c. Null-in-list test (String selector pattern)

Use a `String` selector parameter when you need to construct a list that **contains `null`**,
because `List.of()` rejects nulls — use `ArrayList` instead.

```java
@MethodSource("invalidMembershipsListRequests")
@ParameterizedTest
void whenMembershipsListIsInvalid(String membershipsType, String expectedError) throws Exception {
    List<Item> memberships = switch (membershipsType) {
        case "null" -> null;
        case "withNull" -> {
            List<Item> list = new ArrayList<>();
            list.add(new Item("ref1", "role1"));
            list.add(null);
            yield list;
        }
        default -> throw new IllegalArgumentException("Unknown type: " + membershipsType);
    };

    var request = new MyRequest("slug", memberships);

    mockMvc.perform(MockMvcRequestBuilders.put(MY_API, "valid-slug", "1.0")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.title").value("Bad Request"))
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.detail", containsString(expectedError)));
}
```

---

## Step 6 — Add Edge-Case `@Test` Methods

When multiple violations must appear in a single response, or the scenario cannot be expressed
as a simple argument tuple, use a plain `@Test`:

```java
@Test
void whenMultipleFieldsHaveInvalidData() throws Exception {
    var memberships = List.of(
            new Item(null, "role1"),
            new Item("ref2", "")
    );
    var request = new MyRequest("slug", memberships);

    mockMvc.perform(MockMvcRequestBuilders.put(MY_API, "valid-slug", "1.0")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.title").value("Bad Request"))
            .andExpect(jsonPath("$.status").value(400))
            // check only the common prefix when multiple violations are present
            .andExpect(jsonPath("$.detail", containsString("memberships")));
}
```

---

## Complete Example Mapping

Given this controller method:

```java
@GetMapping("/{groupSlug}/children")
public ResponseEntity<List<GroupResponse>> getGroupChildren(
        @PathVariable("hierarchySlug") @VersionableSlug String hierarchySlug,  // arg0
        @PathVariable("hierarchyVersion") @Version String hierarchyVersion,     // arg1
        @PathVariable("groupSlug") @Slug String groupSlug) {                   // arg2
```

The `invalidPaths()` factory should be:

```java
public static Stream<Arguments> invalidPaths() {
    return Stream.of(
        Arguments.of("slug#",     "1.0",   "group1", "getGroupChildren.arg0: must not contain '#' character"),
        Arguments.of(" ",         "1.0",   "group1", "getGroupChildren.arg0: must not be blank"),
        Arguments.of("hierarchy", " ",     "group1", "getGroupChildren.arg1: must not be blank"),
        Arguments.of("hierarchy", "1.0",   " ",      "getGroupChildren.arg2: must not be blank")
    );
}
```

> Note: `@Version` also has `@Pattern(regexp = "^[^#]+$")` — add a `"#"`-containing test for
> `hierarchyVersion` if it is annotated `@Version` (same as `@VersionableSlug`).

---

## Required Imports

```java
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
```

---

## Checklist — Is the Test Complete?

- [ ] Class annotated with `@ApiTest(controllers = TheController.class)`
- [ ] `MockMvc` and `ObjectMapper` are `@Autowired`
- [ ] **Every** constructor-injected dependency of the controller is declared as `@MockitoBean`
- [ ] There is one `@Nested` class per public controller method
- [ ] Each `@Nested` method class contains an inner `@Nested class BadRequest`
- [ ] Path variable tests:
    - [ ] `@VersionableSlug` → test with `#`, test with blank (` `)
    - [ ] `@Version` → test with `#`, test with blank (` `)
    - [ ] `@Slug` → test with blank (` `)
    - [ ] Error string uses correct `{methodName}.arg{N}:` prefix
- [ ] Request body field tests:
    - [ ] `@Slug` / `@NotBlank` → null, blank (` `), empty (`""`)
    - [ ] `@NullOrNotBlank` → blank (` `) and empty (`""`) (null is valid, skip)
    - [ ] `@NotNull List` → null list, list-with-null-element using `ArrayList` + switch
    - [ ] Nested record `@NotBlank` / `@NotNull` fields covered
- [ ] No `List.of(null, ...)` — use `ArrayList` for null-containing lists
- [ ] `@MethodSource` value matches the exact static factory method name in the same class
- [ ] All assertions include `status().isBadRequest()`, `$.title`, `$.status`, `$.detail`, `$.errorCode`,
  `$.correlationId`

