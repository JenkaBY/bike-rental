---
agent: 'agent'
tools: [ 'changes', 'search/codebase', 'edit/editFiles', 'problems', 'search' ]
description: 'Get best practices for JUnit 5 unit testing, including data-driven tests'
---

# JUnit 5+ Best Practices

Your goal is to help me write effective unit tests with JUnit 5, covering both standard and data-driven testing
approaches.

## Project Setup

- Use a standard Maven or Gradle project structure.
- Place test source code in `src/test/java`.
- Include dependencies for `junit-jupiter-api`, `junit-jupiter-engine`, and `junit-jupiter-params` for parameterized
  tests.
- Include dependencies for `assertj`, `awaitility` for assertations.
- Use build tool commands to run tests: `mvn test` or `gradle test`.

## Test Structure

- Test classes should have a `Test` suffix, e.g., `CalculatorTest` for a `Calculator` class.
- Use `@Test` for test methods.
- Follow the Given-When-Then pattern.
- Name tests using a descriptive convention, like `methodName_should_expectedBehavior_when_scenario`.
- Use `@BeforeEach` and `@AfterEach` for per-test setup and teardown.
- Use `@BeforeAll` and `@AfterAll` for per-class setup and teardown (must be static methods).
- Use `@DisplayName` to provide a human-readable name for test classes and methods.
- object returned by a method under test must be named `actual`. The expected object must be named `expected`.

## Standard Tests

- Keep tests focused on a single behavior.
- Avoid testing multiple conditions in one test method.
- Make tests independent and idempotent (can run in any order).
- Avoid test interdependencies.

## Data-Driven (Parameterized) Tests

- Use `@ParameterizedTest` to mark a method as a parameterized test.
- Use `@ValueSource` for simple literal values (strings, ints, etc.).
- Use `@MethodSource` to refer to a factory method that provides test arguments as a `Stream`, `Collection`, etc.
- Use `@CsvSource` for inline comma-separated values.
- Use `@CsvFileSource` to use a CSV file from the classpath.
- Use `@EnumSource` to use enum constants.

## Assertions

- Use fluent and readable assertions, consider using a library like AssertJ (`assertThat(...).is...`).
- Use `assertThatThrownBy` to test for exceptions.
- Group related assertions with `SoftAssertions.assertAll()` to ensure all assertions are checked before the test
  fails.
- Use descriptive messages in assertions to provide clarity on failure.

## Mocking and Isolation

- Use a mocking framework like Mockito to create mock objects for dependencies.
- Use `@Mock` and `@InjectMocks` annotations from Mockito to simplify mock creation and injection.
- Use interfaces to facilitate mocking.

## Test Organization

- Group tests by feature or component using packages.
- Use `@TestMethodOrder(MethodOrderer.OrderAnnotation.class)` and `@Order` to control test execution order when strictly
  necessary.
- Use `@Disabled` to temporarily skip a test method or class, providing a reason.
- Use `@Nested` to group tests in a nested inner class for better organization and structure.

## WebMvcTest (Spring Boot 4+)

- use `@WebMvcTest`(org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest) to test Spring MVC controllers in
  isolation.
- Mock dependencies using `@MockitoBean`(org.springframework.test.context.bean.override.mockito.MockitoBean)
- ObjectMapper comes from `tools.jackson.databind.ObjectMapper` packages
- Assume that injected services(useCases) are not throw any exceptions, they always return not null results. Mock them
  accordingly.
- Don't verify response fields for positive scenarios, only check HTTP status codes
- Don't verify any 409, 500, 415 status codes, assume that the controller never throws them.
- Group test cases by HTTP methods(GET, POST, PUT, DELETE) using nested classes with `@Nested` annotation. E.g.

```java

@ApiTest
class ControllerTest {

  @Nested
  class Post {
    @Nested
    class ShouldReturn404 {
      @Test
      @ParametrizedTest
      void whenRequestContainsInvalidData(CreateRequest request, Expected expected) {
        // test scenario here
      }
    }

    @Nested
    class ShouldReturn201 {
      @ParametrizedTest
      void whenRequestInvalidData(CreateRequest request) {
        // test scenario here
      }

      @Test
      void mayBeAnotherUseCase() {
        // test scenario here
      }
    }
  }

  @Nested
  class Get {
    // similar structure for GET requests
  }
}
```