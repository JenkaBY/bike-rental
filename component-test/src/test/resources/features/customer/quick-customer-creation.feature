Feature: Quick Customer Creation
  As an operator
  I want to quickly create a customer profile with only a phone number
  So that I can proceed with rental operations without delay

  Background:
    Given the database is empty for "customers" table
    And the request header "Content-Type" is "application/vnd.bikerental.v1+json"

  Scenario Outline: Create customer with various request valid fields
    Given a customer request with the following data
      | phone   | firstName   | lastName   | birthDate   | email   |
      | <phone> | <firstName> | <lastName> | <birthDate> | <email> |
    When a POST request has been made to "/api/customers" endpoint
    Then the response status is 201
    And the response contains
      | path        | value             |
      | $.phone     | <normalizedPhone> |
      | $.firstName | <firstName>       |
      | $.lastName  | <lastName>        |
      | $.birthDate | <birthDate>       |
      | $.email     | <email>           |
    And the response contains a UUID at "$.id"
    Examples:
      | phone              | firstName | lastName | email             | birthDate  | normalizedPhone |
      | +79991234567       | John      | Doe      | null              | null       | +79991234567    |
      | +7 (999) 123-45-67 | Jane      | Smith    | null              | null       | +79991234567    |
      | +7 (999) 123-45-67 | Jane      | Smith    | email@example.com | 2011-12-13 | +79991234567    |

  Scenario: Fail to create customer with duplicate phone
    Given a customer exists in the database with the following data
      | phone        | firstName | lastName |
      | +79995551122 | First     | Customer |
    And a customer request with the following data
      | phone        | firstName | lastName |
      | +79995551122 | Second    | Customer |
    When a POST request has been made to "/api/customers" endpoint
    Then the response status is 409
