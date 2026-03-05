Feature: Actuator endpoints

  Scenario: Health endpoint returns UP status
    When a GET request has been made to "/actuator/health" endpoint
    Then the response status is 200
    And the response contains
      | path     | value |
      | $.status | UP    |

  Scenario: Info endpoint returns build info
    When a GET request has been made to "/actuator/info" endpoint
    Then the response status is 200
    And the response build version matches git commit
