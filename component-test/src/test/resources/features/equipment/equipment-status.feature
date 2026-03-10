Feature: Equipment status endpoints
  As an operator or admin
  I want to manage and view equipment statuses
  So that I can track and update equipment availability

  Background:
    Given the following equipment statues exist in the database
      | slug        | name        | description      | transitions           |
      | AVAILABLE   | Available   | Ready for rental | MAINTENANCE           |
      | MAINTENANCE | Maintenance | null             | AVAILABLE             |
      | IN_USE      | In use      | null             | MAINTENANCE,AVAILABLE |

  Scenario: Retrieve all statuses
    When a GET request has been made to "/api/equipment-statuses" endpoint
    Then the response status is 200
    And the equipment status response only contains list of
      | slug        | name        | description      | transitions           |
      | AVAILABLE   | Available   | Ready for rental | MAINTENANCE           |
      | MAINTENANCE | Maintenance | null             | AVAILABLE             |
      | IN_USE      | In use      | null             | MAINTENANCE,AVAILABLE |

  Scenario Outline: Create a new status
    Given the equipment status request is prepared with the following data
      | slug   | name   | description   | transitions   |
      | <slug> | <name> | <description> | <transitions> |
    When a POST request has been made to "/api/equipment-statuses" endpoint
    Then the response status is 201
    And the equipment status response only contains
      | slug   | name   | description   | transitions   |
      | <slug> | <name> | <description> | <transitions> |
    And the following equipment status records was persisted in db
      | slug   | name   | description   | transitions   |
      | <slug> | <name> | <description> | <transitions> |
    Examples:
      | slug    | name    | description      | transitions           |
      | RETIRED | Retired | No longer in use | AVAILABLE,MAINTENANCE |

  Scenario Outline: Update status by slug
    Given the following equipment statues exist in the database
      | slug    | name    | description      | transitions |
      | RETIRED | Retired | No longer in use |             |
    And the equipment status request is prepared with the following data
      | name   | description   | transitions   |
      | <name> | <description> | <transitions> |
    When a PUT request has been made to "/api/equipment-statuses/{slug}" endpoint with
      | {slug} |
      | <slug> |
    Then the response status is 200
    And the equipment status response only contains
      | slug   | name   | description   | transitions   |
      | <slug> | <name> | <description> | <transitions> |
    And the following equipment status records was persisted in db
      | slug   | name   | description   | transitions   |
      | <slug> | <name> | <description> | <transitions> |
    Examples:
      | slug        | name             | description      | transitions |
      | MAINTENANCE | Very Maintenance | No longer in use | RETIRED     |


  Scenario: Update status by slug when no status exists
    Given the equipment status request is prepared with the following data
      | slug           | name |
      | DOES_NOT_EXIST | any  |
    When a PUT request has been made to "/api/equipment-statuses/DOES_NOT_EXIST" endpoint
    Then the response status is 404
    And the response contains
      | path     | value                                                      |
      | $.title  | Not Found                                                  |
      | $.detail | EquipmentStatus with identifier 'DOES_NOT_EXIST' not found |

  Scenario Outline: Update status by slug when no allowed transition exists
    Given the equipment status request is prepared with the following data
      | name   | description   | transitions    |
      | <name> | <description> | DOES_NOT_EXIST |
    When a PUT request has been made to "/api/equipment-statuses/{slug}" endpoint with
      | {slug} |
      | <slug> |
    Then the response status is 422
    And the response contains
      | path     | value                                                                 |
      | $.title  | Unprocessable Content                                                 |
      | $.detail | Referenced EquipmentStatus with identifier 'DOES_NOT_EXIST' not found |
    Examples:
      | slug        | name             | description      |
      | MAINTENANCE | Very Maintenance | No longer in use |
