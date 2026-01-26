Feature: Quick Customer Creation (US-CL-002)
  As an operator
  I want to quickly create a customer profile with only a phone number
  So that I can proceed with rental operations without delay

  Background:
    Given the database is empty for "customers" table

  Scenario: Create customer with phone number only
    Given a prepared payload is
      """
      {
        "phone": "+79991234567",
        "firstName": "John",
        "lastName": "Doe"
      }
      """
    And the request header "Content-Type" is "application/vnd.bikerent.v1+json"
    When a POST request has been made to "/api/customers" endpoint
    Then the response status is 201
    And the response contains
      | path        | value        |
      | $.phone     | +79991234567 |
      | $.firstName | John         |
      | $.lastName  | Doe          |
    And the response contains a UUID at "$.id"

  Scenario: Create customer with phone normalization
    Given a prepared payload is
      """
      {
        "phone": "+7 (999) 123-45-67",
        "firstName": "Jane",
        "lastName": "Smith"
      }
      """
    And the request header "Content-Type" is "application/vnd.bikerent.v1+json"
    When a POST request has been made to "/api/customers" endpoint
    Then the response status is 201
    And the response contains
      | path        | value        |
      | $.phone     | +79991234567 |
      | $.firstName | Jane         |
      | $.lastName  | Smith        |

  Scenario: Create customer with minimal data (phone only via firstName/lastName)
    Given a prepared payload is
      """
      {
        "phone": "+79998887766",
        "firstName": "Guest",
        "lastName": "User"
      }
      """
    And the request header "Content-Type" is "application/vnd.bikerent.v1+json"
    When a POST request has been made to "/api/customers" endpoint
    Then the response status is 201
    And the response contains
      | path    | value        |
      | $.phone | +79998887766 |

  Scenario: Fail to create customer with duplicate phone
    Given a prepared payload is
      """
      {
        "phone": "+79995551122",
        "firstName": "First",
        "lastName": "Customer"
      }
      """
    And the request header "Content-Type" is "application/vnd.bikerent.v1+json"
    When a POST request has been made to "/api/customers" endpoint
    Then the response status is 201
    Given a prepared payload is
      """
      {
        "phone": "+79995551122",
        "firstName": "Second",
        "lastName": "Customer"
      }
      """
    When a POST request has been made to "/api/customers" endpoint
    Then the response status is 409


  Scenario: Customer creation time is under 2 seconds
    Given a prepared payload is
      """
      {
        "phone": "+79994445566",
        "firstName": "Fast",
        "lastName": "Creation"
      }
      """
    And the request header "Content-Type" is "application/vnd.bikerent.v1+json"
    When a POST request has been made to "/api/customers" endpoint
    Then the response status is 201
    And the response time is less than 2000 milliseconds
