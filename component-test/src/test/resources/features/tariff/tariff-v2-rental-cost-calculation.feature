Feature: Tariff V2 API
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
    And the pricing params list for tariff request is
      | tariffId | pricingType       | firstHourPrice | hourlyDiscount | minimumHourlyPrice | hourlyPrice | dailyPrice | overtimeHourlyPrice | issuanceFee | minimumDurationMinutes | minimumDurationSurcharge | price |
      | 1        | DEGRESSIVE_HOURLY | 9.00           | 2.00           | 1.00               |             |            |                     |             | 30                     | 1.00                     |       |
      | 2        | FLAT_HOURLY       |                |                |                    | 15.00       |            |                     |             | 30                     | 1.00                     |       |
      | 3        | DAILY             |                |                |                    |             | 25.00      | 1.00                |             |                        |                          |       |
      | 4        | FLAT_FEE          |                |                |                    |             |            |                     | 1.00        |                        |                          |       |
      | 5        | SPECIAL           |                |                |                    |             |            |                     |             |                        |                          | 0     |
    And the following tariff v2 records exist in db
      | id | name                | description             | equipmentType | pricingType       | status | validFrom  | validTo |
      | 1  | Hourly Bicycle      | Degressive hourly       | BICYCLE       | DEGRESSIVE_HOURLY | ACTIVE | 2026-01-01 |         |
      | 2  | Flat Hourly Scooter | Flat hourly             | SCOOTER       | FLAT_HOURLY       | ACTIVE | 2026-01-01 |         |
      | 3  | Daily Bicycle       | Daily hourly            | BICYCLE       | DAILY             | ACTIVE | 2026-01-01 |         |
      | 4  | Flat Fee Helmet     | Flat fee                | HELMET        | FLAT_FEE          | ACTIVE | 2026-01-01 |         |
      | 5  | Special Tariff      | Apply for any equipment | ANY           | SPECIAL           | ACTIVE | 2025-01-31 |         |


  Scenario Outline: Get rental cost calculation for a single rental - Template
    Given the rental request is prepared with the following data
      | equipmentTypes  | plannedDurationMinutes | actualDurationMinutes | discountPercent   | specialTariffId   | specialPrice   | rentalDate   |
      | <equipmentType> | <durationMinutes>      | <durationMinutes>     | <discountPercent> | <specialTariffId> | <specialPrice> | <rentalDate> |
    When a POST request has been made to "/api/tariffs/calculate" endpoint
    Then the response status is 200
    And the rental cost calculation response only contains
      | totalCost  | subtotal | discountAmount   | discountPercent   | effectiveDurationMinutes | estimate   | specialPricingApplied |
      | <subtotal> | <total>  | <discountAmount> | <discountPercent> | <durationMinutes>        | <estimate> | <special>             |
    And the rental cost calculation response only contains the breakdown with the following data
      | equipmentType   | tariffId   | tariffName   | pricingType   | itemCost | billedDuration   | overtimeMinutes | forgivenMinutes | pattern   | message   |
      | <equipmentType> | <tariffId> | <tariffName> | <pricingType> | <cost>   | <billedDuration> | <overtime>      | <forgiven>      | <pattern> | <message> |
    Examples:
      | pricingType       | equipmentType | durationMinutes | billedDuration | cost  | subtotal | total | discountPercent | discountAmount | specialTariffId | specialPrice | rentalDate | tariffId | tariffName          | overtime | forgiven | message                           | pattern                                   | special | estimate |
      | DEGRESSIVE_HOURLY | BICYCLE       | 60              | 60             | 9.00  | 9.00     | 9.00  | 0               | 0              |                 |              |            | 1        | Hourly Bicycle      |          |          | 1h 0min degressive: 9 = 9         | breakdown.cost.degressive_hourly.standard | false   | false    |
      | FLAT_HOURLY       | SCOOTER       | 60              | 60             | 15.00 | 15.00    | 15.00 | 0               | 0              |                 |              |            | 2        | Flat Hourly Scooter |          |          | 1h 0min flat: 1*15 + partial = 15 | breakdown.cost.flat_hourly.standard       | false   | false    |
      | DAILY             | BICYCLE       | 480             | 480            | 25.00 | 25.00    | 25.00 | 0               | 0              |                 |              |            | 3        | Daily Bicycle       |          |          | 1d = 25                           | breakdown.cost.daily.standard             | false   | false    |
      | FLAT_FEE          | HELMET        | 60              | 60             | 1     | 1        | 1     | 0               | 0              |                 |              |            | 4        | Flat Fee Helmet     |          |          | Flat fee: 1*1d = 1                | breakdown.cost.flat_fee                   | false   | false    |
      | SPECIAL           | ANY           | 60              | 60             | 0     | 666      | 666   | 0               | 0              | 5               | 666          |            | 5        | Special Tariff      |          |          | Special tariff applied to group   | breakdown.cost.special.group              | true    | false    |


  Scenario Outline: Get rental cost calculation for a single rental - DEGRESSIVE_HOURLY:<durationMinutes>min
    Given the rental request is prepared with the following data
      | equipmentTypes | plannedDurationMinutes | actualDurationMinutes | discountPercent   |
      | BICYCLE        | 60                     | <durationMinutes>     | <discountPercent> |
    When a POST request has been made to "/api/tariffs/calculate" endpoint
    Then the response status is 200
    And the rental cost calculation response only contains
      | totalCost | subtotal   | discountAmount   | discountPercent   | effectiveDurationMinutes | estimate   |
      | <total>   | <subtotal> | <discountAmount> | <discountPercent> | <durationMinutes>        | <estimate> |
    And the rental cost calculation response only contains the breakdown with the following data
      | equipmentType | tariffId | tariffName     | pricingType       | itemCost | billedDuration   | overtimeMinutes | forgivenMinutes | pattern   | message   |
      | BICYCLE       | 1        | Hourly Bicycle | DEGRESSIVE_HOURLY | <cost>   | <billedDuration> | <overtime>      | <forgiven>      | <pattern> | <message> |
    Examples:
      | durationMinutes | billedDuration | cost  | subtotal | total | discountPercent | discountAmount | overtime | forgiven | message                                     | pattern                                   | estimate |
      | 5               | 5              | 5.5   | 5.5      | 5.5   | 0               | 0              |          |          | 30min minimum: 9/2 + 1 = 5.5                | breakdown.cost.degressive_hourly.minimum  | false    |
      | 20              | 20             | 5.5   | 5.5      | 5.5   | 0               | 0              |          |          | 30min minimum: 9/2 + 1 = 5.5                | breakdown.cost.degressive_hourly.minimum  | false    |
      | 60              | 60             | 9.00  | 9.00     | 9.00  | 0               | 0              |          |          | 1h 0min degressive: 9 = 9                   | breakdown.cost.degressive_hourly.standard | false    |
      | 65              | 60             | 9.00  | 9.00     | 9.0   | 0               | 0              | 5        | 5        | 1h 0min degressive: 9 = 9                   | breakdown.cost.degressive_hourly.standard | false    |
      | 68              | 68             | 9.58  | 9.58     | 9.58  | 0               | 0              | 8        | 0        | 1h 8min degressive: 9+1*(7/12) = 9.58       | breakdown.cost.degressive_hourly.standard | false    |
      | 70              | 70             | 10.16 | 10.16    | 10.16 | 0               | 0              | 10       | 0        | 1h 10min degressive: 9+2*(7/12) = 10.16     | breakdown.cost.degressive_hourly.standard | false    |
      | 120             | 120            | 16    | 16       | 16    | 0               | 0              | 60       | 0        | 2h 0min degressive: 9+7 = 16                | breakdown.cost.degressive_hourly.standard | false    |
      | 180             | 180            | 21    | 21       | 21    | 0               | 0              | 120      | 0        | 3h 0min degressive: 9+7+5 = 21              | breakdown.cost.degressive_hourly.standard | false    |
      | 240             | 240            | 24    | 24       | 24    | 0               | 0              | 180      | 0        | 4h 0min degressive: 9+7+5+3 = 24            | breakdown.cost.degressive_hourly.standard | false    |
      | 300             | 300            | 25    | 25       | 25    | 0               | 0              | 240      | 0        | 5h 0min degressive: 9+7+5+3+1 = 25          | breakdown.cost.degressive_hourly.standard | false    |
      | 304             | 304            | 25    | 25       | 25    | 0               | 0              | 244      | 0        | 5h 4min degressive: 9+7+5+3+1+0*(1/12) = 25 | breakdown.cost.degressive_hourly.standard | false    |
      # starts the next DAILY tariff
      # | 360             | 360            | 25    | 25       | 25    | 0               | 0              | 300      | 0        | 24h daily: 25.0                                | breakdown.cost.daily.standard             | false    |
      #
      #     Discount applied
      | 60              | 60             | 9     | 9.0      | 8.1   | 10              | 0.9            | 0        | 0        | 1h 0min degressive: 9 = 9                   | breakdown.cost.degressive_hourly.standard | false    |
      | 60              | 60             | 9     | 9.0      | 0     | 100             | 9              | 0        | 0        | 1h 0min degressive: 9 = 9                   | breakdown.cost.degressive_hourly.standard | false    |
      | 300             | 300            | 25    | 25       | 12.5  | 50              | 12.5           | 240      | 0        | 5h 0min degressive: 9+7+5+3+1 = 25          | breakdown.cost.degressive_hourly.standard | false    |


  Scenario: Multiple rental equipments calculation
    Given the rental request is prepared with the following data
      | equipmentTypes         | plannedDurationMinutes |
      | BICYCLE,HELMET,BICYCLE | 60                     |
    When a POST request has been made to "/api/tariffs/calculate" endpoint
    Then the response status is 200
    And the rental cost calculation response only contains
      | totalCost | subtotal | effectiveDurationMinutes | estimate |
      | 19        | 19       | 60                       | true     |
    And the rental cost calculation response only contains the breakdown with the following data
      | equipmentType | tariffId | tariffName      | pricingType       | itemCost | billedDuration |
      | BICYCLE       | 1        | Hourly Bicycle  | DEGRESSIVE_HOURLY | 9        | 60             |
      | BICYCLE       | 1        | Hourly Bicycle  | DEGRESSIVE_HOURLY | 9        | 60             |
      | HELMET        | 4        | Flat Fee Helmet | FLAT_FEE          | 1        | 60             |

  Scenario Outline: Get rental cost calculation for a single rental - FLAT_HOURLY:<durationMinutes>min
    Given the rental request is prepared with the following data
      | equipmentTypes | plannedDurationMinutes | actualDurationMinutes | discountPercent   |
      | SCOOTER        | 60                     | <durationMinutes>     | <discountPercent> |
    When a POST request has been made to "/api/tariffs/calculate" endpoint
    Then the response status is 200
    And the rental cost calculation response only contains
      | totalCost | subtotal   | discountAmount   | discountPercent   | effectiveDurationMinutes | estimate |
      | <total>   | <subtotal> | <discountAmount> | <discountPercent> | <durationMinutes>        | false    |
    And the rental cost calculation response only contains the breakdown with the following data
      | equipmentType | tariffId | tariffName          | pricingType | itemCost | billedDuration   | overtimeMinutes | forgivenMinutes | pattern   | message   |
      | SCOOTER       | 2        | Flat Hourly Scooter | FLAT_HOURLY | <cost>   | <billedDuration> | <overtime>      | <forgiven>      | <pattern> | <message> |
    Examples:
      | durationMinutes | billedDuration | cost  | subtotal | total | discountPercent | discountAmount | overtime | forgiven | message                               | pattern                             |
      | 5               | 5              | 8.5   | 8.5      | 8.5   | 0               | 0              |          |          | 30min minimum: 15/2 + 1 = 8.5         | breakdown.cost.flat_hourly.minimum  |
      | 20              | 20             | 8.5   | 8.5      | 8.5   | 0               | 0              |          |          | 30min minimum: 15/2 + 1 = 8.5         | breakdown.cost.flat_hourly.minimum  |
      | 60              | 60             | 15    | 15       | 15    | 0               | 0              |          |          | 1h 0min flat: 1*15 + partial = 15     | breakdown.cost.flat_hourly.standard |
      | 65              | 60             | 15    | 15       | 15    | 0               | 0              | 5        | 5        | 1h 0min flat: 1*15 + partial = 15     | breakdown.cost.flat_hourly.standard |
      | 68              | 68             | 16.25 | 16.25    | 16.25 | 0               | 0              | 8        | 0        | 1h 8min flat: 1*15 + partial = 16.25  | breakdown.cost.flat_hourly.standard |
      | 120             | 120            | 30    | 30       | 30    | 0               | 0              | 60       | 0        | 2h 0min flat: 2*15 + partial = 30     | breakdown.cost.flat_hourly.standard |
      | 300             | 300            | 75    | 75       | 75    | 0               | 0              | 240      | 0        | 5h 0min flat: 5*15 + partial = 75     | breakdown.cost.flat_hourly.standard |
      | 304             | 304            | 75    | 75       | 75    | 0               | 0              | 244      | 0        | 5h 4min flat: 5*15 + partial = 75     | breakdown.cost.flat_hourly.standard |
      | 315             | 315            | 78.75 | 78.75    | 78.75 | 0               | 0              | 255      | 0        | 5h 15min flat: 5*15 + partial = 78.75 | breakdown.cost.flat_hourly.standard |
      #     Discount applied
      | 60              | 60             | 15    | 15       | 13.5  | 10              | 1.5            | 0        | 0        | 1h 0min flat: 1*15 + partial = 15     | breakdown.cost.flat_hourly.standard |
      | 300             | 300            | 75.0  | 75.0     | 37.50 | 50              | 37.50          | 240      | 0        | 5h 0min flat: 5*15 + partial = 75     | breakdown.cost.flat_hourly.standard |

  Scenario Outline: Get rental cost calculation for a single rental - DAILY:<durationMinutes>min
    Given the rental request is prepared with the following data
      | equipmentTypes | plannedDurationMinutes | actualDurationMinutes | discountPercent   |
      | BICYCLE        | 310                    | <durationMinutes>     | <discountPercent> |
    When a POST request has been made to "/api/tariffs/calculate" endpoint
    Then the response status is 200
    And the rental cost calculation response only contains
      | totalCost | subtotal   | discountAmount   | discountPercent   | effectiveDurationMinutes | estimate |
      | <total>   | <subtotal> | <discountAmount> | <discountPercent> | <durationMinutes>        | false    |
    And the rental cost calculation response only contains the breakdown with the following data
      | equipmentType | tariffId | tariffName    | pricingType | itemCost | billedDuration   | overtimeMinutes | forgivenMinutes | pattern   | message   |
      | BICYCLE       | 3        | Daily Bicycle | DAILY       | <cost>   | <billedDuration> | <overtime>      | <forgiven>      | <pattern> | <message> |
    Examples:
      | durationMinutes | billedDuration | cost  | subtotal | total | discountPercent | discountAmount | overtime | forgiven | message               | pattern                       |
      | 305             | 305            | 25    | 25       | 25    | 0               | 0              | 0        |          | 1d = 25               | breakdown.cost.daily.standard |
      | 315             | 310            | 25.0  | 25.0     | 25.0  | 0               | 0              | 5        | 5        | 1d = 25               | breakdown.cost.daily.standard |
      | 1440            | 1440           | 25.0  | 25.0     | 25.0  | 0               | 0              | 1130     | 0        | 1d = 25               | breakdown.cost.daily.standard |
      | 1500            | 1500           | 26.0  | 26.0     | 26.0  | 0               | 0              | 1190     | 0        | 1d + 1h 0min = 26     | breakdown.cost.daily.overtime |
      | 1535            | 1535           | 26.56 | 26.56    | 26.56 | 0               | 0              | 1225     | 0        | 1d + 1h 35min = 26.56 | breakdown.cost.daily.overtime |
      | 2880            | 2880           | 50    | 50       | 50    | 0               | 0              | 2570     | 0        | 2d = 50               | breakdown.cost.daily.standard |
      | 2890            | 2890           | 50.16 | 50.16    | 50.16 | 0               | 0              | 2580     | 0        | 2d + 0h 10min = 50.16 | breakdown.cost.daily.overtime |
      #     Discount applied
      | 305             | 305            | 25    | 25       | 22.5  | 10              | 2.5            | 0        | 0        | 1d = 25               | breakdown.cost.daily.standard |

  Scenario Outline: Get rental cost calculation for a single rental - FLAT_FEE:<durationMinutes>min
    Given the rental request is prepared with the following data
      | equipmentTypes | plannedDurationMinutes | actualDurationMinutes | discountPercent   |
      | HELMET         | 60                     | <durationMinutes>     | <discountPercent> |
    When a POST request has been made to "/api/tariffs/calculate" endpoint
    Then the response status is 200
    And the rental cost calculation response only contains
      | totalCost | subtotal   | discountAmount   | discountPercent   | effectiveDurationMinutes | estimate |
      | <total>   | <subtotal> | <discountAmount> | <discountPercent> | <durationMinutes>        | false    |
    And the rental cost calculation response only contains the breakdown with the following data
      | equipmentType | tariffId | tariffName      | pricingType | itemCost | billedDuration   | overtimeMinutes | forgivenMinutes | pattern   | message   |
      | HELMET        | 4        | Flat Fee Helmet | FLAT_FEE    | <cost>   | <billedDuration> | <overtime>      | <forgiven>      | <pattern> | <message> |
    Examples:
      | durationMinutes | billedDuration | cost | subtotal | total | discountPercent | discountAmount | overtime | forgiven | message            | pattern                 |
      | 5               | 5              | 1    | 1        | 1     | 0               | 0              | 0        |          | Flat fee: 1*1d = 1 | breakdown.cost.flat_fee |
      | 10              | 10             | 1    | 1        | 1     | 0               | 0              | 0        |          | Flat fee: 1*1d = 1 | breakdown.cost.flat_fee |
      | 60              | 60             | 1    | 1        | 1     | 0               | 0              | 0        |          | Flat fee: 1*1d = 1 | breakdown.cost.flat_fee |
      | 67              | 60             | 1    | 1        | 1     | 0               | 0              | 7        |          | Flat fee: 1*1d = 1 | breakdown.cost.flat_fee |
      | 1440            | 1440           | 1    | 1        | 1     | 0               | 0              | 1380     |          | Flat fee: 1*1d = 1 | breakdown.cost.flat_fee |
      | 1441            | 1441           | 2    | 2        | 2     | 0               | 0              | 1381     |          | Flat fee: 1*2d = 2 | breakdown.cost.flat_fee |
      #     Discount applied
      | 305             | 305            | 1    | 1        | 0.9   | 10              | 0.1            | 245      | 0        | Flat fee: 1*1d = 1 | breakdown.cost.flat_fee |

