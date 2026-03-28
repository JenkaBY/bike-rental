Feature: Customer Finance Account Creation
  As a system
  I want to automatically create a finance account for every new customer upon registration
  So that the customer can immediately make deposits and participate in rental transactions

  Background:
    Given the request header "Content-Type" is "application/vnd.bikerental.v1+json"

  Scenario: New customer registration creates a finance account with zero-balance sub-ledgers
    Given a customer request with the following data
      | phone        | firstName | lastName |
      | +3701234567 | Ivan      | Petrov   |
    When a POST request has been made to "/api/customers" endpoint
    Then the response status is 201
    And a customer finance account is created in db for the registered customer