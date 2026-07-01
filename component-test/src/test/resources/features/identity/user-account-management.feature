@Admin
Feature: Identity user account management
  As an administrator
  I want to create and manage user accounts and their roles
  So that staff can sign in with the appropriate permissions

  Scenario: Administrator creates a new operator account
    Given a create user request with the following data
      | username | email             | displayName | roles    |
      | j.doe    | j.doe@example.com | John Doe    | OPERATOR |
    When a POST request has been made to "/api/auth/users" endpoint
    Then the response status is 201
    And the created account matches
      | username | email             | status | mustChangePassword | roles    |
      | j.doe    | j.doe@example.com | ACTIVE | true               | OPERATOR |

  Scenario Outline: Creating an account with a duplicate <field> is rejected
    Given user accounts exist in the database with the following data
      | username | email             | password    | roles    | status |
      | j.doe    | existing@mail.com | Secret12345 | OPERATOR | ACTIVE |
    And a create user request with the following data
      | username   | email   | roles    |
      | <username> | <email> | OPERATOR |
    When a POST request has been made to "/api/auth/users" endpoint
    Then the response status is 409
    And the response contains
      | path        | value       |
      | $.errorCode | <errorCode> |
    Examples:
      | field    | username | email               | errorCode                   |
      | username | j.doe    | another@example.com | identity.username.duplicate |
      | email    | fresh    | existing@mail.com   | identity.email.duplicate    |

  Scenario: Administrator resets a user password and forces a change
    Given user accounts exist in the database with the following data
      | id   | username | email         | password    | roles    | status | mustChangePassword |
      | USR2 | j.doe    | j@example.com | Secret12345 | OPERATOR | ACTIVE | false              |
    When a POST request has been made to "/api/auth/users/{id}/reset-password" endpoint with
      | {id} |
      | USR2 |
    Then the response status is 200
    And the created account matches
      | id   | username | email         | status | mustChangePassword | roles    |
      | USR2 | j.doe    | j@example.com | ACTIVE | true               | OPERATOR |

  Scenario: Administrator updates roles and disables an account
    Given user accounts exist in the database with the following data
      | id   | username | email         | password    | roles    | status |
      | USR3 | j.doe    | j@example.com | Secret12345 | OPERATOR | ACTIVE |
    And an update user request with the following data
      | roles          | status   |
      | ADMIN,OPERATOR | DISABLED |
    When a PUT request has been made to "/api/auth/users/{id}" endpoint with
      | {id} |
      | USR3 |
    Then the response status is 200
    And the response matches the user
      | id   | username | email         | status   | mustChangePassword | roles          |
      | USR3 | j.doe    | j@example.com | DISABLED | false              | ADMIN,OPERATOR |

  Scenario: Administrator deactivates an account
    Given user accounts exist in the database with the following data
      | id   | username | email         | password    | roles    | status |
      | USR4 | j.doe    | j@example.com | Secret12345 | OPERATOR | ACTIVE |
    When a POST request has been made to "/api/auth/users/{id}/deactivate" endpoint with
      | {id} |
      | USR4 |
    Then the response status is 200
    And the response matches the user
      | id   | username | email         | status   | mustChangePassword | roles    |
      | USR4 | j.doe    | j@example.com | DISABLED | false              | OPERATOR |
