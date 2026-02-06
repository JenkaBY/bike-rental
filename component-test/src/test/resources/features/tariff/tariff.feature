Feature: Tariff management endpoints
  As an administrator
  I want to manage tariff catalog
  So that I can create, activate, deactivate and query tariffs

  Background:
    Given the following equipment types exist in the database
      | slug    | name    | description        |
      | bicycle | Bicycle | Two-wheeled        |
      | scooter | Scooter | Electric           |
      | samokat | Samokat | Samokat mechanical |
    And the following tariff record exists in db
      | name    | description | equipmentType | basePrice | halfHourPrice | hourPrice | dayPrice | discountedPrice | validFrom  | validTo    | status   |
      | Bike    | For bike    | bicycle       | 5.00      | 2.50          | 5.00      | 20.00    | 4.50            | 2026-01-01 | 2030-01-01 | INACTIVE |
      | Samokat | For Samokat | samokat       | 25.00     | 22.50         | 25.00     | 22.00    | 24.50           | 2024-01-01 |            | ACTIVE   |
      | E-Bike  | For ebike   | ebike         | 35.00     | 32.50         | 35.00     | 32.00    | 34.50           | 2026-05-01 |            | ACTIVE   |


  Scenario Outline: Create a tariff
    Given the tariff request is prepared with the following data
      | name   | description   | equipmentType   | basePrice   | halfHourPrice   | hourPrice   | dayPrice   | discountedPrice   | validFrom   | validTo   | status   |
      | <name> | <description> | <equipmentType> | <basePrice> | <halfHourPrice> | <hourPrice> | <dayPrice> | <discountedPrice> | <validFrom> | <validTo> | <status> |
    When a POST request has been made to "/api/tariffs" endpoint
    Then the response status is 201
    And the tariff response only contains
      | name   | description   | equipmentType   | basePrice   | halfHourPrice   | hourPrice   | dayPrice   | discountedPrice   | validFrom   | validTo   | status   |
      | <name> | <description> | <equipmentType> | <basePrice> | <halfHourPrice> | <hourPrice> | <dayPrice> | <discountedPrice> | <validFrom> | <validTo> | <status> |
    And the following tariff record was persisted in db
      | name   | description   | equipmentType   | basePrice   | halfHourPrice   | hourPrice   | dayPrice   | discountedPrice   | validFrom   | validTo   | status   |
      | <name> | <description> | <equipmentType> | <basePrice> | <halfHourPrice> | <hourPrice> | <dayPrice> | <discountedPrice> | <validFrom> | <validTo> | <status> |
    Examples:
      | name    | description   | equipmentType | basePrice | halfHourPrice | hourPrice | dayPrice | discountedPrice | validFrom  | validTo    | status |
      | Scooter | For E-scooter | scooter       | 1.00      | 2.50          | 3.00      | 4.00     | 5.50            | 2025-01-01 | 2029-01-01 | ACTIVE |

  Scenario: Get all paginated tariffs
    When a GET request has been made to "/api/tariffs" endpoint with query parameters
      | page | size |
      | 0    | 2    |
    Then the response status is 200
    And the response contains
      | path                 | value |
      | $.totalItems         | 3     |
      | $.pageRequest.size   | 2     |
      | $.pageRequest.page   | 0     |
      | $.pageRequest.sortBy | name  |
    And the tariff response only contains page of
      | name   | description | equipmentType | basePrice | halfHourPrice | hourPrice | dayPrice | discountedPrice | validFrom  | validTo    | status   |
      | Bike   | For bike    | bicycle       | 5.00      | 2.50          | 5.00      | 20.00    | 4.50            | 2026-01-01 | 2030-01-01 | INACTIVE |
      | E-Bike | For ebike   | ebike         | 35.00     | 32.50         | 35.00     | 32.00    | 34.50           | 2026-05-01 |            | ACTIVE   |
    Then a GET request has been made to "/api/tariffs" endpoint with query parameters
      | page | size |
      | 1    | 2    |
    And the tariff response only contains page of
      | name    | description | equipmentType | basePrice | halfHourPrice | hourPrice | dayPrice | discountedPrice | validFrom  | validTo | status |
      | Samokat | For Samokat | samokat       | 25.00     | 22.50         | 25.00     | 22.00    | 24.50           | 2024-01-01 |         | ACTIVE |
    And the response contains
      | path                 | value |
      | $.totalItems         | 3     |
      | $.pageRequest.size   | 2     |
      | $.pageRequest.page   | 1     |
      | $.pageRequest.sortBy | name  |

  Scenario: Get active tariffs by equipment type
    Given the following tariff record exists in db
      | name   | description | equipmentType | basePrice | halfHourPrice | hourPrice | dayPrice | discountedPrice | validFrom  | validTo    | status |
      | Bike 1 | For bike 1  | bicycle       | 5.00      | 2.50          | 5.00      | 20.00    | 4.50            | 2026-01-01 | 2030-01-01 | ACTIVE |
      | Bike 2 | For bike 2  | bicycle       | 5.00      | 2.50          | 5.00      | 20.00    | 4.50            | 2026-01-01 |            | ACTIVE |
    When a GET request has been made to "/api/tariffs/active" endpoint with query parameters
      | equipmentType |
      | bicycle       |
    Then the response status is 200
    And the tariff response only contains list of
      | name   | description | equipmentType | basePrice | halfHourPrice | hourPrice | dayPrice | discountedPrice | validFrom  | validTo    | status |
      | Bike 1 | For bike 1  | bicycle       | 5.00      | 2.50          | 5.00      | 20.00    | 4.50            | 2026-01-01 | 2030-01-01 | ACTIVE |
      | Bike 2 | For bike 2  | bicycle       | 5.00      | 2.50          | 5.00      | 20.00    | 4.50            | 2026-01-01 |            | ACTIVE |
    And total tariff records in db is 5

  Scenario Outline: Update existing tariff and Get it by id
    #    persist tariff id in the scenario context as requestedObjectId
    Given the tariff being updated is
      | name       | description | equipmentType | basePrice | halfHourPrice | hourPrice | dayPrice | discountedPrice | validFrom  | validTo | status |
      | SamokatNew | For Samokat | samokat       | 25.00     | 22.50         | 25.00     | 22.00    | 24.50           | 2027-01-01 |         | ACTIVE |
    And the tariff request is prepared with the following data
      | name   | description   | equipmentType   | basePrice   | halfHourPrice   | hourPrice   | dayPrice   | discountedPrice   | validFrom   | validTo   | status   |
      | <name> | <description> | <equipmentType> | <basePrice> | <halfHourPrice> | <hourPrice> | <dayPrice> | <discountedPrice> | <validFrom> | <validTo> | <status> |
    When a PUT request has been made to "/api/tariffs/{requestedObjectId}" endpoint with context
    Then the response status is 200
    And the tariff response only contains
      | name   | description   | equipmentType   | basePrice   | halfHourPrice   | hourPrice   | dayPrice   | discountedPrice   | validFrom   | validTo   | status   |
      | <name> | <description> | <equipmentType> | <basePrice> | <halfHourPrice> | <hourPrice> | <dayPrice> | <discountedPrice> | <validFrom> | <validTo> | <status> |
    And the following tariff record was persisted in db
      | name   | description   | equipmentType   | basePrice   | halfHourPrice   | hourPrice   | dayPrice   | discountedPrice   | validFrom   | validTo   | status   |
      | <name> | <description> | <equipmentType> | <basePrice> | <halfHourPrice> | <hourPrice> | <dayPrice> | <discountedPrice> | <validFrom> | <validTo> | <status> |
#    retrieve by id
    When a GET request has been made to "/api/tariffs/{requestedObjectId}" endpoint with context
    And the tariff response only contains
      | name   | description   | equipmentType   | basePrice   | halfHourPrice   | hourPrice   | dayPrice   | discountedPrice   | validFrom   | validTo   | status   |
      | <name> | <description> | <equipmentType> | <basePrice> | <halfHourPrice> | <hourPrice> | <dayPrice> | <discountedPrice> | <validFrom> | <validTo> | <status> |
    Examples:
      | name        | description     | equipmentType | basePrice | halfHourPrice | hourPrice | dayPrice | discountedPrice | validFrom  | validTo    | status   |
      | Samokat New | For Samokat New | scooter       | 1.00      | 2.50          | 3.00      | 4.00     | 5.50            | 2025-01-01 | 2028-01-01 | INACTIVE |

  Scenario: Activate and Deactivate a tariff
    Given the tariff being updated is
      | name       | description | equipmentType | basePrice | halfHourPrice | hourPrice | dayPrice | discountedPrice | validFrom  | validTo | status   |
      | SamokatNew | For Samokat | samokat       | 25.00     | 22.50         | 25.00     | 22.00    | 24.50           | 2027-01-01 |         | INACTIVE |
    When a PATCH request has been made to "/api/tariffs/{requestedObjectId}/activate" endpoint with context
    Then the response status is 200
    And the response contains
      | path     | value  |
      | $.status | ACTIVE |
    When a PATCH request has been made to "/api/tariffs/{requestedObjectId}/deactivate" endpoint with context
    Then the response status is 200
    And the response contains
      | path     | value    |
      | $.status | INACTIVE |

  Scenario Outline: Activate/Deactivate non-existing tariff
    When a PATCH request has been made to "/api/tariffs/{tariffId}/<action>" endpoint with
      | {tariffId} |
      | <tariffId> |
    Then the response status is 404
    And the response contains
      | path     | value                                         |
      | $.title  | Not Found                                     |
      | $.detail | Tariff with identifier '<tariffId>' not found |
    Examples:
      | tariffId | action     |
      | 9999     | activate   |
      | 8888     | deactivate |

  Scenario: Get non-existing tariff
    When a GET request has been made to "/api/tariffs/999999" endpoint
    Then the response status is 404
    And the response contains
      | path     | value                                     |
      | $.title  | Not Found                                 |
      | $.detail | Tariff with identifier '999999' not found |

  Scenario: Update non-existing tariff
    Given the tariff request is prepared with the following data
      | name       | description | equipmentType | basePrice | halfHourPrice | hourPrice | dayPrice | discountedPrice | validFrom  | validTo | status |
      | SamokatNew | For Samokat | samokat       | 25.00     | 22.50         | 25.00     | 22.00    | 24.50           | 2027-01-01 |         | ACTIVE |
    When a PUT request has been made to "/api/tariffs/999999" endpoint
    Then the response status is 404
    And the response contains
      | path     | value                                     |
      | $.title  | Not Found                                 |
      | $.detail | Tariff with identifier '999999' not found |