Feature: Equipment management endpoints
  As an operator or admin
  I want to manage equipment information
  So that I can create, update, view and search equipment details

  Background:
    Given the database is empty for "equipments" table
    And the following equipment statues exist in the database
      | slug        | name        | description       |
      | BROKEN      | Broken      | Not Ready to rent |
      | AVAILABLE   | Available   | Ready to rent     |
      | MAINTENANCE | Maintenance | null              |
    And the following equipment types exist in the database
      | slug    | name    | description |
      | bicycle | Bicycle | Two-wheeled |
      | scooter | Scooter | Electric    |
    And the following equipment records exist in db
      | id | serialNumber | uid        | status    | type    | model   | commissionedAt | condition |
      | 1  | EQ-001       | BIKE-001   | AVAILABLE | bicycle | Model A |                | Good      |
      | 2  | EQ-002       | E-BIKE-001 | RENTED    | scooter | Model B |                | Excellent |
      | 3  | EQ-005       | BIKE-003   | AVAILABLE | bicycle | Model C |                | Fair      |
      | 4  | EQ-004       | BIKE-002   | BROKEN    | bicycle | Model C |                | Fair      |

  Scenario Outline: Get equipment by ID
    When a GET request has been made to "/api/equipments/{id}" endpoint with
      | {id} |
      | <id> |
    Then the response status is 200
    And the equipment response only contains
      | id   | serialNumber | uid      | status    | type    | model   | commissionedAt | condition |
      | <id> | EQ-001       | BIKE-001 | AVAILABLE | bicycle | Model A |                | Good      |
    Examples:
      | id |
      | 1  |

  Scenario Outline: Get equipment by non-existent field IDs
    When a GET request has been made to "/api/equipments/<partUrl>" endpoint
    Then the response status is 404
    And the response contains
      | path     | value         |
      | $.title  | Not Found     |
      | $.detail | <errorDetail> |
    Examples:
      | partUrl          | errorDetail                                  |
      | 999              | Equipment with identifier '999' not found    |
      | by-uid/uid       | Equipment with identifier 'uid' not found    |
      | by-serial/serial | Equipment with identifier 'serial' not found |

  Scenario: Search all equipment with default pagination
    When a GET request has been made to "/api/equipments" endpoint
    Then the response status is 200
    And the equipment response only contains list of
      | serialNumber | uid        | status    | type    | model   | commissionedAt | condition |
      | EQ-001       | BIKE-001   | AVAILABLE | bicycle | Model A |                | Good      |
      | EQ-002       | E-BIKE-001 | RENTED    | scooter | Model B |                | Excellent |
      | EQ-005       | BIKE-003   | AVAILABLE | bicycle | Model C |                | Fair      |
      | EQ-004       | BIKE-002   | BROKEN    | bicycle | Model C | 2026-01-30     | Fair      |

  Scenario: Search equipments by status and pagination
    When a GET request has been made to "/api/equipments" endpoint with query parameters
      | status    | page | size | sort             |
      | AVAILABLE | 0    | 2    | serialNumber,asc |
    Then the response status is 200
    And the equipment response only contains list of
      | serialNumber | uid      | status    | type    | model   | commissionedAt | condition |
      | EQ-001       | BIKE-001 | AVAILABLE | bicycle | Model A |                | Good      |
      | EQ-005       | BIKE-003 | AVAILABLE | bicycle | Model C |                | Fair      |

  Scenario: Search equipments by type
    When a GET request has been made to "/api/equipments" endpoint with query parameters
      | type    |
      | scooter |
    Then the response status is 200
    And the equipment response only contains list of
      | serialNumber | uid        | status | type    | model   | commissionedAt | condition |
      | EQ-002       | E-BIKE-001 | RENTED | scooter | Model B |                | Excellent |

  Scenario: Retrieve equipment by serial number
    When a GET request has been made to "/api/equipments/by-serial/{serialNumber}" endpoint with
      | {serialNumber} |
      | EQ-005         |
    Then the response status is 200
    And the equipment response only contains
      | serialNumber | uid      | status    | type    | model   | commissionedAt | condition |
      | EQ-005       | BIKE-003 | AVAILABLE | bicycle | Model C |                | Fair      |

  Scenario: Retrieve equipment by uid
    When a GET request has been made to "/api/equipments/by-uid/{uid}" endpoint with
      | {uid}      |
      | E-BIKE-001 |
    Then the response status is 200
    And the equipment response only contains
      | serialNumber | uid        | status | type    | model   | commissionedAt | condition |
      | EQ-002       | E-BIKE-001 | RENTED | scooter | Model B |                | Excellent |

  Scenario Outline: Create new equipment
    Given the equipment request is prepared with the following data
      | serialNumber   | uid   | type            | status   | model   | commissionedAt   | condition   |
      | <serialNumber> | <uid> | <equipmentType> | <status> | <model> | <commissionedAt> | <condition> |
    When a POST request has been made to "/api/equipments" endpoint
    Then the response status is 201
    And the equipment response contains
      | serialNumber   | uid   | type            | status   | model   | commissionedAt   | condition   |
      | <serialNumber> | <uid> | <equipmentType> | <status> | <model> | <commissionedAt> | <condition> |
    And the following equipment record was persisted in db
      | serialNumber   | uid   | status   | type            | model   | commissionedAt   | condition   |
      | <serialNumber> | <uid> | <status> | <equipmentType> | <model> | <commissionedAt> | <condition> |
    Examples:
      | serialNumber | uid          | equipmentType | status    | status    | model   | commissionedAt | condition |
      | EQ-999       | BIKE-999-NEW | bicycle       | AVAILABLE | AVAILABLE | Model X | 2026-01-15     | Excellent |

  Scenario Outline: Update existing equipment
    Given the equipment being updated is
      | serialNumber | uid          | type    | status | model   | commissionedAt | condition |
      | EQ-TO-UPDATE | EQ-TO-UPDATE | bicycle | RENTED | Model C | 2026-01-30     | New       |
    And the equipment request is prepared with the following data
      | serialNumber   | uid   | type            | status   | model   | commissionedAt   | condition   |
      | <serialNumber> | <uid> | <equipmentType> | <status> | <model> | <commissionedAt> | <condition> |
    When a PUT request has been made to "/api/equipments/{modifiedObjectId}" endpoint with context
    Then the response status is 200
    And the equipment response contains
      | serialNumber   | uid   | type            | status   | model   | commissionedAt   | condition   |
      | <serialNumber> | <uid> | <equipmentType> | <status> | <model> | <commissionedAt> | <condition> |
    And the following equipment record was persisted in db
      | serialNumber   | uid   | status   | type            | model   | commissionedAt   | condition   |
      | <serialNumber> | <uid> | <status> | <equipmentType> | <model> | <commissionedAt> | <condition> |
    Examples:
      | serialNumber | uid             | equipmentType | status | model     | commissionedAt | condition |
      | EQ-001-UPD   | BIKE-001-UPDATE | scooter       | BROKEN | Model A++ | 2026-01-20     | Fair      |

  Scenario Outline: Create new equipment when <violation> unique constraints are violated
    Given the equipment request is prepared with the following data
      | serialNumber   | uid   | type    | status    | model   | commissionedAt | condition |
      | <serialNumber> | <uid> | bicycle | AVAILABLE | Model X | 2026-01-15     | Excellent |
    When a POST request has been made to "/api/equipments" endpoint
    Then the response status is 409
    And the response contains
      | path     | value         |
      | $.title  | Conflict      |
      | $.detail | <errorDetail> |
    Examples:
      | serialNumber | uid      | violation    | errorDetail                                         |
      | unique       | BIKE-001 | uid          | Equipment with identifier 'BIKE-001' already exists |
      | EQ-001       | unique   | serialNumber | Equipment with identifier 'EQ-001' already exists   |

  Scenario: Update equipment with non-existent ID
    Given the equipment request is prepared with the following data
      | serialNumber | uid             | type    | status | model   | commissionedAt | condition |
      | EQ-999-UPD   | BIKE-999-UPDATE | scooter | BROKEN | Model X | 2026-01-20     | Fair      |
    When a PUT request has been made to "/api/equipments/{id}" endpoint with
      | {id} |
      | 999  |
    Then the response status is 404
    And the response contains
      | path     | value                                     |
      | $.title  | Not Found                                 |
      | $.detail | Equipment with identifier '999' not found |

