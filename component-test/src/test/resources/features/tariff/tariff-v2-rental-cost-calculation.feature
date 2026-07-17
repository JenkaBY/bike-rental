Feature: Tariff V2 API rental cost calculation
  As an operator
  I want to know the rental cost calculation with the pricing model
  So that I can request the cost calculation for a rental with the tariff v2

  Background:
    Given the following equipment types exist in the database
      | slug    | name    | description                  |
      | BICYCLE | Bicycle | Two-wheeled                  |
      | SCOOTER | Scooter | Electric                     |
      | HELMET  | Helmet  | Accessory                    |
      | SPECIAL | Special | It's used for special tariff |
      | MOPED   | Moped   | Degressive-only, no daily    |
    And the pricing params list for tariff request is
      | tariffId | pricingType       | firstHourPrice | hourlyDiscount | minimumHourlyPrice | hourlyPrice | dailyPrice | overtimeHourlyPrice | issuanceFee | minimumDurationMinutes | minimumDurationSurcharge | price |
      | 1        | DEGRESSIVE_HOURLY | 9.00           | 2.00           | 1.00               |             |            |                     |             | 30                     | 1.00                     |       |
      | 2        | FLAT_HOURLY       |                |                |                    | 15.00       |            |                     |             | 30                     | 1.00                     |       |
      | 3        | DAILY             |                |                |                    |             | 25.00      | 1.00                |             |                        |                          |       |
      | 4        | FLAT_FEE          |                |                |                    |             |            |                     | 1.00        |                        |                          |       |
      | 5        | SPECIAL           |                |                |                    |             |            |                     |             |                        |                          | 0     |
      | 6        | DEGRESSIVE_HOURLY | 9.00           | 2.00           | 1.00               |             |            |                     |             | 30                     | 1.00                     |       |
    And the following tariff v2 records exist in db
      | id | name                | description             | equipmentType | pricingType       | status | validFrom  | validTo |
      | 1  | Hourly Bicycle      | Degressive hourly       | BICYCLE       | DEGRESSIVE_HOURLY | ACTIVE | 2026-01-01 |         |
      | 2  | Flat Hourly Scooter | Flat hourly             | SCOOTER       | FLAT_HOURLY       | ACTIVE | 2026-01-01 |         |
      | 3  | Daily Bicycle       | Daily hourly            | BICYCLE       | DAILY             | ACTIVE | 2026-01-01 |         |
      | 4  | Flat Fee Helmet     | Flat fee                | HELMET        | FLAT_FEE          | ACTIVE | 2026-01-01 |         |
      | 5  | Special Tariff      | Apply for any equipment | ANY           | SPECIAL           | ACTIVE | 2025-01-31 |         |
      | 6  | Hourly Moped        | Degressive hourly       | MOPED         | DEGRESSIVE_HOURLY | ACTIVE | 2026-01-01 |         |


  # ---------------------------------------------------------------------------
  # V2 endpoint: PUT /api/tariffs/calculations (per-equipment return timestamps)
  # ---------------------------------------------------------------------------

  Scenario: V2 partial return - two items of the same type returned at different times
    Given the equipment items for cost calculation request are
      | equipmentId | equipmentType | returnAt         |
      | 10          | SCOOTER       | 2026-06-01T11:00 |
      | 11          | SCOOTER       | 2026-06-01T14:00 |
    And the rental cost calculation request is prepared with the following data
      | startAt          | plannedDurationMinutes |
      | 2026-06-01T09:00 | 180                    |
    When a PUT request has been made to "/api/tariffs/calculations" endpoint
    Then the response status is 200
    And the rental cost calculation response only contains
      | totalCost | subtotal | effectiveDurationMinutes | estimate | specialPricingApplied |
      | 105       | 105      | 300                      | false    | false                 |
    And the rental cost calculation response only contains the breakdown with the following data
      | equipmentId | equipmentType | tariffId | tariffName          | pricingType | itemCost | billedDuration | overtimeMinutes | forgivenMinutes | pattern                             | message                           |
      | 10          | SCOOTER       | 2        | Flat Hourly Scooter | FLAT_HOURLY | 30       | 120            | 0               | 0               | breakdown.cost.flat_hourly.standard | 2h 0min flat: 2*15 + partial = 30 |
      | 11          | SCOOTER       | 2        | Flat Hourly Scooter | FLAT_HOURLY | 75       | 300            | 120             | 0               | breakdown.cost.flat_hourly.standard | 5h 0min flat: 5*15 + partial = 75 |

  Scenario: V2 overnight FLAT_FEE item spans two calendar dates - charged 2 days
    Given the equipment items for cost calculation request are
      | equipmentId | equipmentType | returnAt         |
      | 20          | HELMET        | 2026-06-02T08:00 |
    And the rental cost calculation request is prepared with the following data
      | startAt          | plannedDurationMinutes |
      | 2026-06-01T20:00 | 720                    |
    When a PUT request has been made to "/api/tariffs/calculations" endpoint
    Then the response status is 200
    And the rental cost calculation response only contains
      | totalCost | subtotal | effectiveDurationMinutes | estimate | specialPricingApplied |
      | 2         | 2        | 720                      | false    | false                 |
    And the rental cost calculation response only contains the breakdown with the following data
      | equipmentId | equipmentType | tariffId | tariffName      | pricingType | itemCost | billedDuration | overtimeMinutes | forgivenMinutes | pattern                 | message            |
      | 20          | HELMET        | 4        | Flat Fee Helmet | FLAT_FEE    | 2        | 720            | 0               | 0               | breakdown.cost.flat_fee | Flat fee: 1*2d = 2 |

  Scenario: V2 item without returnAt is billed using the planned duration (estimate mode)
    Given the equipment items for cost calculation request are
      | equipmentId | equipmentType | returnAt |
      | 30          | SCOOTER       |          |
    And the rental cost calculation request is prepared with the following data
      | startAt          | plannedDurationMinutes |
      | 2026-06-01T09:00 | 180                    |
    When a PUT request has been made to "/api/tariffs/calculations" endpoint
    Then the response status is 200
    And the rental cost calculation response only contains
      | totalCost | subtotal | effectiveDurationMinutes | estimate | specialPricingApplied |
      | 45        | 45       | 180                      | true     | false                 |
    And the rental cost calculation response only contains the breakdown with the following data
      | equipmentId | equipmentType | tariffId | tariffName          | pricingType | itemCost | billedDuration | overtimeMinutes | forgivenMinutes | pattern                             | message                           |
      | 30          | SCOOTER       | 2        | Flat Hourly Scooter | FLAT_HOURLY | 45       | 180            | 0               | 0               | breakdown.cost.flat_hourly.standard | 3h 0min flat: 3*15 + partial = 45 |

  Scenario: V2 forgiveness threshold is applied independently per item
    Given the equipment items for cost calculation request are
      | equipmentId | equipmentType | returnAt         |
      | 40          | SCOOTER       | 2026-06-01T10:05 |
      | 41          | SCOOTER       | 2026-06-01T10:08 |
    And the rental cost calculation request is prepared with the following data
      | startAt          | plannedDurationMinutes |
      | 2026-06-01T09:00 | 60                     |
    When a PUT request has been made to "/api/tariffs/calculations" endpoint
    Then the response status is 200
    And the rental cost calculation response only contains
      | totalCost | subtotal | effectiveDurationMinutes | estimate | specialPricingApplied |
      | 31.25     | 31.25    | 68                       | false    | false                 |
    And the rental cost calculation response only contains the breakdown with the following data
      | equipmentId | equipmentType | tariffId | tariffName          | pricingType | itemCost | billedDuration | overtimeMinutes | forgivenMinutes | pattern                             | message                              |
      | 40          | SCOOTER       | 2        | Flat Hourly Scooter | FLAT_HOURLY | 15       | 60             | 5               | 5               | breakdown.cost.flat_hourly.standard | 1h 0min flat: 1*15 + partial = 15    |
      | 41          | SCOOTER       | 2        | Flat Hourly Scooter | FLAT_HOURLY | 16.25    | 68             | 8               | 0               | breakdown.cost.flat_hourly.standard | 1h 8min flat: 1*15 + partial = 16.25 |

  # ---------------------------------------------------------------------------
  # Early-return free window (app.rental.forgiveness.early-return-window = 10m):
  # an item returned within 10 minutes of ITS OWN start is free (no charge,
  # no minimum-duration surcharge, no penalty).
  # ---------------------------------------------------------------------------

  Scenario Outline: V2 early return within the free window is not charged - returned at <returnAt>
    Given the equipment items for cost calculation request are
      | equipmentId | equipmentType | returnAt   |
      | 1           | SCOOTER       | <returnAt> |
    And the rental cost calculation request is prepared with the following data
      | startAt          | plannedDurationMinutes |
      | 2026-06-01T09:00 | 60                     |
    When a PUT request has been made to "/api/tariffs/calculations" endpoint
    Then the response status is 200
    And the rental cost calculation response only contains
      | totalCost | subtotal   | effectiveDurationMinutes | estimate | specialPricingApplied |
      | <total>   | <subtotal> | <billedDuration>         | false    | false                 |
    And the rental cost calculation response only contains the breakdown with the following data
      | equipmentId | equipmentType | tariffId | tariffName          | pricingType | itemCost | billedDuration   | overtimeMinutes | forgivenMinutes | pattern   | message   |
      | 1           | SCOOTER       | 2        | Flat Hourly Scooter | FLAT_HOURLY | <cost>   | <billedDuration> | <overtime>      | <forgiven>      | <pattern> | <message> |
    Examples:
      | returnAt         | billedDuration | cost | subtotal | total | overtime | forgiven | message                             | pattern                            |
      | 2026-06-01T09:05 | 5              | 0    | 0        | 0     | 0        | 0        | Early return within 10min: free = 0 | breakdown.cost.early_return_free   |
      | 2026-06-01T09:10 | 10             | 0    | 0        | 0     | 0        | 0        | Early return within 10min: free = 0 | breakdown.cost.early_return_free   |
      | 2026-06-01T09:11 | 11             | 8.5  | 8.5      | 8.5   | 0        | 0        | 30min minimum: 15/2 + 1 = 8.5       | breakdown.cost.flat_hourly.minimum |

  Scenario: V2 all equipment returned within the free window is fully free
    Given the equipment items for cost calculation request are
      | equipmentId | equipmentType | returnAt         |
      | 70          | SCOOTER       | 2026-06-01T09:05 |
      | 71          | SCOOTER       | 2026-06-01T09:08 |
    And the rental cost calculation request is prepared with the following data
      | startAt          | plannedDurationMinutes |
      | 2026-06-01T09:00 | 60                     |
    When a PUT request has been made to "/api/tariffs/calculations" endpoint
    Then the response status is 200
    And the rental cost calculation response only contains
      | totalCost | subtotal | effectiveDurationMinutes | estimate | specialPricingApplied |
      | 0         | 0        | 8                        | false    | false                 |
    And the rental cost calculation response only contains the breakdown with the following data
      | equipmentId | equipmentType | tariffId | tariffName          | pricingType | itemCost | billedDuration | overtimeMinutes | forgivenMinutes | pattern                          | message                             |
      | 70          | SCOOTER       | 2        | Flat Hourly Scooter | FLAT_HOURLY | 0        | 5              | 0               | 0               | breakdown.cost.early_return_free | Early return within 10min: free = 0 |
      | 71          | SCOOTER       | 2        | Flat Hourly Scooter | FLAT_HOURLY | 0        | 8              | 0               | 0               | breakdown.cost.early_return_free | Early return within 10min: free = 0 |

  Scenario: V2 equipment taken mid-rental and returned within the free window of its own start is not charged
    Given the equipment items for cost calculation request are
      | equipmentId | equipmentType | startAt          | returnAt         |
      | 80          | SCOOTER       |                  | 2026-06-01T11:00 |
      | 81          | SCOOTER       | 2026-06-01T10:55 | 2026-06-01T11:00 |
    And the rental cost calculation request is prepared with the following data
      | startAt          | plannedDurationMinutes |
      | 2026-06-01T09:00 | 120                    |
    When a PUT request has been made to "/api/tariffs/calculations" endpoint
    Then the response status is 200
    And the rental cost calculation response only contains
      | totalCost | subtotal | effectiveDurationMinutes | estimate | specialPricingApplied |
      | 30        | 30       | 120                      | false    | false                 |
    And the rental cost calculation response only contains the breakdown with the following data
      | equipmentId | equipmentType | tariffId | tariffName          | pricingType | itemCost | billedDuration | overtimeMinutes | forgivenMinutes | pattern                             | message                             |
      | 80          | SCOOTER       | 2        | Flat Hourly Scooter | FLAT_HOURLY | 30       | 120            | 0               | 0               | breakdown.cost.flat_hourly.standard | 2h 0min flat: 2*15 + partial = 30   |
      | 81          | SCOOTER       | 2        | Flat Hourly Scooter | FLAT_HOURLY | 0        | 5              | 0               | 0               | breakdown.cost.early_return_free    | Early return within 10min: free = 0 |

  Scenario: V2 global discount is applied to the combined subtotal
    Given the equipment items for cost calculation request are
      | equipmentId | equipmentType | returnAt         |
      | 60          | SCOOTER       | 2026-06-01T10:00 |
      | 61          | SCOOTER       | 2026-06-01T10:00 |
    And the rental cost calculation request is prepared with the following data
      | startAt          | plannedDurationMinutes | discountPercent |
      | 2026-06-01T09:00 | 60                     | 10              |
    When a PUT request has been made to "/api/tariffs/calculations" endpoint
    Then the response status is 200
    And the rental cost calculation response only contains
      | totalCost | subtotal | discountAmount | discountPercent | effectiveDurationMinutes | estimate | specialPricingApplied |
      | 27        | 30       | 3              | 10              | 60                       | false    | false                 |
    And the rental cost calculation response only contains the breakdown with the following data
      | equipmentId | equipmentType | tariffId | tariffName          | pricingType | itemCost | billedDuration | overtimeMinutes | forgivenMinutes | pattern                             | message                           |
      | 60          | SCOOTER       | 2        | Flat Hourly Scooter | FLAT_HOURLY | 15       | 60             | 0               | 0               | breakdown.cost.flat_hourly.standard | 1h 0min flat: 1*15 + partial = 15 |
      | 61          | SCOOTER       | 2        | Flat Hourly Scooter | FLAT_HOURLY | 15       | 60             | 0               | 0               | breakdown.cost.flat_hourly.standard | 1h 0min flat: 1*15 + partial = 15 |

  Scenario: V2 special tariff mode applies a fixed group price and zeroes item costs
    Given the equipment items for cost calculation request are
      | equipmentId | equipmentType | returnAt         |
      | 50          | SCOOTER       | 2026-06-01T10:00 |
      | 51          | SCOOTER       | 2026-06-01T11:00 |
    And the rental cost calculation request is prepared with the following data
      | startAt          | plannedDurationMinutes | specialTariffId | specialPrice |
      | 2026-06-01T09:00 | 60                     | 5               | 666          |
    When a PUT request has been made to "/api/tariffs/calculations" endpoint
    Then the response status is 200
    And the rental cost calculation response only contains
      | totalCost | subtotal | effectiveDurationMinutes | estimate | specialPricingApplied |
      | 666       | 666      | 60                       | false    | true                  |
    And the rental cost calculation response only contains the breakdown with the following data
      | equipmentId | equipmentType | tariffId | tariffName     | pricingType | itemCost | billedDuration | overtimeMinutes | forgivenMinutes | pattern                      | message                         |
      | 50          | SCOOTER       | 5        | Special Tariff | SPECIAL     | 0        | 60             | 0               | 0               | breakdown.cost.special.group | Special tariff applied to group |
      | 51          | SCOOTER       | 5        | Special Tariff | SPECIAL     | 0        | 60             | 0               | 0               | breakdown.cost.special.group | Special tariff applied to group |

  Scenario: V2 returns 404 when an equipment type has no active tariff
    Given the equipment items for cost calculation request are
      | equipmentId | equipmentType | returnAt         |
      | 99          | UNKNOWN       | 2026-06-01T10:00 |
    And the rental cost calculation request is prepared with the following data
      | startAt          | plannedDurationMinutes |
      | 2026-06-01T09:00 | 60                     |
    When a PUT request has been made to "/api/tariffs/calculations" endpoint
    Then the response status is 404


  # ---------------------------------------------------------------------------
  # V2 parity with V1 — same costs via PUT /api/tariffs/calculations.
  # A V1 duration D maps to startAt 2026-06-01T00:00 + returnAt = startAt + D.
  # effectiveDurationMinutes asserts the V2 value (billed duration).
  # ---------------------------------------------------------------------------

  Scenario Outline: V2 parity - single rental <pricingType>
    Given the equipment items for cost calculation request are
      | equipmentId | equipmentType   | returnAt   |
      | 1           | <equipmentType> | <returnAt> |
    And the rental cost calculation request is prepared with the following data
      | startAt          | plannedDurationMinutes   | specialTariffId   | specialPrice   |
      | 2026-06-01T00:00 | <plannedDurationMinutes> | <specialTariffId> | <specialPrice> |
    When a PUT request has been made to "/api/tariffs/calculations" endpoint
    Then the response status is 200
    And the rental cost calculation response only contains
      | totalCost | subtotal   | effectiveDurationMinutes | estimate | specialPricingApplied |
      | <total>   | <subtotal> | <billedDuration>         | false    | <special>             |
    And the rental cost calculation response only contains the breakdown with the following data
      | equipmentType   | tariffId   | tariffName   | pricingType   | itemCost | billedDuration   | pattern   | message   |
      | <equipmentType> | <tariffId> | <tariffName> | <pricingType> | <cost>   | <billedDuration> | <pattern> | <message> |
    Examples:
      | pricingType       | equipmentType | returnAt         | plannedDurationMinutes | billedDuration | cost  | subtotal | total | specialTariffId | specialPrice | tariffId | tariffName          | message                           | pattern                                   | special |
      | DEGRESSIVE_HOURLY | BICYCLE       | 2026-06-01T01:00 | 60                     | 60             | 9.00  | 9.00     | 9.00  |                 |              | 1        | Hourly Bicycle      | 1h 0min degressive: 9 = 9         | breakdown.cost.degressive_hourly.standard | false   |
      | FLAT_HOURLY       | SCOOTER       | 2026-06-01T01:00 | 60                     | 60             | 15.00 | 15.00    | 15.00 |                 |              | 2        | Flat Hourly Scooter | 1h 0min flat: 1*15 + partial = 15 | breakdown.cost.flat_hourly.standard       | false   |
      | DAILY             | BICYCLE       | 2026-06-01T08:00 | 480                    | 480            | 25.00 | 25.00    | 25.00 |                 |              | 3        | Daily Bicycle       | 1d = 25                           | breakdown.cost.daily.standard             | false   |
      | FLAT_FEE          | HELMET        | 2026-06-01T01:00 | 60                     | 60             | 1     | 1        | 1     |                 |              | 4        | Flat Fee Helmet     | Flat fee: 1*1d = 1                | breakdown.cost.flat_fee                   | false   |
      | SPECIAL           | ANY           | 2026-06-01T01:00 | 60                     | 60             | 0     | 666      | 666   | 5               | 666          | 5        | Special Tariff      | Special tariff applied to group   | breakdown.cost.special.group              | true    |


  Scenario Outline: V2 parity - DEGRESSIVE_HOURLY returned at <returnAt> discount <discountPercent>
    Given the equipment items for cost calculation request are
      | equipmentId | equipmentType | returnAt   |
      | 1           | BICYCLE       | <returnAt> |
    And the rental cost calculation request is prepared with the following data
      | startAt          | plannedDurationMinutes | discountPercent   |
      | 2026-06-01T00:00 | 60                     | <discountPercent> |
    When a PUT request has been made to "/api/tariffs/calculations" endpoint
    Then the response status is 200
    And the rental cost calculation response only contains
      | totalCost | subtotal   | discountAmount   | discountPercent   | effectiveDurationMinutes | estimate |
      | <total>   | <subtotal> | <discountAmount> | <discountPercent> | <billedDuration>         | false    |
    And the rental cost calculation response only contains the breakdown with the following data
      | equipmentType | tariffId | tariffName     | pricingType       | itemCost | billedDuration   | overtimeMinutes | forgivenMinutes | pattern   | message   |
      | BICYCLE       | 1        | Hourly Bicycle | DEGRESSIVE_HOURLY | <cost>   | <billedDuration> | <overtime>      | <forgiven>      | <pattern> | <message> |
    Examples:
      | returnAt         | billedDuration | cost  | subtotal | total | discountPercent | discountAmount | overtime | forgiven | message                                     | pattern                                   |
      | 2026-06-01T00:05 | 5              | 0     | 0        | 0     | 0               | 0              |          |          | Early return within 10min: free = 0         | breakdown.cost.early_return_free          |
      | 2026-06-01T00:20 | 20             | 5.5   | 5.5      | 5.5   | 0               | 0              |          |          | 30min minimum: 9/2 + 1 = 5.5                | breakdown.cost.degressive_hourly.minimum  |
      | 2026-06-01T01:00 | 60             | 9.00  | 9.00     | 9.00  | 0               | 0              |          |          | 1h 0min degressive: 9 = 9                   | breakdown.cost.degressive_hourly.standard |
      | 2026-06-01T01:05 | 60             | 9.00  | 9.00     | 9.0   | 0               | 0              | 5        | 5        | 1h 0min degressive: 9 = 9                   | breakdown.cost.degressive_hourly.standard |
      | 2026-06-01T01:08 | 68             | 9.58  | 9.58     | 9.58  | 0               | 0              | 8        | 0        | 1h 8min degressive: 9+1*(7/12) = 9.58       | breakdown.cost.degressive_hourly.standard |
      | 2026-06-01T01:10 | 70             | 10.16 | 10.16    | 10.16 | 0               | 0              | 10       | 0        | 1h 10min degressive: 9+2*(7/12) = 10.16     | breakdown.cost.degressive_hourly.standard |
      | 2026-06-01T02:00 | 120            | 16    | 16       | 16    | 0               | 0              | 60       | 0        | 2h 0min degressive: 9+7 = 16                | breakdown.cost.degressive_hourly.standard |
      | 2026-06-01T03:00 | 180            | 21    | 21       | 21    | 0               | 0              | 120      | 0        | 3h 0min degressive: 9+7+5 = 21              | breakdown.cost.degressive_hourly.standard |
      | 2026-06-01T04:00 | 240            | 24    | 24       | 24    | 0               | 0              | 180      | 0        | 4h 0min degressive: 9+7+5+3 = 24            | breakdown.cost.degressive_hourly.standard |
      | 2026-06-01T05:00 | 300            | 25    | 25       | 25    | 0               | 0              | 240      | 0        | 5h 0min degressive: 9+7+5+3+1 = 25          | breakdown.cost.degressive_hourly.standard |
      | 2026-06-01T05:04 | 304            | 25    | 25       | 25    | 0               | 0              | 244      | 0        | 5h 4min degressive: 9+7+5+3+1+0*(1/12) = 25 | breakdown.cost.degressive_hourly.standard |
      | 2026-06-01T01:00 | 60             | 9     | 9.0      | 8.1   | 10              | 0.9            | 0        | 0        | 1h 0min degressive: 9 = 9                   | breakdown.cost.degressive_hourly.standard |
      | 2026-06-01T01:00 | 60             | 9     | 9.0      | 0     | 100             | 9              | 0        | 0        | 1h 0min degressive: 9 = 9                   | breakdown.cost.degressive_hourly.standard |
      | 2026-06-01T05:00 | 300            | 25    | 25       | 12.5  | 50              | 12.5           | 240      | 0        | 5h 0min degressive: 9+7+5+3+1 = 25          | breakdown.cost.degressive_hourly.standard |


  Scenario Outline: V2 parity - FLAT_HOURLY returned at <returnAt> discount <discountPercent>
    Given the equipment items for cost calculation request are
      | equipmentId | equipmentType | returnAt   |
      | 1           | SCOOTER       | <returnAt> |
    And the rental cost calculation request is prepared with the following data
      | startAt          | plannedDurationMinutes | discountPercent   |
      | 2026-06-01T00:00 | 60                     | <discountPercent> |
    When a PUT request has been made to "/api/tariffs/calculations" endpoint
    Then the response status is 200
    And the rental cost calculation response only contains
      | totalCost | subtotal   | discountAmount   | discountPercent   | effectiveDurationMinutes | estimate |
      | <total>   | <subtotal> | <discountAmount> | <discountPercent> | <billedDuration>         | false    |
    And the rental cost calculation response only contains the breakdown with the following data
      | equipmentType | tariffId | tariffName          | pricingType | itemCost | billedDuration   | overtimeMinutes | forgivenMinutes | pattern   | message   |
      | SCOOTER       | 2        | Flat Hourly Scooter | FLAT_HOURLY | <cost>   | <billedDuration> | <overtime>      | <forgiven>      | <pattern> | <message> |
    Examples:
      | returnAt         | billedDuration | cost  | subtotal | total | discountPercent | discountAmount | overtime | forgiven | message                               | pattern                             |
      | 2026-06-01T00:05 | 5              | 0     | 0        | 0     | 0               | 0              |          |          | Early return within 10min: free = 0   | breakdown.cost.early_return_free    |
      | 2026-06-01T00:20 | 20             | 8.5   | 8.5      | 8.5   | 0               | 0              |          |          | 30min minimum: 15/2 + 1 = 8.5         | breakdown.cost.flat_hourly.minimum  |
      | 2026-06-01T01:00 | 60             | 15    | 15       | 15    | 0               | 0              |          |          | 1h 0min flat: 1*15 + partial = 15     | breakdown.cost.flat_hourly.standard |
      | 2026-06-01T01:05 | 60             | 15    | 15       | 15    | 0               | 0              | 5        | 5        | 1h 0min flat: 1*15 + partial = 15     | breakdown.cost.flat_hourly.standard |
      | 2026-06-01T01:08 | 68             | 16.25 | 16.25    | 16.25 | 0               | 0              | 8        | 0        | 1h 8min flat: 1*15 + partial = 16.25  | breakdown.cost.flat_hourly.standard |
      | 2026-06-01T02:00 | 120            | 30    | 30       | 30    | 0               | 0              | 60       | 0        | 2h 0min flat: 2*15 + partial = 30     | breakdown.cost.flat_hourly.standard |
      | 2026-06-01T05:00 | 300            | 75    | 75       | 75    | 0               | 0              | 240      | 0        | 5h 0min flat: 5*15 + partial = 75     | breakdown.cost.flat_hourly.standard |
      | 2026-06-01T05:04 | 304            | 75    | 75       | 75    | 0               | 0              | 244      | 0        | 5h 4min flat: 5*15 + partial = 75     | breakdown.cost.flat_hourly.standard |
      | 2026-06-01T05:15 | 315            | 78.75 | 78.75    | 78.75 | 0               | 0              | 255      | 0        | 5h 15min flat: 5*15 + partial = 78.75 | breakdown.cost.flat_hourly.standard |
      | 2026-06-01T01:00 | 60             | 15    | 15       | 13.5  | 10              | 1.5            | 0        | 0        | 1h 0min flat: 1*15 + partial = 15     | breakdown.cost.flat_hourly.standard |
      | 2026-06-01T05:00 | 300            | 75.0  | 75.0     | 37.50 | 50              | 37.50          | 240      | 0        | 5h 0min flat: 5*15 + partial = 75     | breakdown.cost.flat_hourly.standard |


  Scenario Outline: V2 parity - DAILY returned at <returnAt> discount <discountPercent>
    Given the equipment items for cost calculation request are
      | equipmentId | equipmentType | returnAt   |
      | 1           | BICYCLE       | <returnAt> |
    And the rental cost calculation request is prepared with the following data
      | startAt          | plannedDurationMinutes | discountPercent   |
      | 2026-06-01T00:00 | 310                    | <discountPercent> |
    When a PUT request has been made to "/api/tariffs/calculations" endpoint
    Then the response status is 200
    And the rental cost calculation response only contains
      | totalCost | subtotal   | discountAmount   | discountPercent   | effectiveDurationMinutes | estimate |
      | <total>   | <subtotal> | <discountAmount> | <discountPercent> | <billedDuration>         | false    |
    And the rental cost calculation response only contains the breakdown with the following data
      | equipmentType | tariffId | tariffName    | pricingType | itemCost | billedDuration   | overtimeMinutes | forgivenMinutes | pattern   | message   |
      | BICYCLE       | 3        | Daily Bicycle | DAILY       | <cost>   | <billedDuration> | <overtime>      | <forgiven>      | <pattern> | <message> |
    Examples:
      | returnAt         | billedDuration | cost  | subtotal | total | discountPercent | discountAmount | overtime | forgiven | message               | pattern                       |
      | 2026-06-01T05:05 | 305            | 25    | 25       | 25    | 0               | 0              | 0        |          | 1d = 25               | breakdown.cost.daily.standard |
      | 2026-06-01T05:15 | 310            | 25.0  | 25.0     | 25.0  | 0               | 0              | 5        | 5        | 1d = 25               | breakdown.cost.daily.standard |
      | 2026-06-02T00:00 | 1440           | 25.0  | 25.0     | 25.0  | 0               | 0              | 1130     | 0        | 1d = 25               | breakdown.cost.daily.standard |
      | 2026-06-02T01:00 | 1500           | 26.0  | 26.0     | 26.0  | 0               | 0              | 1190     | 0        | 1d + 1h 0min = 26     | breakdown.cost.daily.overtime |
      | 2026-06-02T01:35 | 1535           | 26.56 | 26.56    | 26.56 | 0               | 0              | 1225     | 0        | 1d + 1h 35min = 26.56 | breakdown.cost.daily.overtime |
      | 2026-06-03T00:00 | 2880           | 50    | 50       | 50    | 0               | 0              | 2570     | 0        | 2d = 50               | breakdown.cost.daily.standard |
      | 2026-06-03T00:10 | 2890           | 50.16 | 50.16    | 50.16 | 0               | 0              | 2580     | 0        | 2d + 0h 10min = 50.16 | breakdown.cost.daily.overtime |
      | 2026-06-01T05:05 | 305            | 25    | 25       | 22.5  | 10              | 2.5            | 0        | 0        | 1d = 25               | breakdown.cost.daily.standard |


  Scenario Outline: V2 parity - FLAT_FEE returned at <returnAt> discount <discountPercent>
    Given the equipment items for cost calculation request are
      | equipmentId | equipmentType | returnAt   |
      | 1           | HELMET        | <returnAt> |
    And the rental cost calculation request is prepared with the following data
      | startAt          | plannedDurationMinutes | discountPercent   |
      | 2026-06-01T00:00 | 60                     | <discountPercent> |
    When a PUT request has been made to "/api/tariffs/calculations" endpoint
    Then the response status is 200
    And the rental cost calculation response only contains
      | totalCost | subtotal   | discountAmount   | discountPercent   | effectiveDurationMinutes | estimate |
      | <total>   | <subtotal> | <discountAmount> | <discountPercent> | <billedDuration>         | false    |
    And the rental cost calculation response only contains the breakdown with the following data
      | equipmentType | tariffId | tariffName      | pricingType | itemCost | billedDuration   | overtimeMinutes | forgivenMinutes | pattern   | message   |
      | HELMET        | 4        | Flat Fee Helmet | FLAT_FEE    | <cost>   | <billedDuration> | <overtime>      | <forgiven>      | <pattern> | <message> |
    Examples:
      | returnAt         | billedDuration | cost | subtotal | total | discountPercent | discountAmount | overtime | forgiven | message            | pattern                 |
      | 2026-06-01T00:05 | 5              | 0    | 0        | 0     | 0               | 0              | 0        |          | Early return within 10min: free = 0 | breakdown.cost.early_return_free |
      | 2026-06-01T00:10 | 10             | 0    | 0        | 0     | 0               | 0              | 0        |          | Early return within 10min: free = 0 | breakdown.cost.early_return_free |
      | 2026-06-01T01:00 | 60             | 1    | 1        | 1     | 0               | 0              | 0        |          | Flat fee: 1*1d = 1 | breakdown.cost.flat_fee |
      | 2026-06-01T01:07 | 60             | 1    | 1        | 1     | 0               | 0              | 7        |          | Flat fee: 1*1d = 1 | breakdown.cost.flat_fee |
      | 2026-06-02T00:00 | 1440           | 2    | 2        | 2     | 0               | 0              | 1380     |          | Flat fee: 1*2d = 2 | breakdown.cost.flat_fee |
      | 2026-06-02T00:01 | 1441           | 2    | 2        | 2     | 0               | 0              | 1381     |          | Flat fee: 1*2d = 2 | breakdown.cost.flat_fee |
      | 2026-06-01T05:05 | 305            | 1    | 1        | 0.9   | 10              | 0.1            | 245      | 0        | Flat fee: 1*1d = 1 | breakdown.cost.flat_fee |


  Scenario: V2 parity - multiple equipments estimate mode
    Given the equipment items for cost calculation request are
      | equipmentId | equipmentType | returnAt |
      | 1           | BICYCLE       |          |
      | 2           | HELMET        |          |
      | 3           | BICYCLE       |          |
    And the rental cost calculation request is prepared with the following data
      | startAt          | plannedDurationMinutes |
      | 2026-06-01T00:00 | 60                     |
    When a PUT request has been made to "/api/tariffs/calculations" endpoint
    Then the response status is 200
    And the rental cost calculation response only contains
      | totalCost | subtotal | effectiveDurationMinutes | estimate |
      | 19        | 19       | 60                       | true     |
    And the rental cost calculation response only contains the breakdown with the following data
      | equipmentType | tariffId | tariffName      | pricingType       | itemCost | billedDuration |
      | BICYCLE       | 1        | Hourly Bicycle  | DEGRESSIVE_HOURLY | 9        | 60             |
      | BICYCLE       | 1        | Hourly Bicycle  | DEGRESSIVE_HOURLY | 9        | 60             |
      | HELMET        | 4        | Flat Fee Helmet | FLAT_FEE          | 1        | 60             |

