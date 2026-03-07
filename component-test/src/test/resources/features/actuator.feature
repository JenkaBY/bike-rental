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

  Scenario: Management endpoint preflight (allowed origin)
    Given the request header 'Origin' is 'http://localhost:4200'
    And the request header 'Access-Control-Request-Method' is 'GET'
    When a OPTIONS request has been made to "/actuator/health" endpoint
    Then the response status is 200
    And the response headers contain
      | name                        | value                 |
      | Access-Control-Allow-Origin | http://localhost:4200 |

  Scenario: Management endpoint preflight (disallowed origin)
    Given the request header 'Origin' is 'http://malicious.test'
    And the request header 'Access-Control-Request-Method' is 'GET'
    When a OPTIONS request has been made to "/actuator/health" endpoint
    Then the response status is 403
    And the response headers do not contain
      | name                        | value |
      | Access-Control-Allow-Origin |       |

  Scenario: Management endpoint GET with Origin header (allowed)
    Given the request header 'Origin' is 'http://localhost:4200'
    When a GET request has been made to "/actuator/health" endpoint
    Then the response status is 200
    And the response headers contain
      | name                        | value                 |
      | Access-Control-Allow-Origin | http://localhost:4200 |

  Scenario: Management endpoint GET with Origin header (not-allowed)
    Given the request header 'Origin' is 'http://localhost:4201'
    When a GET request has been made to "/actuator/health" endpoint
    Then the response status is 403
