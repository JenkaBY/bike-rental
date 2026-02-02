Feature: Customer Search by Phone
  As an operator
  I want to search customers by partial phone digits
  So that I can quickly identify a customer during rental

  Background:
    Given the database is empty for "customers" table
    And the request header "Content-Type" is "application/vnd.bikerental.v1+json"

  Scenario: Search returns matching customers limited to 10 results
    Given customers exist in the database with the following data
      | phone        | firstName | lastName |
      | +79991234001 | Alex      | Doe      |
      | +79991234002 | Ben       | Doe      |
      | +79991234003 | Chris     | Doe      |
      | +79991234004 | Dana      | Doe      |
      | +79991234005 | Evan      | Doe      |
      | +79991234006 | Fran      | Doe      |
      | +79991234007 | Gary      | Doe      |
      | +79991234008 | Hana      | Doe      |
      | +79991234009 | Ivan      | Doe      |
      | +79991234010 | Jane      | Doe      |
      | +79991234011 | Kyle      | Doe      |
      | +79991234012 | Liam      | Doe      |
      | +70000000000 | Nora      | Doe      |
    When a GET request has been made to "/api/customers" endpoint with query parameters
      | phone |
      | 1234  |
    Then the response status is 200
    And the response list at "$" has size 10
    And the response list at "$[*].phone" contains values
      | +79991234001 |
      | +79991234005 |
      | +79991234010 |

  Scenario: Search requires at least 4 digits
    When a GET request has been made to "/api/customers" endpoint with query parameters
      | phone |
      | 123   |
    Then the response status is 400
