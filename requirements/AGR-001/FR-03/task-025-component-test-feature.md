<task_file_template>

# Task 025: Create the agreement-template component test feature

> **Applied Skill:** `spring-boot-java-cucumber` — happy paths + business-rule failures only (request
> validation lives in WebMvc tests, Task 026/027); features under
> `component-test/src/test/resources/features/{module}/`; reuse generic steps in `steps/common`
> (`a prepared payload is`, `a {httpMethod} request has been made to {string} endpoint`,
> `the response status is`, `the response contains`, `... endpoint with context`). `component-tests`
> rule — AssertJ via the generic assertions. Uses the seeding + id-extraction steps from Task 024.

## 1. Objective

Cover fr.md scenarios 1–6: create+activate first version, activation archives the previous active,
edit/delete/activate on a non-draft rejected (409), and no-active → 404.

## 2. File to Modify / Create

* **File Path:** `component-test/src/test/resources/features/agreement/agreement-template.feature`
* **Action:** Create New File

## 3. Code Implementation

Create the file with EXACTLY this content:

```gherkin
Feature: Agreement Template Management
  As an administrator
  I want to create, edit, activate and delete versions of the rental agreement text
  So that exactly one agreed-upon agreement version is active at any time

  Background:
    Given the request header "Content-Type" is "application/json"
    And the request header "Accept" is "application/json"

  Scenario: Create and activate the first template
    Given a prepared payload is
    """
    {"title": "Rental Agreement v1", "content": "You agree to return the equipment on time."}
    """
    When a POST request has been made to "/api/agreements" endpoint
    Then the response status is 201
    And the response contains
      | path            | value                                     |
      | $.title         | Rental Agreement v1                       |
      | $.status        | DRAFT                                     |
      | $.versionNumber | null                                      |
    And the created agreement template id is stored as 'requestedObjectId'
    When a PATCH request has been made to "/api/agreements/{requestedObjectId}/activate" endpoint with context
    Then the response status is 200
    And the response contains
      | path            | value  |
      | $.status        | ACTIVE |
      | $.versionNumber | 1      |
    When a GET request has been made to "/api/agreements/active" endpoint
    Then the response status is 200
    And the response contains
      | path            | value                                     |
      | $.status        | ACTIVE                                    |
      | $.versionNumber | 1                                         |
      | $.content       | You agree to return the equipment on time.|

  Scenario: Activation archives the previous active version
    Given agreement templates exist in the database with the following data
      | id | versionNumber | title      | content         | contentSha256                                                    | status | createdAt           | updatedAt           | activatedAt         |
      | 1  | 1             | Version 1  | Old content     | 0000000000000000000000000000000000000000000000000000000000000000 | ACTIVE | 2026-01-01T09:00:00 | 2026-01-01T09:00:00 | 2026-01-01T09:00:00 |
      | 2  |               | Version 2  | New content     |                                                                  | DRAFT  | 2026-02-01T09:00:00 | 2026-02-01T09:00:00 |                     |
    When a PATCH request has been made to "/api/agreements/2/activate" endpoint
    Then the response status is 200
    And the response contains
      | path            | value  |
      | $.status        | ACTIVE |
      | $.versionNumber | 2      |
    When a GET request has been made to "/api/agreements/1" endpoint
    Then the response status is 200
    And the response contains
      | path      | value       |
      | $.status  | DEACTIVATED |

  Scenario: Editing a non-draft template is rejected
    Given a single agreement template exists in the database with the following data
      | id | versionNumber | title     | content     | contentSha256                                                    | status | createdAt           | updatedAt           | activatedAt         |
      | 1  | 1             | Version 1 | Old content | 0000000000000000000000000000000000000000000000000000000000000000 | ACTIVE | 2026-01-01T09:00:00 | 2026-01-01T09:00:00 | 2026-01-01T09:00:00 |
    And a prepared payload is
    """
    {"title": "Edited title", "content": "Edited content"}
    """
    When a PATCH request has been made to "/api/agreements/{requestedObjectId}" endpoint with context
    Then the response status is 409
    And the response contains
      | path        | value                           |
      | $.errorCode | agreement.template.not_editable |

  Scenario: Deleting a non-draft template is rejected
    Given a single agreement template exists in the database with the following data
      | id | versionNumber | title     | content     | contentSha256                                                    | status | createdAt           | updatedAt           | activatedAt         |
      | 1  | 1             | Version 1 | Old content | 0000000000000000000000000000000000000000000000000000000000000000 | ACTIVE | 2026-01-01T09:00:00 | 2026-01-01T09:00:00 | 2026-01-01T09:00:00 |
    When a DELETE request has been made to "/api/agreements/{requestedObjectId}" endpoint with context
    Then the response status is 409
    And the response contains
      | path        | value                            |
      | $.errorCode | agreement.template.not_deletable |

  Scenario: Activating a non-draft template is rejected
    Given a single agreement template exists in the database with the following data
      | id | versionNumber | title     | content         | contentSha256                                                    | status      | createdAt           | updatedAt           | activatedAt         | deactivatedAt       |
      | 1  | 1             | Version 1 | Archived text   | 0000000000000000000000000000000000000000000000000000000000000000 | DEACTIVATED | 2026-01-01T09:00:00 | 2026-01-02T09:00:00 | 2026-01-01T09:00:00 | 2026-01-02T09:00:00 |
    When a PATCH request has been made to "/api/agreements/{requestedObjectId}/activate" endpoint with context
    Then the response status is 409
    And the response contains
      | path        | value                              |
      | $.errorCode | agreement.template.not_activatable |

  Scenario: No active template returns 404
    Given a single agreement template exists in the database with the following data
      | id | versionNumber | title     | content     | status | createdAt           | updatedAt           |
      | 1  |               | Draft one | Draft text  | DRAFT  | 2026-01-01T09:00:00 | 2026-01-01T09:00:00 |
    When a GET request has been made to "/api/agreements/active" endpoint
    Then the response status is 404
    And the response contains
      | path        | value                        |
      | $.errorCode | agreement.template.no_active |
```

## 4. Validation Steps

Assume the DB is already up. Run ONLY the agreement feature. Do NOT run `bootRun`.

```bash
./gradlew :component-test:test "-Dspring.profiles.active=test"
```

</task_file_template>
