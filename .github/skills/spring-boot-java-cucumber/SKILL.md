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
│   │   └── {feature}/                        # Feature-specific steps
│   │       └── {Feature}WebSteps.java        # Feature-specific web related steps
│   │       └── {Feature}DbSteps.java         # Feature-specific db related steps
│   │       └── {Feature}MessagingSteps.java  # Feature-specific messaging related steps
│   └── transformer/                          # Data transformers(Parameter transformers, )
│       └── ParameterTypes.java               # Custom parameter types
│       └── {ModelOrEntity}Transformer.java   # Datatable transformers. Converts DataTable to domain objects
└── src/test/resources/       
    ├── features/                             # Gherkin feature files
    │   ├── application.feature               # Basic app tests
    │   └── {module}/                         # Organized by module
    │       └── feature-name.feature          # Feature scenarios
    ├── application-test.yaml                 # Test profile configuration
    └── junit-platform.properties             # Cucumber configuration
```

## Gherkin Best Practices

- Use `Feature:` to describe the user story. Keep it short but descriptive. Don't use a ticket or user story reference
- Use `Background:` for common setup across scenarios
- Prefer to use `Scenario Outline:` with `Examples:` for data-driven tests.
  See [the example](./examples/data-driven-scenario.feature)
- Use descriptive scenario names that explain the expected outcome
- Prefer datatables in horizontal format over to passing JSON in steps.
  See [the example](./examples/datatable-transformers.md)
- If datatable becomes too large, split object creation into several steps and save intermediate state in scenario
  context. See [the example](./examples/datatable-transformers.md)
- Use `Scenario Outline:` with `Examples:` for data repeated across multiple steps.
  See [the example](./examples/use-examples-as-variable.feature)

## Naming Conventions

- Create domain-specific steps for your features
- Organize step definitions by feature AND infrastructure layer. Split steps into separate classes based on the
  infrastructure they interact with. The pattern is '{ModelOrEntity}{Infra}Steps' E.g. `UserWebSteps`, `UserDbSteps`,
  `UserMessagingSteps`
- Transformer names should follow the pattern '{ModelOrEntity}Transformer'. E.g. `UserTransformer`, `OrderTransformer`,
  `UserCreateRequestTransformer`

## Testing Strategy

### Component Tests

- Test happy paths
- Don't cover requests validation
- Verify integration between components
- Verify final state of components (e.g. DB state, Kafka topics, queues and so on) by reading from real infrastructure
- Clear state between scenarios
- Use scenario context to share state between steps
- Mock external dependencies (e.g. third-party services)
- Test against real infrastructure (DB, messaging and so on)
- Focus on business scenarios from user stories

## Best Practices

1. **Isolation**: Each scenario should be independent
2. **Reusability**: Keep common steps (like common Db steps, performing http requests, sending messages and so) in
   `steps/common/`
3. **Performance**: Use Background for expensive setup or repeated steps
4. **Debugging**: Log request/response in hooks for failed scenarios
5. **Organization**: Group features by domain module
6. **Naming**: Use descriptive scenario names tied to user stories
7. **Assertions**: Use AssertJ for fluent, readable assertions
8. **Context**: Keep scenario context clean and minimal
9. **Don't repeat yourself**: Don't duplicate already existing steps. Try to reuse them, refactor existing ones to be
   reusable
10. **No comments**: Avoid comments in the code and Gherkin files

