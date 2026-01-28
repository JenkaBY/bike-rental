# Project Context and Architecture

SYSTEM_CONTEXT: |
You are a senior developer working on a **Spring Boot** project.

Required file reads on startup:

- docs/backend-architecture.md: The project architecture
- docs/single-module-details-v1.md: The module implementation details
- docs/tasks/user-stories.md: Project progress and state

Before making any changes:

1. Parse and understand system architecture from docs/backend-architecture.md
2. Check current task context from tasks/user-stories.md
3. Update progress in docs/status.md
4. Follow technical specifications from docs/technical-details.md

# Code Style and Patterns

JAVA_GUIDELINES: |

- Follow SOLID principles
- Write unit tests for all public methods
- Never comment your code; use descriptive names instead
- Use dependency injection for all dependencies
- Use record instead of class where it's possible

# Architecture Understanding

READ_ARCHITECTURE: |
File: docs/backend-architecture.md
Required parsing:

1. Extract and understand:
    - Module boundaries and relationships
    - Data flow patterns
    - System interfaces
    - Component dependencies
2. Validate any changes against architectural constraints
3. Ensure new code maintains defined separation of concerns

Error handling:

1. If file not found: STOP and notify user
2. If diagram parse fails: REQUEST clarification
3. If architectural violation detected: WARN user

# Task Management

TASK_WORKFLOW: |
Required files:

- docs/tasks/user-stories.md: Source of task list, Progress tracking and status legend. Task details is linked there.
- docs/technical.md: Implementation guidelines

Workflow steps:

1. READ tasks/tasks/user-stories.md:
   - Take a first user store(US) with the 'In-Progress' status. Drill down to its details:
      - Check whether the US has been decomposed. If no, plan your work and decompose the US, create subtasks and save
        them in the US details.
      - Take first 'In-Progress' sub task. Validate its implementation and proceed working on it if it is not completed
        yet.
      - If no sub tasks with "In-Progress" state, take the next 'New' sub task. Change its status to 'In-Progress' and
        proceed working on it.
      - After the subtask is completed, mark it as 'Done'.
      - If no 'In-Progress' sub tasks left, mark the US as 'Done'
      - Stop working on it.
   - if no 'In-Progress' US found:
      - take the first 'New' US.
      - Change its status to the 'In-Progress'
      - Follow the link in the user story row to read the details of the user story

2. UPDATE docs/tasks/user-stories.md:
    - Plan your work and decompose the user story. Update the attached file with subtasks. Follow the same approach as
      it's done for user stories with 'In-Progress' status.
    - Mark task as In-Progress
    - Track completion of sub-tasks in the appropriate file
    - Document any blockers

3. IMPLEMENT following TDD:
    - Create test files first in the `component-test` gradle module
    - Implement to pass tests in the `service` gradle module
    - Update status on test completion

4. Validation and compliance:
   - ensure code adheres to project architecture
   - validate the package structure, naming conventions, and coding standards adhere the guidelines specified
     in [this example](docs/single-module-details-v1.md)

# Testing Strategy

1. TDD approach - write tests before implementation:
   - component tests in `component-test` module for endpoints testing only positive scenarios.
   - simple unit tests in `service` module for service, application, and modules testing
   - WebMvc tests in `service` module for controllers testing only negative scenarios - requests validation.
2. use assertJ for assertions
3. awaitility for async operations
4. Mock external dependencies using Mockito
5. To run test the 'test' profile must be activated. Simple add `-Dspring.profiles.active=test` to Gradle test command.

COMPONENT_TESTING: |

## Rules:

Component tests module package structure:

```
src/test/java/com/github/jenkaby/
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

# Error Prevention

VALIDATION_RULES: |

1. Verify type consistency
2. Check for potential null/undefined
3. Validate against business rules
4. Ensure error handling