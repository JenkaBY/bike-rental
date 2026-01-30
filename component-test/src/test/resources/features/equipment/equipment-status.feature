Feature: Equipment status endpoints
  As an operator or admin
  I want to manage and view equipment statuses
  So that I can track and update equipment availability

  Background:
    Given the database is empty for "equipment-statuses" table
    And the following equipment statues exist in the database
      | slug        | name        | description   |
      | AVAILABLE   | Available   | Ready to rent |
      | MAINTENANCE | Maintenance | null          |

  Scenario: Retrieve all statuses
    When a GET request has been made to "/api/equipment-statuses" endpoint
    Then the response status is 200
    And the 'equipment status' response only contains
      | slug        | name        | description   |
      | AVAILABLE   | Available   | Ready to rent |
      | MAINTENANCE | Maintenance | null          |

  Scenario Outline: Create a new status
    Given the 'equipment status' request is prepared with the following data
      | slug   | name   | description   |
      | <slug> | <name> | <description> |
    When a POST request has been made to "/api/equipment-statuses" endpoint
    Then the response status is 201
    And the 'equipment status' response only contains
      | slug   | name   | description   |
      | <slug> | <name> | <description> |
    Examples:
      | slug    | name    | description      |
      | RETIRED | Retired | No longer in use |

  Scenario Outline: Update status by slug
    Given the 'equipment status' request is prepared with the following data
      | slug   | name          | description          |
      | <slug> | <initialName> | <initialDescription> |
    When a POST request has been made to "/api/equipment-statuses/{slug}" endpoint with
      | {slug} |
      | <slug> |
    Then the response status is 200
    And the 'equipment status' response only contains
      | slug   | name   | description   |
      | <slug> | <name> | <description> |
    Examples:
      | slug    | initialName | initialDescription | slug    | name         | description      |
      | RETIRED | Retired     | Can use a bit      | RETIRED | Very Retired | No longer in use |


  Scenario: Update status by slug when no status exists
    Given the 'equipment status' request is prepared with the following data
      | slug       | name |
      | NOT_EXISTS | any  |
    When a POST request has been made to "/api/equipment-statuses/NOT_EXISTS" endpoint
    Then the response status is 404
    And the response contains
      | path     | value     |
      | $.title  | Not found |
      | $.detail | Not found |
