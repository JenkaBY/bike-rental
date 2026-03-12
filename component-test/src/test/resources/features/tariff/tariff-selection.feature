Feature: Tariff selection endpoint
  As an operator
  I want to automatically select the best tariff for equipment and duration
  So that I don't have to manually choose a tariff

  Background:
    Given the following equipment types exist in the database
      | slug    | name    | description |
      | bicycle | Bicycle | Two-wheeled |
      | scooter | Scooter | Electric    |
    And the following tariff record exists in db
      | id | name          | description | equipmentType | basePrice | halfHourPrice | hourPrice | dayPrice | discountedPrice | validFrom  | validTo    | status |
      | 1  | Bike Hour     | For bike    | bicycle       | 100.00    | 60.00         | 110.00    | 500.00   | 90.00           | 2026-01-01 | 2030-01-01 | ACTIVE |
      | 2  | Bike Advanced | For bike    | bicycle       | 50.00     | 30.00         | 50.00     | 250.00   | 90.00           | 2026-01-01 | 2030-01-01 | ACTIVE |
      | 3  | Scooter       | For scooter | scooter       | 200.00    | 120.00        | 200.00    | 800.00   | 180.00          | 2026-01-01 | 2030-01-01 | ACTIVE |

  Scenario: Successfully select tariff for hour period
    When a GET request has been made to "/api/tariffs/selection" endpoint with query parameters
      | equipmentType | durationMinutes | rentalDate |
      | bicycle       | 60              | 2026-02-10 |
    Then the response status is 200
    And the tariff selection response contains
      | id | name          | equipmentType | price | period |
      | 2  | Bike Advanced | bicycle       | 50.00 | HOUR   |

  Scenario: Successfully select tariff for half hour period
    When a GET request has been made to "/api/tariffs/selection" endpoint with query parameters
      | equipmentType | durationMinutes | rentalDate |
      | bicycle       | 30              | 2026-02-10 |
    Then the response status is 200
    And the tariff selection response contains
      | id | name          | equipmentType | price | period    |
      | 2  | Bike Advanced | bicycle       | 30.00 | HALF_HOUR |

  Scenario: Successfully select tariff for day period
    When a GET request has been made to "/api/tariffs/selection" endpoint with query parameters
      | equipmentType | durationMinutes | rentalDate |
      | bicycle       | 1440            | 2026-02-10 |
    Then the response status is 200
    And the tariff selection response contains
      | id | name          | equipmentType | price  | period |
      | 2  | Bike Advanced | bicycle       | 250.00 | DAY    |

  Scenario: Select tariff with lowest price for period
    Given the following tariff record exists in db
      | id | name       | description | equipmentType | basePrice | halfHourPrice | hourPrice | dayPrice | discountedPrice | validFrom  | validTo    | status |
      | 4  | Bike Cheap | For bike    | bicycle       | 100.00    | 50.00         | 10.00     | 400.00   | 85.00           | 2026-01-01 | 2030-01-01 | ACTIVE |
    When a GET request has been made to "/api/tariffs/selection" endpoint with query parameters
      | equipmentType | durationMinutes | rentalDate |
      | bicycle       | 60              | 2026-02-10 |
    Then the response status is 200
    And the tariff selection response contains
      | id | name       | equipmentType | price | period |
      | 4  | Bike Cheap | bicycle       | 10.00 | HOUR   |

  @ResetClock
  Scenario: Select tariff without rentalDate (uses today)
    Given today is "2026-01-15"
    When a GET request has been made to "/api/tariffs/selection" endpoint with query parameters
      | equipmentType | durationMinutes |
      | bicycle       | 60              |
    Then the response status is 200
    And the tariff selection response contains
      | id | name          | equipmentType | price | period |
      | 2  | Bike Advanced | bicycle       | 50.00 | HOUR   |

  Scenario: Return 400 when equipmentType is missing
    When a GET request has been made to "/api/tariffs/selection" endpoint with query parameters
      | durationMinutes |
      | 60              |
    Then the response status is 400
    And the response contains
      | path     | value                                                                                      |
      | $.title  | Bad Request                                                                                |
      | $.detail | Required request parameter 'equipmentType' for method parameter type String is not present |

  Scenario: Return 400 when durationMinutes is missing
    When a GET request has been made to "/api/tariffs/selection" endpoint with query parameters
      | equipmentType |
      | bicycle       |
    Then the response status is 400
    And the response contains
      | path     | value                                                                                     |
      | $.title  | Bad Request                                                                               |
      | $.detail | Required request parameter 'durationMinutes' for method parameter type int is not present |

  Scenario: Return 404 when no suitable tariff found
    When a GET request has been made to "/api/tariffs/selection" endpoint with query parameters
      | equipmentType | durationMinutes | rentalDate |
      | unknown       | 60              | 2026-02-10 |
    Then the response status is 404
    And the response contains
      | path     | value                     |
      | $.title  | Resource not found        |
      | $.detail | Suitable tariff not found |

  @ResetClock
  Scenario: Return 404 when no active tariffs for equipment type
    Given today is "2026-01-15"
    Given the following equipment types exist in the database
      | slug  | name  | description |
      | e-car | E-Car | Kids e-car  |
    Given the following tariff record exists in db
      | id | name     | description | equipmentType | basePrice | halfHourPrice | hourPrice | dayPrice | discountedPrice | validFrom  | validTo    | status   |
      | 5  | Inactive | For E-car   | e-car         | 100.00    | 60.00         | 100.00    | 500.00   | 90.00           | 2026-01-10 | 2030-01-01 | INACTIVE |
    When a GET request has been made to "/api/tariffs/selection" endpoint with query parameters
      | equipmentType | durationMinutes | rentalDate |
      | e-car         | 60              | 2026-02-07 |
    Then the response status is 404
    And the response contains
      | path     | value                     |
      | $.title  | Resource not found        |
      | $.detail | Suitable tariff not found |
