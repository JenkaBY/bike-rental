Feature: Equipment type endpoints
  As an operator or admin
  I want to manage and view equipment types
  So that I can categorize and manage equipment

  Background:
    Given the database is empty for "equipment-types" table
    And the following equipment types exist in the database
      | slug    | name    | description |
      | bicycle | Bicycle | Two-wheeled |
      | scooter | Scooter | Electric    |

  Scenario: Retrieve all types
    When a GET request has been made to "/api/equipment-types" endpoint
    Then the response status is 200
    And the 'equipment type' response only contains
      | slug    | name    | description |
      | bicycle | Bicycle | Two-wheeled |
      | scooter | Scooter | Electric    |

  Scenario Outline: Create a new type
    Given the 'equipment type' request is prepared with the following data
      | slug   | name   | description   |
      | <slug> | <name> | <description> |
    When a POST request has been made to "/api/equipment-types" endpoint
    Then the response status is 201
    And the 'equipment type' response only contains
      | slug   | name   | description   |
      | <slug> | <name> | <description> |
    Examples:
      | slug    | name    | description        |
      | trailer | Trailer | Tow-behind trailer |

  Scenario Outline: Update type by slug
    Given the 'equipment type' request is prepared with the following data
      | slug   | name          | description          |
      | <slug> | <initialName> | <initialDescription> |
    When a POST request has been made to "/api/equipment-types/{slug}" endpoint with
      | {slug} |
      | <slug> |
    Then the response status is 200
    And the 'equipment type' response only contains
      | slug   | name   | description   |
      | <slug> | <name> | <description> |
    Examples:
      | slug    | initialName | initialDescription | slug    | name        | description        |
      | trailer | Trailer     | Can tow loads      | trailer | Big Trailer | Heavy-duty trailer |

  Scenario: Update equipment type by slug when no status exists
    Given the 'equipment status' request is prepared with the following data
      | slug       | name |
      | NOT_EXISTS | any  |
    When a POST request has been made to "/api/equipment-types/NOT_EXISTS" endpoint
    Then the response status is 404
    And the response contains
      | path     | value     |
      | $.title  | Not found |
      | $.detail | Not found |