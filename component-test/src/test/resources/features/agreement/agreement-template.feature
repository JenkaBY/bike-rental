Feature: Agreement Template Management
  As an administrator
  I want to create, edit, activate and delete versions of the rental agreement text
  So that exactly one agreed-upon agreement version is active at any time

  Background:
    Given the request header "Content-Type" is "application/json"
    And the request header "Accept" is "application/json"

  Scenario: Create and activate the first template
    Given the agreement template request is
      | title               | content                                    |
      | Rental Agreement v1 | You agree to return the equipment on time. |
    When a POST request has been made to "/api/agreements" endpoint
    Then the response status is 201
    And the agreement template response contains
      | title               | status |
      | Rental Agreement v1 | DRAFT  |
    When a PATCH request has been made to "/api/agreements/{requestedObjectId}/activate" endpoint with context
    Then the response status is 200
    And the agreement template response contains
      | status | versionNumber |
      | ACTIVE | 1             |
    When a GET request has been made to "/api/agreements/active" endpoint
    Then the response status is 200
    And the agreement template response contains
      | status | versionNumber | content                                    |
      | ACTIVE | 1             | You agree to return the equipment on time. |

  Scenario: Activation archives the previous active version
    Given agreement templates exist in the database with the following data
      | id | versionNumber | title     | content     | contentSha256 | status | createdAt           | updatedAt           | activatedAt         |
      | 1  | 1             | Version 1 | Old content | SHA_ZERO      | ACTIVE | 2026-01-01T09:00:00 | 2026-01-01T09:00:00 | 2026-01-01T09:00:00 |
      | 2  |               | Version 2 | New content |               | DRAFT  | 2026-02-01T09:00:00 | 2026-02-01T09:00:00 |                     |
    When a PATCH request has been made to "/api/agreements/2/activate" endpoint
    Then the response status is 200
    And the agreement template response contains
      | status | versionNumber |
      | ACTIVE | 2             |
    When a GET request has been made to "/api/agreements/1" endpoint
    Then the response status is 200
    And the agreement template response contains
      | status      |
      | DEACTIVATED |

  Scenario: Editing a non-draft template is rejected
    Given a single agreement template exists in the database with the following data
      | id | versionNumber | title     | content     | contentSha256 | status | createdAt           | updatedAt           | activatedAt         |
      | 1  | 1             | Version 1 | Old content | SHA_ZERO      | ACTIVE | 2026-01-01T09:00:00 | 2026-01-01T09:00:00 | 2026-01-01T09:00:00 |
    And the agreement template request is
      | title        | content        |
      | Edited title | Edited content |
    When a PATCH request has been made to "/api/agreements/{requestedObjectId}" endpoint with context
    Then the response status is 409
    And the response contains
      | path        | value                           |
      | $.errorCode | agreement.template.not_editable |

  Scenario: Deleting a non-draft template is rejected
    Given a single agreement template exists in the database with the following data
      | id | versionNumber | title     | content     | contentSha256 | status | createdAt           | updatedAt           | activatedAt         |
      | 1  | 1             | Version 1 | Old content | SHA_ZERO      | ACTIVE | 2026-01-01T09:00:00 | 2026-01-01T09:00:00 | 2026-01-01T09:00:00 |
    When a DELETE request has been made to "/api/agreements/{requestedObjectId}" endpoint with context
    Then the response status is 409
    And the response contains
      | path        | value                            |
      | $.errorCode | agreement.template.not_deletable |

  Scenario: Activating a non-draft template is rejected
    Given a single agreement template exists in the database with the following data
      | id | versionNumber | title     | content       | contentSha256 | status      | createdAt           | updatedAt           | activatedAt         | deactivatedAt       |
      | 1  | 1             | Version 1 | Archived text | SHA_ZERO      | DEACTIVATED | 2026-01-01T09:00:00 | 2026-01-02T09:00:00 | 2026-01-01T09:00:00 | 2026-01-02T09:00:00 |
    When a PATCH request has been made to "/api/agreements/{requestedObjectId}/activate" endpoint with context
    Then the response status is 409
    And the response contains
      | path        | value                              |
      | $.errorCode | agreement.template.not_activatable |

  Scenario: No active template returns 404
    Given a single agreement template exists in the database with the following data
      | id | versionNumber | title     | content    | status | createdAt           | updatedAt           |
      | 1  |               | Draft one | Draft text | DRAFT  | 2026-01-01T09:00:00 | 2026-01-01T09:00:00 |
    When a GET request has been made to "/api/agreements/active" endpoint
    Then the response status is 404
    And the response contains
      | path        | value                        |
      | $.errorCode | agreement.template.no_active |
