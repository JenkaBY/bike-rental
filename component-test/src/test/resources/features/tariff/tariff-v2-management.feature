Feature: Tariff V2 API
  As an operator
  I want to manage V2 tariffs
  So that I can create tariffs, select by equipment/duration

  Background:
    Given the following equipment types exist in the database
      | slug    | name    | description                  |
      | bicycle | Bicycle | Two-wheeled                  |
      | scooter | Scooter | Electric                     |
      | helmet  | Helmet  | Accessory                    |
      | special | Special | It's used for special tariff |

  Scenario Outline: Create a tariff v2 and get by id
    Given the pricing params for tariff request are
      | firstHourPrice   | hourlyDiscount   | minimumHourlyPrice   | hourlyPrice   | dailyPrice   | overtimeHourlyPrice   | issuanceFee   | minimumDurationMinutes   | minimumDurationSurcharge   | price   |
      | <firstHourPrice> | <hourlyDiscount> | <minimumHourlyPrice> | <hourlyPrice> | <dailyPrice> | <overtimeHourlyPrice> | <issuanceFee> | <minimumDurationMinutes> | <minimumDurationSurcharge> | <price> |
    And the tariff v2 request is prepared with the following data
      | name           | description       | equipmentType | pricingType   | validFrom  | validTo    |
      | Hourly Bicycle | Degressive hourly | bicycle       | <pricingType> | 2026-01-01 | 2029-01-01 |
    When a POST request has been made to "/api/v2/tariffs" endpoint
    Then the response status is 201
    And the tariff v2 response only contains
      | name           | description       | equipmentType | pricingType   | status   | validFrom  | validTo    |
      | Hourly Bicycle | Degressive hourly | bicycle       | <pricingType> | INACTIVE | 2026-01-01 | 2029-01-01 |
#    next scenario
    When a GET request has been made to "/api/v2/tariffs/{requestedObjectId}" endpoint with context
    Then the response status is 200
    And the tariff v2 response only contains
      | name           | description       | equipmentType | pricingType   | status   | validFrom  | validTo    |
      | Hourly Bicycle | Degressive hourly | bicycle       | <pricingType> | INACTIVE | 2026-01-01 | 2029-01-01 |
    Examples:
      | pricingType       | firstHourPrice | hourlyDiscount | minimumHourlyPrice | hourlyPrice | dailyPrice | overtimeHourlyPrice | issuanceFee | minimumDurationMinutes | minimumDurationSurcharge | price |
      | DEGRESSIVE_HOURLY | 9.00           | 2.00           | 1.00               |             |            |                     |             | 30                     | 1.00                     |       |
      | FLAT_HOURLY       |                |                |                    | 15.00       |            |                     |             | 30                     | 1.00                     |       |
      | DAILY             |                |                |                    |             | 900.00     | 100.00              |             |                        |                          |       |
      | FLAT_FEE          |                |                |                    |             |            |                     | 10.00       |                        |                          |       |
      | SPECIAL           |                |                |                    |             |            |                     |             |                        |                          | 0     |

  Scenario: Update a tariff v2
    Given the pricing params for tariff request are
      | issuanceFee |
      | 1           |
    And the tariff v2 request is prepared with the following data
      | name           | description | equipmentType | pricingType | validFrom  | validTo    |
      | Hourly Bicycle | Flat fee    | helmet        | FLAT_FEE    | 2026-01-01 | 2029-01-01 |
    When a POST request has been made to "/api/v2/tariffs" endpoint
    Then the response status is 201
    And the tariff v2 response only contains
      | name           | description | equipmentType | pricingType | status   | validFrom  | validTo    |
      | Hourly Bicycle | Flat fee    | helmet        | FLAT_FEE    | INACTIVE | 2026-01-01 | 2029-01-01 |
#    next stage
    Given the pricing params for tariff request are
      | price |
      | 0     |
    And the tariff v2 request is prepared with the following data
      | name            | description | equipmentType | pricingType | validFrom  | validTo    |
      | Special Bicycle | Special     | bicycle       | SPECIAL     | 2025-01-31 | 2028-01-02 |
    When a PUT request has been made to "/api/v2/tariffs/{requestedObjectId}" endpoint with context
    Then the response status is 200
    And the tariff v2 response only contains
      | name            | description | equipmentType | pricingType | status   | validFrom  | validTo    |
      | Special Bicycle | Special     | bicycle       | SPECIAL     | INACTIVE | 2025-01-31 | 2028-01-02 |
    When a GET request has been made to "/api/v2/tariffs/{requestedObjectId}" endpoint with context
    Then the response status is 200
    And the tariff v2 response only contains
      | name            | description | equipmentType | pricingType | status   | validFrom  | validTo    |
      | Special Bicycle | Special     | bicycle       | SPECIAL     | INACTIVE | 2025-01-31 | 2028-01-02 |

  Scenario: Get all tariff v2 paginated
    When a GET request has been made to "/api/v2/tariffs" endpoint with query parameters
      | page | size |
      | 0    | 20   |
    Then the response status is 200
    And the response contains
      | path               | value |
      | $.pageRequest.size | 20    |
      | $.pageRequest.page | 0     |

  Scenario: Activate and Deactivate tariff v2
    Given the pricing params for tariff request are
      | issuanceFee |
      | 1           |
    And the tariff v2 request is prepared with the following data
      | name           | description | equipmentType | pricingType | validFrom  | validTo    |
      | Hourly Bicycle | Flat fee    | helmet        | FLAT_FEE    | 2026-01-01 | 2029-01-01 |
    When a POST request has been made to "/api/v2/tariffs" endpoint
    Then the response status is 201
#    default inactive
    And the tariff v2 response only contains
      | name           | description | equipmentType | pricingType | status   | validFrom  | validTo    |
      | Hourly Bicycle | Flat fee    | helmet        | FLAT_FEE    | INACTIVE | 2026-01-01 | 2029-01-01 |
    When a PATCH request has been made to "/api/v2/tariffs/{requestedObjectId}/activate" endpoint with context
#    Activate
    Then the response status is 200
    And the response contains
      | path     | value  |
      | $.status | ACTIVE |
#    deactivate
    When a PATCH request has been made to "/api/v2/tariffs/{requestedObjectId}/deactivate" endpoint with context
    Then the response status is 200
    And the response contains
      | path     | value    |
      | $.status | INACTIVE |

  Scenario: Fetch active tariffs
    Given the pricing params list for tariff request is
      | tariffId | issuanceFee | price |
      | 1        | 1           |       |
      | 2        | 2           |       |
      | 3        |             | 0     |
    And the following tariff v2 records exist in db
      | id | name            | description       | equipmentType | pricingType | status   | validFrom  | validTo    |
      | 1  | Hourly Bicycle  | Degressive hourly | bicycle       | FLAT_FEE    | ACTIVE   | 2026-01-01 | 2029-01-01 |
      | 2  | Flat Fee Helmet | Flat fee          | helmet        | FLAT_FEE    | INACTIVE | 2026-01-01 | 2029-01-01 |
      | 3  | Special Scooter | Special           | special       | SPECIAL     | ACTIVE   | 2025-01-31 | 2028-01-02 |
    When a GET request has been made to "/api/v2/tariffs/active" endpoint with query parameters
      | equipmentType |
      | special       |
    Then the response status is 200
    And the tariff v2 response contains list of
      | id | name            | description | equipmentType | pricingType | status | validFrom  | validTo    |
      | 3  | Special Scooter | Special     | special       | SPECIAL     | ACTIVE | 2025-01-31 | 2028-01-02 |
