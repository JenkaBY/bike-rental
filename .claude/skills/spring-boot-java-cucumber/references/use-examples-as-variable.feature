Feature: Demonstrate use of Examples as Variables

# usage of Examples table values as variables in scenario steps
  Scenario Outline: Get user profile by recipient ID
    When a GET request has been made to '/bff/users/<recipientId>/profile' endpoint
    And the response contains
      | path                | value         |
      | $.userPrincipalName | <recipientId> |
      | $.displayName       | Test User     |
      | $.mail              | <recipientId> |
    Examples:
      | recipientId         |
      | testuser@domain.com |