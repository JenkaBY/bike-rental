Feature: Customer Profile Management
  As an operator
  I want to create and update customer profiles
  So that I can maintain accurate customer records for rental operations

  Background:
    Given the request header "Content-Type" is "application/vnd.bikerental.v1+json"
    And customers exist in the database with the following data
      | id   | phone        | firstName | lastName | email                    | birthDate  | comments |
      | CUS1 | +79995551111 | Alex      | Johnson  | null                     | null       | null     |
      | CUS2 | +79991232222 | John      | Doe      | john@example.com         | 1922-02-22 | null     |
      | CUS3 | +79998883333 | Maria     | Garcia   | maria@test.com           | 1983-03-03 | Sexy     |
      | CUS4 | +79998884444 | Anna      | Bond     | anna@test.com            | 1984-04-04 | Cool     |
      | CUS5 | +79998885555 | Normalize | Phone    | normalize.phone@test.com | null       | null     |

  # Customer Creation Scenarios

  Scenario Outline: Create customer with various valid field combinations
    Given a customer request with the following data
      | phone   | firstName   | lastName   | birthDate   | email   | comments  |
      | <phone> | <firstName> | <lastName> | <birthDate> | <email> | <comment> |
    When a POST request has been made to "/api/customers" endpoint
    Then the response status is 201
    And the response contains
      | path        | value             |
      | $.phone     | <normalizedPhone> |
      | $.firstName | <firstName>       |
      | $.lastName  | <lastName>        |
      | $.birthDate | <birthDate>       |
      | $.email     | <email>           |
      | $.comments  | <comment>         |
    And the response contains a UUID at "$.id"
    Examples:
      | phone              | firstName | lastName | email | birthDate | comment | normalizedPhone |
      | +79991234567       | John      | Doe      | null  | null      | null    | +79991234567    |
      | +7 (999) 123-45-67 | Jane      | Smith    | null  | null      | null    | +79991234567    |
#      | +7 (999) 123-45-67 | Jane      | Smith    | email@example.com | 2011-12-13 | I don't trust him | +79991234567    |

  Scenario: Fail to create customer with duplicate phone number
    Given a customer exists in the database with the following data
      | phone        | firstName | lastName |
      | +79995551122 | First     | Customer |
    And a customer request with the following data
      | phone        | firstName | lastName |
      | +79995551122 | Second    | Customer |
    When a POST request has been made to "/api/customers" endpoint
    Then the response status is 409

  # Customer Update Scenarios
  Scenario Outline: Successfully update customer with various field combinations
    Given a customer update request with the following data
      | phone   | firstName   | lastName   | email   | birthDate   | comments   |
      | <phone> | <firstName> | <lastName> | <email> | <birthDate> | <comments> |
    When a PUT request has been made to "/api/customers/{customerId}" endpoint with
      | {customerId} |
      | <id>         |
    Then the response status is 200
    And the response matches expected customer
      | id   | phone             | firstName   | lastName   | email   | birthDate   | comments   |
      | <id> | <normalizedPhone> | <firstName> | <lastName> | <email> | <birthDate> | <comments> |
    Examples:
      | id   | phone             | normalizedPhone | firstName  | lastName | email                    | birthDate  | comments                          |
      | CUS1 | +79997654321      | +79997654321    | Jane       | Smith    | jane.smith@domain.com    | 1985-05-20 | VIP customer                      |
      | CUS2 | +79995551122      | +79995551122    | Alexander1 | Johnson1 | alex.j@mail.com          | 1992-03-10 | Frequent customer                 |
      | CUS3 | +79991112233      | +79991112233    | Maria      | Garcia   | maria.garcia@new.com     | 1988-07-22 | Updated contact                   |
      | CUS4 | +79998887768      | +79998887768    | James      | Cameron  | null                     | null       | Changed sex and phone             |
      | CUS5 | +7(999)-123-45-67 | +79991234567    | Normalize  | Phone    | normalize.phone@test.com | null       | Phone Number should be normalized |

  # Error Scenarios
  Scenario Outline: Fail to update customer - phone number already used by another customer
    And a customer update request with the following data
      | phone        | firstName | lastName |
      | +79998883333 | John      | Doe      |
    When a PUT request has been made to "/api/customers/{customerId}" endpoint with
      | {customerId} |
      | <id>         |
    Then the response status is 409
    And the response contains
      | path     | value                                                  |
      | $.title  | Duplicate phone number                                 |
      | $.detail | Customer with identifier '+79998883333' already exists |
    Examples:
      | id   |
      | CUS2 |


  Scenario: Fail to update customer - customer not found
    Given a customer update request with the following data
      | phone        | firstName | lastName |
      | +79991234567 | John      | Doe      |
    When a PUT request has been made to "/api/customers/999e8400-e29b-41d4-a716-446655440099" endpoint
    Then the response status is 404
    And the response contains
      | path     | value                                                                     |
      | $.title  | Not Found                                                                 |
      | $.detail | Customer with identifier '999e8400-e29b-41d4-a716-446655440099' not found |
