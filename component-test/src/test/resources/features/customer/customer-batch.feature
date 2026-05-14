Feature: Batch customer fetch by UUIDs
  As a frontend client
  I want to fetch multiple customer records in a single request
  So that rendering the rental list does not require N individual API calls

  Background:
    Given customers exist in the database with the following data
      | id   | phone        | firstName | lastName | email            | birthDate  | comments |
      | CUS1 | +79995551111 | Alex      | Johnson  | null             | null       | null     |
      | CUS2 | +79991232222 | John      | Doe      | john@example.com | 1922-02-22 | null     |
      | CUS3 | +79998883333 | Maria     | Garcia   | maria@test.com   | 1983-03-03 | Sexy     |

  Scenario: Batch fetch returns all matching customers when all UUIDs exist
    When a GET request has been made to "/api/customers/batch" endpoint with query parameters
      | ids                                                                       |
      | 11111111-1111-1111-1111-111111111111,11111111-1111-1111-1111-111111111112 |
    Then the response status is 200
    And the batch customer response contains
      | id   | phone        | firstName | lastName | email            | birthDate  | comments |
      | CUS1 | +79995551111 | Alex      | Johnson  | null             | null       | null     |
      | CUS2 | +79991232222 | John      | Doe      | john@example.com | 1922-02-22 | null     |

  Scenario: Batch fetch silently omits non-existent UUIDs
    When a GET request has been made to "/api/customers/batch" endpoint with query parameters
      | ids                                                                       |
      | 11111111-1111-1111-1111-111111111111,00000000-0000-0000-0000-000000000099 |
    Then the response status is 200
    And the batch customer response contains
      | id   | phone        | firstName | lastName | email | birthDate | comments |
      | CUS1 | +79995551111 | Alex      | Johnson  | null  | null      | null     |

  Scenario: Batch fetch returns empty list when no UUIDs match any record
    When a GET request has been made to "/api/customers/batch" endpoint with query parameters
      | ids                                  |
      | 00000000-0000-0000-0000-000000000098 |
    Then the response status is 200
    And the batch customer response is empty