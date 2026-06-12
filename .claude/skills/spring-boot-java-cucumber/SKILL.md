---
name: spring-boot-java-cucumber
description: 'Spring Boot component testing with Cucumber BDD. Use for component or integration testing of Spring Boot applications.'
---

# Spring Boot Java Cucumber Testing

A comprehensive skill for implementing component tests using Cucumber BDD framework in Spring Boot applications. This
skill covers test structure, step definitions, naming convention, context management, and best practices for
behavior-driven testing.

## When to Use This Skill

- Before writing a component or integration test
- When user asks to write component tests, to cover an implementation by integration or component tests

## Prerequisites

- Spring Boot application with web endpoints or message listeners
- Gradle or Maven build tool
- JUnit 5+ (Jupiter)
- Cucumber dependencies configured and ready to use
- AssertJ for assertions

## Project Structure

```
component-test/
├── src/test/java/{project/package/structure}/compoenttest/
│   ├── RunComponentTests.java                # Test suite runner
│   ├── config/                               # Test configuration
│   ├── context/                              # Test state management (@ScenarioScope)
│   ├── model/                                # Test models and DTOs
│   ├── infrastructure/                       # External system integration(e.g. Kafka)
│   ├── steps/                                # Step definitions
│   │   ├── common/                           # Reusable steps (HTTP request/response, DB steps)
│   │   │   └── hook/                         # Lifecycle hooks (clear scenario contexts, DB, message storage and etc.)
│   │   └── {module}/                        # Feature-specific steps
│   │       └── {Module}WebSteps.java        # Feature-specific web related steps
│   │       └── {Module}DbSteps.java         # Feature-specific db related steps
│   │       └── {Module}MessagingSteps.java  # Feature-specific messaging related steps
│   └── transformer/                          # Data transformers(Parameter transformers, )
│       └── ParameterTypes.java               # Custom parameter types
│       └── {Module}/
|          └── {ModelOrEntity}Transformer.java   # Datatable transformers. Converts DataTable to domain objects
└── src/test/resources/       
    ├── features/                             # Gherkin feature files
    │   ├── application.feature               # Basic app tests
    │   └── {module}/                         # Organized by module
    │       └── {feature-name}.feature        # Feature scenarios
    ├── application-test.yaml                 # Test profile configuration
    └── junit-platform.properties             # Cucumber configuration
```

## Gherkin Best Practices

## Naming Conventions

- Create domain-specific steps for your features
- Organize step definitions by feature AND infrastructure layer. Split steps into separate classes based on the
  infrastructure they interact with. The pattern is '{ModelOrEntity}{Infra}Steps' E.g. `UserWebSteps`, `UserDbSteps`,
  `UserMessagingSteps`
- Transformer names should follow the pattern '{ModelOrEntity}Transformer'. E.g. `UserTransformer`, `OrderTransformer`,
  `UserCreateRequestTransformer`

## Testing Strategy

### Component Tests

- Test happy paths and business validations
- Don't cover requests validation
- Verify integration between components
- Verify final state of components (e.g. DB state, Kafka topics, queues and so on) by reading from real infrastructure
- Clear state between scenarios
- Use scenario context to share state between steps
- Mock external dependencies (e.g. third-party services)
- Test against real infrastructure (DB, messaging and so on)
- Focus on business scenarios from user stories

## Best Practices and project constraints

- **Isolation**: Each scenario should be independent
- **Reusability**: Keep common steps (like common Db steps, performing http requests, sending messages and so) in
   `steps/common/`
- **Debugging**: Log request/response in hooks for failed scenarios
- **Organization**: Group features by domain module
- **Naming**: Use descriptive scenario names tied to user stories
- **Assertions**: Use AssertJ for fluent, readable assertions
- **Context**: Keep scenario context clean and minimal
- **Don't repeat yourself**: Don't duplicate already existing steps. Try to reuse them, refactor existing ones to be
  reusable
- **No comments**: Avoid comments in the code and Gherkin files
- Use `Feature:` to describe the user story. Keep it short but descriptive. Don't use a ticket or user story reference
- Use `Background:` for common setup across scenarios
- Prefer to use `Scenario Outline:` with `Examples:` for data-driven tests.
  See [the example](references/data-driven-scenario.feature)
- Use descriptive scenario names that explain the expected outcome
- Never use JSON to pass it as request. Split complex request into several steps and then merge in a final request.
  See [the example](references/datatable-transformers.md)
- If datatable becomes too large, split object creation into several steps and save intermediate state in scenario
  context. See [the example](references/datatable-transformers.md)
- Use transformers to convert datatables directly into domain objects and avoid using JSON in steps.
  See [the example](references/datatable-transformers.md) Public method in the transformer should be annotated with
  `@DataTableType` and have the `transform` name.
- Use `Scenario Outline:` with `Examples:` for data repeated across multiple steps.
  See [the example](references/use-examples-as-variable.feature)
- Always validate that new DB tables are on the list of DB table names being truncated in after hook (see
  `DbSteps.TABLE_TO_TRUNCATE`)
- Use corresponding `jpaRepository` to retrieve an entity or entities list.
  See [use-jpa-repository.md](references/use-jpa-repository.md)
- use `isEqualByComparingTo` to compare `BigDecimal` objects
- use `.isCloseTo(expectedTimestamp, within(1, ChronoUnit.SECONDS))` to compare timestamps
- use `SoftAssertations` to assert several fields of a single object
- use ` assertThat(sortedActual).zipSatisfy(expectedList, (actual, expected) -> assertSingle(actual, expected))` for
  asserting list of objects.
- use `Aliases`(`com.github.jenkaby.bikerental.componenttest.transformer.shared.Aliases`) utility class to hold short
  human-readable id of entities to use them on feature files and in the transformer and.
  See [alias-usage](references/alias-usage.md)
- use `DataTableHelper` convert standard objects from map in transformers
