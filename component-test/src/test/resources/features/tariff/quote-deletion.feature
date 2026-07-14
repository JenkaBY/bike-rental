Feature: Cost Quote Deletion
  As an operator
  I want to delete a cost quote by its id
  So that a quote I no longer need does not stay around until it expires on its own

  Background:
    Given the following equipment types exist in the database
      | slug    | name    | description |
      | BICYCLE | Bicycle | Two-wheeled |
    And the pricing params list for tariff request is
      | tariffId | pricingType       | firstHourPrice | hourlyDiscount | minimumHourlyPrice | minimumDurationMinutes | minimumDurationSurcharge |
      | 1        | DEGRESSIVE_HOURLY | 9.00           | 2.00           | 1.00               | 30                     | 1.00                     |
    And the following tariff v2 records exist in db
      | id | name           | description       | equipmentType | pricingType       | status | validFrom  | validTo |
      | 1  | Hourly Bicycle | Degressive hourly | BICYCLE       | DEGRESSIVE_HOURLY | ACTIVE | 2026-01-01 |         |

  Scenario: Deleting an existing cost quote succeeds
    Given the equipment items for cost calculation request are
      | equipmentId | equipmentType | returnAt            |
      | 1           | BICYCLE       | 2026-02-10T09:30:00 |
    And the rental cost calculation request is prepared with the following data
      | startAt             | plannedDurationMinutes |
      | 2026-02-10T08:30:00 | 60                     |
    When a POST request has been made to "/api/tariffs/quotes" endpoint
    Then the response status is 201
    And the cost quote response contains
      | totalCost | subtotal | effectiveDurationMinutes | estimate |
      | 9.00      | 9.00     | 60                       | false    |
    When a DELETE request has been made to "/api/tariffs/quotes/{requestedObjectId}" endpoint with context
    Then the response status is 204
    When a DELETE request has been made to "/api/tariffs/quotes/{requestedObjectId}" endpoint with context
    Then the response status is 404
    And the response contains
      | path        | value                  |
      | $.errorCode | tariff.quote.not_found |

  Scenario: Deleting a non-existent cost quote returns 404
    When a DELETE request has been made to "/api/tariffs/quotes/11111111-1111-1111-1111-111111111111" endpoint
    Then the response status is 404
    And the response contains
      | path        | value                  |
      | $.errorCode | tariff.quote.not_found |
