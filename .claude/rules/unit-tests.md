---
paths:
  - "service/src/test/**"
---

# Unit / WebMvc Tests (`service` module) — hard constraints

- Controller slice tests use the `@ApiTest` meta-annotation (`support/web/ApiTest.java`) — **never bare
  `@WebMvcTest`**. It activates profile `test` and imports `TestingAppConfig` (registers `CorsProperties`, imports
  `UuidCreatorAdapter` + `BaseValidationErrorMapper`).
- Use **`@MockitoBean`** (not the deprecated `@MockBean`) for Spring-managed dependencies.
- **AssertJ only** — `assertThat(actual).as("description")...`; no Hamcrest/JUnit assertions.
- Name the result of the method under test `actual`; the expectation `expected`. Follow Given-When-Then.
- Request-validation **negative cases belong here** (WebMvc tests), not in component tests.
- Verify with: `./gradlew :service:test "-Dspring.profiles.active=test" --tests <TestClassName>`

Depth: `junit-best-practices` and `spring-mvc-controller-test` skills.
