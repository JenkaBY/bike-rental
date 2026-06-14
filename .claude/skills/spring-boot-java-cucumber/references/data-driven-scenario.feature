Feature: Demonstrate use of Examples as data-driven testing

  Scenario Outline: Data-driven testing example
    Given a payload for retrive user list is prepared
      | filterValue | FilterType | page | size |
      | <upnPart>   | contains   | 1    | 10   |
    And MS Graph API returns '<msGraphStatusCode>' and then returns data for the following UPN '<upnPart>'
    When a POST request has been made to '/api/users/list' endpoint
    Then the response status is <expectedStatus>
    Examples:
#    DDD. Several rows with different input and expected output values
      | upnPart             | msGraphStatusCode | expectedStatus |
      | testuser@domain.com | 400               | 500            |
      | testuser@domain.com | 401               | 502            |
      | testuser@domain.com | 403               | 502            |
      | testuser@domain.com | 404               | 424            |