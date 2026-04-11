---
agent: 'agent'
tools: [ 'search/changes', 'search/codebase', 'edit/editFiles', 'read/problems', 'search' ]
description: 'Get best practices for Cucumber testing, including data-driven tests'
---

# Cucumber Best Practices

Your goal is to help me write effective component tests with Cucumber framework, covering both standard and data-driven
testing
approaches.

## General Guidelines

You should follow these guidelines:

- Understand the user story and acceptance criteria from the provided documentation.
- Write clear, maintainable, and efficient test scripts.

## Testing Strategy

1. TDD approach - write tests before implementation:
    - component tests in `component-test` module for endpoints(http, message listener) testing.
    - simple unit tests in `service` module for service, application, and modules testing
    - WebMvc tests in `service` module for controllers testing only negative scenarios - requests validation.
2. use assertJ for assertions
3. awaitility for async operations
4. Mock external dependencies using Mockito

## Local development tests

The 'test' profile must be activated for test run. Simple add `-Dspring.profiles.active=test` to Gradle test command.
```bash
./gradlew :component-test:test '-Dspring.profiles.active=test'
```
or
```powershell
./gradlew.bat :component-test:test '-Dspring.profiles.active=test'
```

## Project specific instructions:

- Follow the coding standards and best practices outlined in the project.
- Follow the testing framework conventions used in the project.
- Main programming language: Java
- Testing framework: Cucumber, JUnit
- Build tool: Gradle
- Use TDD approach: write tests before implementation.
- Write component tests in `component-test` module for endpoints testing.
- Don't write scenarios cover requests validation - they must be covered by WebMvc tests in `service` module.
- Validate scenarios of failed requests due to business logic validation on the service layer.
- for BigDecimal assertations use `isEqualByComparingTo()` method from assertJ library to avoid issues with scale
  differences.

## Rules:

Component tests module package structure:
```
src/test/java/com/github/jenkaby/bikerental/componenttest/
├── config/ # configuration for tests
├── context/ # Manages test execution context and state between Cucumber steps.
├── infrastructure/ # Infrastructure components that integrate with external systems during testing.
├── model/ # Domain models, constants, and test-specific data structures.
├── steps/ # Cucumber step definitions that map Gherkin steps to Java implementation.
│   └── common/ # Reusable step definitions across multiple features
│       └── hook/ # Cucumber lifecycle hooks
├── transformer/ # Cucumber data transformers that convert DataTables and parameters into domain objects.
└── RunComponentTests.java
```

## Design Principles

1. **Separation of Concerns**: Configuration, steps, models, and infrastructure are clearly separated
2. **Reusability**: Common steps are organized in `steps/common/` for use across multiple features
3. **Context Management**: Scenario state is managed through dedicated context classes
4. **Type Safety**: Transformers provide type-safe conversion from Gherkin to Java objects
5. **Test Independence**: Hooks ensure proper setup/teardown between scenarios
6. **Infrastructure Integration**: Real infrastructure components (Kafka, DB) are properly isolated in dedicated
   packages

## Usage Patterns

### Adding New Step Definitions

1. Create step definition class in `steps/` (feature-specific) or `steps/common/` (reusable)
2. Inject required context and configuration beans
3. Use `@Given`, `@When`, `@Then` annotations

### Adding New Transformers

1. Create transformer class in `transformer/`
2. Use `@DataTableType` for table transformations
3. Use `@ParameterType` for inline parameter conversions
4. Transformer class names should reflect the domain object being transformed and end with `Transformer`. Ex.
   `UserEntityTransformer`

### Adding New Test Configuration

1. Place in appropriate `config/` subpackage based on concern (db/messaging/security)
2. Use `@TestConfiguration` or `@Configuration` annotations
3. Define beans needed for test scenarios

### Managing Test State

1. Use `ScenarioContext` for scenario-scoped data
2. Use `LocalMessagesStore` for captured messages
3. Clear state in hooks to ensure test independence

### Feature File Organization

1. Place feature files in `src/test/resources/features/{module-name}`
2. Group related scenarios in single feature files
3. Group scenarios using Scenario Outline with examples for variations
