Feature: Identity self-service
  As an authenticated user
  I want to view my profile and change my own password
  So that I can manage my own credentials

  Background:
    Given user accounts exist in the database with the following data
      | id   | username | email         | password    | roles    | status | mustChangePassword |
      | USR1 | j.doe    | j@example.com | Secret12345 | OPERATOR | ACTIVE | true               |

  Scenario: User retrieves own profile
    Given the user "j.doe" is authenticated
    When a GET request has been made to "/api/auth/me" endpoint
    Then the response status is 200
    And the response matches the user
      | id   | username | email         | status | mustChangePassword | roles    |
      | USR1 | j.doe    | j@example.com | ACTIVE | true               | OPERATOR |

  Scenario: User changes own password successfully
    Given the user "j.doe" is authenticated
    And a change password request with the following data
      | currentPassword | newPassword   |
      | Secret12345     | BrandNew12345 |
    When a POST request has been made to "/api/auth/password" endpoint
    Then the response status is 204

  Scenario: Changing password with an incorrect current password is rejected
    Given the user "j.doe" is authenticated
    And a change password request with the following data
      | currentPassword | newPassword   |
      | WrongPass999    | BrandNew12345 |
    When a POST request has been made to "/api/auth/password" endpoint
    Then the response status is 422
    And the response contains
      | path        | value                             |
      | $.errorCode | identity.password.invalid_current |
