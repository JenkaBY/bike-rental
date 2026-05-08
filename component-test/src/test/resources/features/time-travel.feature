Feature: Time travel virtual clock

  Background:
    Given the request header "Content-Type" is "application/json"

  Scenario: Set virtual clock to a fixed instant
    Given the time travel request instant is "2026-01-01T00:00:00Z"
    When a PUT request has been made to "/api/dev/time" endpoint
    Then the response status is 200


  Scenario: Reset virtual clock restores system time
    Given the time travel request instant is "2020-06-01T12:00:00Z"
    And a PUT request has been made to "/api/dev/time" endpoint
    Then the response status is 200
    When a DELETE request has been made to "/api/dev/time" endpoint
    Then the response status is 204
    And the virtual clock instant is approximately the current time

