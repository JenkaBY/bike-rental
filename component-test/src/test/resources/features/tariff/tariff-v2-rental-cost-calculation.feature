Feature: Tariff V2 API
  As an operator
  I want to know the rental cost calculation with the pricing model
  So that I can request the cost calculation for a rental with the tariff v2

  Background:
    Given the following equipment types exist in the database
      | slug    | name    | description                  |
      | bicycle | Bicycle | Two-wheeled                  |
      | scooter | Scooter | Electric                     |
      | helmet  | Helmet  | Accessory                    |
      | special | Special | It's used for special tariff |
    And the pricing params list for tariff request is
      | tariffId | pricingType       | firstHourPrice | hourlyDiscount | minimumHourlyPrice | hourlyPrice | dailyPrice | overtimeHourlyPrice | issuanceFee | minimumDurationMinutes | minimumDurationSurcharge | price |
      | 1        | DEGRESSIVE_HOURLY | 9.00           | 2.00           | 1.00               |             |            |                     |             | 30                     | 1.00                     |       |
      | 2        | FLAT_HOURLY       |                |                |                    | 15.00       |            |                     |             | 30                     | 1.00                     |       |
      | 3        | DAILY             |                |                |                    |             | 25.00      | 1.00                |             |                        |                          |       |
      | 4        | FLAT_FEE          |                |                |                    |             |            |                     | 1.00        |                        |                          |       |
      | 5        | SPECIAL           |                |                |                    |             |            |                     |             |                        |                          | 0     |
    And the following tariff v2 records exist in db
      | id | name                | description             | equipmentType | pricingType       | status | validFrom  | validTo |
      | 1  | Hourly Bicycle      | Degressive hourly       | bicycle       | DEGRESSIVE_HOURLY | ACTIVE | 2026-01-01 |         |
      | 2  | Flat Hourly Scooter | Flat hourly             | scooter       | FLAT_HOURLY       | ACTIVE | 2026-01-01 |         |
      | 3  | Daily Bicycle       | Daily hourly            | bicycle       | DAILY             | ACTIVE | 2026-01-01 |         |
      | 4  | Flat Fee Helmet     | Flat fee                | helmet        | FLAT_FEE          | ACTIVE | 2026-01-01 |         |
      | 5  | Special Tariff      | Apply for any equipment | any           | SPECIAL           | ACTIVE | 2025-01-31 |         |


  Scenario Outline: Get rental cost calculation for a single rental with tariff v2 - Template
    Given the rental request is prepared with the following data
      | equipmentTypes  | plannedDurationMinutes | actualDurationMinutes | discountPercent   | specialTariffId   | specialPrice   | rentalDate   |
      | <equipmentType> | <durationMinutes>      | <durationMinutes>     | <discountPercent> | <specialTariffId> | <specialPrice> | <rentalDate> |
    When a POST request has been made to "/api/v2/tariffs/calculate" endpoint
    Then the response status is 200
    And the rental cost calculation response only contains
      | totalCost  | subtotal | discountAmount   | discountPercent   | effectiveDurationMinutes | estimate   | specialPricingApplied |
      | <subtotal> | <total>  | <discountAmount> | <discountPercent> | <durationMinutes>        | <estimate> | <special>             |
    And the rental cost calculation response only contains the breakdown with the following data
      | equipmentType   | tariffId   | tariffName   | pricingType   | itemCost | billedDuration    | overtimeMinutes | forgivenMinutes | pattern   | message   |
      | <equipmentType> | <tariffId> | <tariffName> | <pricingType> | <cost>   | <durationMinutes> | <overtime>      | <forgiven>      | <pattern> | <message> |
    Examples:
      | pricingType       | equipmentType | durationMinutes | cost  | subtotal | total | discountPercent | discountAmount | specialTariffId | specialPrice | rentalDate | tariffId | tariffName          | overtime | forgiven | message                               | pattern                                   | special | estimate |
      | DEGRESSIVE_HOURLY | bicycle       | 60              | 9.00  | 9.00     | 9.00  | 0               | 0              |                 |              |            | 1        | Hourly Bicycle      |          |          | 1h 0min degressive: 9.0 = 9.0         | breakdown.cost.degressive_hourly.standard | false   | false    |
      | FLAT_HOURLY       | scooter       | 60              | 15.00 | 15.00    | 15.00 | 0               | 0              |                 |              |            | 2        | Flat Hourly Scooter |          |          | 1h 0min flat: 1*15.0 + partial = 15.0 | breakdown.cost.flat_hourly.standard       | false   | false    |
      | DAILY             | bicycle       | 480             | 25.00 | 25.00    | 25.00 | 0               | 0              |                 |              |            | 3        | Daily Bicycle       |          |          | 24h daily: 25.0                       | breakdown.cost.daily.standard             | false   | false    |
      | FLAT_FEE          | helmet        | 60              | 1     | 1        | 1     | 0               | 0              |                 |              |            | 4        | Flat Fee Helmet     |          |          | Flat fee: 1.0 * 1 day(s) = 1.0        | breakdown.cost.flat_fee                   | false   | false    |
      | SPECIAL           | any           | 60              | 0     | 666      | 666   | 0               | 0              | 5               | 666          |            | 5        | Special Tariff      |          |          | Special tariff applied to group       | breakdown.cost.special.group              | true    | false    |

