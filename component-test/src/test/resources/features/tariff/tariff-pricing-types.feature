Feature: Tariff V2 Pricing Types
  As an operator
  I want to retrieve available pricing types

  Scenario: Get pricing types list
    When a GET request has been made to "/api/tariffs/pricing-types" endpoint
    Then the response status is 200
    And the response list at "$" has size 5

