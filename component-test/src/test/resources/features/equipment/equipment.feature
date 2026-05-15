Feature: Equipment management endpoints
  As an operator or admin
  I want to manage equipment information
  So that I can create, update, view and search equipment details

  Background:
    Given the following equipment statues exist in the database
      | slug        | name        | description       | transitions               |
      | BROKEN      | Broken      | Not Ready to rent | AVAILABLE,MAINTENANCE     |
      | AVAILABLE   | Available   | Ready to rent     | BROKEN,MAINTENANCE,RENTED |
      | MAINTENANCE | Maintenance | null              | AVAILABLE                 |
      | RENTED      | Rented      | In use already    | AVAILABLE,BROKEN          |
    And the following equipment types exist in the database
      | slug    | name    | description |
      | BICYCLE | Bicycle | Two-wheeled |
      | SCOOTER | Scooter | Electric    |
    And the following equipment records exist in db
      | id | serialNumber | uid        | status    | type    | model      | commissionedAt | conditionNotes | condition |
      | 1  | EQ-001       | BIKE-001   | AVAILABLE | BICYCLE | Model A    |                | Good           | GOOD      |
      | 2  | EQ-002       | E-BIKE-001 | RENTED    | SCOOTER | Model B    |                | Excellent      | GOOD      |
      | 3  | EQ-005       | BIKE-003   | AVAILABLE | BICYCLE | Model C    |                | Fair           | GOOD      |
      | 4  | EQ-004       | BIKE-002   | BROKEN    | BICYCLE | Model C    | 2026-01-30     | Fair           | BROKEN    |
      | 5  | EQ-0066      | BIKE-00-   | AVAILABLE | BICYCLE | Model 1    |                | Good           | GOOD      |
      | 6  | EQ-007       | BIKE-0066  | AVAILABLE | BICYCLE | Model 2    |                | Good           | GOOD      |
      | 7  | EQ-009       | BIKE-009   | RENTED    | BICYCLE | Model 0066 |                | Good           | GOOD      |

  Scenario Outline: Get equipment by ID
    When a GET request has been made to "/api/equipments/{id}" endpoint with
      | {id} |
      | <id> |
    Then the response status is 200
    And the equipment response only contains
      | id   | serialNumber | uid      | status    | type    | model   | commissionedAt | conditionNotes | condition |
      | <id> | EQ-001       | BIKE-001 | AVAILABLE | BICYCLE | Model A |                | Good           | GOOD      |
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
      | serialNumber | uid        | status    | type    | model      | commissionedAt | conditionNotes | condition |
      | EQ-001       | BIKE-001   | AVAILABLE | BICYCLE | Model A    |                | Good           | GOOD      |
      | EQ-002       | E-BIKE-001 | RENTED    | SCOOTER | Model B    |                | Excellent      | GOOD      |
      | EQ-005       | BIKE-003   | AVAILABLE | BICYCLE | Model C    |                | Fair           | GOOD      |
      | EQ-004       | BIKE-002   | BROKEN    | BICYCLE | Model C    | 2026-01-30     | Fair           | BROKEN    |
      | EQ-0066      | BIKE-00-   | AVAILABLE | BICYCLE | Model 1    |                | Good           | GOOD      |
      | EQ-007       | BIKE-0066  | AVAILABLE | BICYCLE | Model 2    |                | Good           | GOOD      |
      | EQ-009       | BIKE-009   | RENTED    | BICYCLE | Model 0066 |                | Good           | GOOD      |

  Scenario: Search equipments by status and pagination
    When a GET request has been made to "/api/equipments" endpoint with query parameters
      | status    | page | size | sort             |
      | AVAILABLE | 0    | 2    | serialNumber,asc |
    Then the response status is 200
    And the equipment response only contains list of
      | serialNumber | uid      | status    | type    | model   | commissionedAt | conditionNotes | condition |
      | EQ-001       | BIKE-001 | AVAILABLE | BICYCLE | Model A |                | Good           | GOOD      |
      | EQ-005       | BIKE-003 | AVAILABLE | BICYCLE | Model C |                | Fair           | GOOD      |

  Scenario: Search equipments by search text in serial number, uid and model
    When a GET request has been made to "/api/equipments" endpoint with query parameters
      | q    |
      | 0066 |
    Then the response status is 200
    And the equipment response only contains list of
      | serialNumber | uid       | status    | type    | model      | commissionedAt | conditionNotes | condition |
      | EQ-0066      | BIKE-00-  | AVAILABLE | BICYCLE | Model 1    |                | Good           | GOOD      |
      | EQ-007       | BIKE-0066 | AVAILABLE | BICYCLE | Model 2    |                | Good           | GOOD      |
      | EQ-009       | BIKE-009  | RENTED    | BICYCLE | Model 0066 |                | Good           | GOOD      |

  Scenario: Search equipments by status and search text in serial number, uid and model
    When a GET request has been made to "/api/equipments" endpoint with query parameters
      | q    | status    |
      | 0066 | AVAILABLE |
    Then the response status is 200
    And the equipment response only contains list of
      | serialNumber | uid       | status    | type    | model   | commissionedAt | conditionNotes | condition |
      | EQ-0066      | BIKE-00-  | AVAILABLE | BICYCLE | Model 1 |                | Good           | GOOD      |
      | EQ-007       | BIKE-0066 | AVAILABLE | BICYCLE | Model 2 |                | Good           | GOOD      |

  Scenario: Search equipments by type
    When a GET request has been made to "/api/equipments" endpoint with query parameters
      | type    |
      | SCOOTER |
    Then the response status is 200
    And the equipment response only contains list of
      | serialNumber | uid        | status | type    | model   | commissionedAt | conditionNotes | condition |
      | EQ-002       | E-BIKE-001 | RENTED | SCOOTER | Model B |                | Excellent      | GOOD      |

  Scenario: Retrieve equipment by serial number
    When a GET request has been made to "/api/equipments/by-serial/{serialNumber}" endpoint with
      | {serialNumber} |
      | EQ-005         |
    Then the response status is 200
    And the equipment response only contains
      | serialNumber | uid      | status    | type    | model   | commissionedAt | conditionNotes | condition |
      | EQ-005       | BIKE-003 | AVAILABLE | BICYCLE | Model C |                | Fair           | GOOD      |

  Scenario: Retrieve equipment by uid
    When a GET request has been made to "/api/equipments/by-uid/{uid}" endpoint with
      | {uid}      |
      | E-BIKE-001 |
    Then the response status is 200
    And the equipment response only contains
      | serialNumber | uid        | status | type    | model   | commissionedAt | conditionNotes | condition |
      | EQ-002       | E-BIKE-001 | RENTED | SCOOTER | Model B |                | Excellent      | GOOD      |

  Scenario Outline: Create new equipment
    Given the equipment request is prepared with the following data
      | serialNumber   | uid   | type            | status   | model   | commissionedAt   | conditionNotes   | condition   |
      | <serialNumber> | <uid> | <equipmentType> | <status> | <model> | <commissionedAt> | <conditionNotes> | <condition> |
    When a POST request has been made to "/api/equipments" endpoint
    Then the response status is 201
    And the equipment response contains
      | serialNumber   | uid   | type            | status   | model   | commissionedAt   | conditionNotes   | condition   |
      | <serialNumber> | <uid> | <equipmentType> | <status> | <model> | <commissionedAt> | <conditionNotes> | <condition> |
    And the following equipment record was persisted in db
      | serialNumber   | uid   | status   | type            | model   | commissionedAt   | conditionNotes   | condition   |
      | <serialNumber> | <uid> | <status> | <equipmentType> | <model> | <commissionedAt> | <conditionNotes> | <condition> |
    Examples:
      | serialNumber | uid          | equipmentType | status      | model   | commissionedAt | conditionNotes | condition |
      | EQ-999       | BIKE-999-NEW | BICYCLE       | MAINTENANCE | Model X | 2026-01-15     | Excellent      | BROKEN    |

  Scenario Outline: Create equipments the same serialNumber
    Given the equipment request is prepared with the following data
      | serialNumber   | uid      | type    | status    | model   | commissionedAt | condition |
      | <serialNumber> | unique-1 | BICYCLE | AVAILABLE | Model X | 2026-01-15     | GOOD      |
    When a POST request has been made to "/api/equipments" endpoint
    Then the response status is 201
    Given the equipment request is prepared with the following data
      | serialNumber   | uid      | type    | status    | model   | commissionedAt | condition |
      | <serialNumber> | unique-2 | BICYCLE | AVAILABLE | Model X | 2026-01-15     | GOOD      |
    When a POST request has been made to "/api/equipments" endpoint
    Then the response status is 201
    Examples:
      | serialNumber |
      | EQ-001       |

  Scenario Outline: Update existing equipment
    Given the equipment being updated is
      | serialNumber | uid          | type    | status | model   | commissionedAt | conditionNotes | condition   |
      | EQ-TO-UPDATE | EQ-TO-UPDATE | BICYCLE | RENTED | Model C | 2026-01-30     | New            | <condition> |
    And the equipment request is prepared with the following data
      | serialNumber   | uid   | type            | status   | model   | commissionedAt   | conditionNotes   | condition   |
      | <serialNumber> | <uid> | <equipmentType> | <status> | <model> | <commissionedAt> | <conditionNotes> | <condition> |
    When a PUT request has been made to "/api/equipments/{requestedObjectId}" endpoint with context
    Then the response status is 200
    And the equipment response contains
      | serialNumber   | uid   | type            | status   | model   | commissionedAt   | conditionNotes   | condition   |
      | <serialNumber> | <uid> | <equipmentType> | <status> | <model> | <commissionedAt> | <conditionNotes> | <condition> |
    And the following equipment record was persisted in db
      | serialNumber   | uid   | status   | type            | model   | commissionedAt   | conditionNotes   | condition   |
      | <serialNumber> | <uid> | <status> | <equipmentType> | <model> | <commissionedAt> | <conditionNotes> | <condition> |
    Examples:
      | serialNumber | uid             | equipmentType | status | model     | commissionedAt | conditionNotes | condition   |
      | EQ-001-UPD   | BIKE-001-UPDATE | SCOOTER       | BROKEN | Model A++ | 2026-01-20     | Fair           | MAINTENANCE |

  Scenario Outline: Update equipment with the existing serialNumber
    Given the equipment being updated is
      | serialNumber | uid          | type    | status | model   | commissionedAt | conditionNotes | condition   |
      | EQ-TO-UPDATE | EQ-TO-UPDATE | BICYCLE | RENTED | Model C | 2026-01-30     | New            | <condition> |
    And the equipment request is prepared with the following data
      | serialNumber   | uid   | type            | status   | model   | commissionedAt   | conditionNotes   | condition   |
      | <serialNumber> | <uid> | <equipmentType> | <status> | <model> | <commissionedAt> | <conditionNotes> | <condition> |
    When a PUT request has been made to "/api/equipments/{requestedObjectId}" endpoint with context
    Then the response status is 200
    And the equipment response contains
      | serialNumber   | uid   | type            | status   | model   | commissionedAt   | conditionNotes   | condition   |
      | <serialNumber> | <uid> | <equipmentType> | <status> | <model> | <commissionedAt> | <conditionNotes> | <condition> |
    And the following equipment record was persisted in db
      | serialNumber   | uid   | status   | type            | model   | commissionedAt   | conditionNotes   | condition   |
      | <serialNumber> | <uid> | <status> | <equipmentType> | <model> | <commissionedAt> | <conditionNotes> | <condition> |
    Examples:
      | serialNumber | uid             | equipmentType | status | model     | commissionedAt | conditionNotes | condition   |
      | EQ-001       | BIKE-001-UPDATE | SCOOTER       | BROKEN | Model A++ | 2026-01-20     | Fair           | MAINTENANCE |

  Scenario Outline: Create new equipment with not existing status
    Given the equipment request is prepared with the following data
      | serialNumber   | uid   | type            | status   | model   | commissionedAt   | condition   |
      | <serialNumber> | <uid> | <equipmentType> | <status> | <model> | <commissionedAt> | <condition> |
    When a POST request has been made to "/api/equipments" endpoint
    Then the response status is 422
    And the response contains
      | path     | value                                                                 |
      | $.title  | Unprocessable Content                                                 |
      | $.detail | Referenced EquipmentStatus with identifier 'DOES_NOT_EXIST' not found |
    Examples:
      | serialNumber | uid          | equipmentType | status         | model   | commissionedAt | condition |
      | EQ-999       | BIKE-999-NEW | BICYCLE       | DOES_NOT_EXIST | Model X | 2026-01-15     | GOOD      |

  Scenario Outline: Create new equipment with not existing condition
    Given the equipment request is prepared with the following data
      | serialNumber   | uid   | type            | status   | model   | commissionedAt   | condition   |
      | <serialNumber> | <uid> | <equipmentType> | <status> | <model> | <commissionedAt> | <condition> |
    When a POST request has been made to "/api/equipments" endpoint
    Then the response status is 500
    Examples:
      | serialNumber | uid          | equipmentType | status | model   | commissionedAt | condition |
      | EQ-999       | BIKE-999-NEW | BICYCLE       | BROKEN | Model X | 2026-01-15     | EXCELLENT |

  Scenario Outline: Update existing equipment with violation of allowed status transitions
    Given the equipment being updated is
      | serialNumber | uid          | type    | status      | model   | commissionedAt | condition   |
      | EQ-TO-UPDATE | EQ-TO-UPDATE | BICYCLE | MAINTENANCE | Model C | 2026-01-30     | <condition> |
    And the equipment request is prepared with the following data
      | serialNumber   | uid   | type            | status   | model   | commissionedAt   | condition   |
      | <serialNumber> | <uid> | <equipmentType> | <status> | <model> | <commissionedAt> | <condition> |
    When a PUT request has been made to "/api/equipments/{requestedObjectId}" endpoint with context
    Then the response status is 422
    And the response contains
      | path                | value                               |
      | $.title             | Invalid status transition           |
      | $.errorCode         | equipment.status.invalid_transition |
      | $.params.fromStatus | MAINTENANCE                         |
      | $.params.toStatus   | BROKEN                              |
    Examples:
      | serialNumber | uid             | equipmentType | status | model     | commissionedAt | condition |
      | EQ-001-UPD   | BIKE-001-UPDATE | SCOOTER       | BROKEN | Model A++ | 2026-01-20     | GOOD      |

  Scenario Outline: Create new equipment when <violation> unique constraints are violated
    Given the equipment request is prepared with the following data
      | serialNumber   | uid   | type    | status    | model   | commissionedAt | condition |
      | <serialNumber> | <uid> | BICYCLE | AVAILABLE | Model X | 2026-01-15     | GOOD      |
    When a POST request has been made to "/api/equipments" endpoint
    Then the response status is 409
    And the response contains
      | path     | value         |
      | $.title  | Conflict      |
      | $.detail | <errorDetail> |
    Examples:
      | serialNumber | uid      | violation    | errorDetail                                         |
      | unique       | BIKE-001 | uid          | Equipment with identifier 'BIKE-001' already exists |

  Scenario: Update equipment with non-existent ID
    Given the equipment request is prepared with the following data
      | serialNumber | uid             | type    | status | model   | commissionedAt | condition |
      | EQ-999-UPD   | BIKE-999-UPDATE | SCOOTER | BROKEN | Model X | 2026-01-20     | GOOD      |
    When a PUT request has been made to "/api/equipments/{id}" endpoint with
      | {id} |
      | 999  |
    Then the response status is 404
    And the response contains
      | path     | value                                     |
      | $.title  | Not Found                                 |
      | $.detail | Equipment with identifier '999' not found |

  Scenario: Batch fetch returns all matching equipment when all IDs exist
    When a GET request has been made to "/api/equipments/batch" endpoint with query parameters
      | ids |
      | 1,2 |
    Then the response status is 200
    And the batch equipment response contains
      | serialNumber | uid        | status    | type    | model   | commissionedAt | conditionNotes | condition |
      | EQ-001       | BIKE-001   | AVAILABLE | BICYCLE | Model A |                | Good           | GOOD      |
      | EQ-002       | E-BIKE-001 | RENTED    | SCOOTER | Model B |                | Excellent      | GOOD      |

  Scenario: Batch fetch silently omits non-existent IDs
    When a GET request has been made to "/api/equipments/batch" endpoint with query parameters
      | ids      |
      | 1,99,100 |
    Then the response status is 200
    And the batch equipment response contains
      | serialNumber | uid      | status    | type    | model   | commissionedAt | conditionNotes | condition |
      | EQ-001       | BIKE-001 | AVAILABLE | BICYCLE | Model A |                | Good           | GOOD      |

  Scenario: Batch fetch returns empty list when no IDs match any record
    When a GET request has been made to "/api/equipments/batch" endpoint with query parameters
      | ids      |
      | 91,92,93 |
    Then the response status is 200
    And the batch equipment response is empty

  Scenario: Batch fetch de-duplicates repeated IDs
    When a GET request has been made to "/api/equipments/batch" endpoint with query parameters
      | ids   |
      | 4,4,4 |
    Then the response status is 200
    And the batch equipment response contains
      | serialNumber | uid      | status | type    | model   | commissionedAt | conditionNotes | condition |
      | EQ-004       | BIKE-002 | BROKEN | BICYCLE | Model C | 2026-01-30     | Fair           | BROKEN    |
