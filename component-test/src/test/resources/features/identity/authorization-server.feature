Feature: Identity authorization server and access control
  As an integrator and a security stakeholder
  I want the OAuth2/OIDC endpoints published and the API protected by role
  So that clients can discover the provider and only authorized callers reach admin operations

  Scenario: OpenID Connect discovery document is published
    When a GET request has been made to "/.well-known/openid-configuration" endpoint
    Then the response status is 200
    And the response contains
      | path                     | value                                  |
      | $.issuer                 | http://localhost:8080                  |
      | $.jwks_uri               | http://localhost:8080/oauth2/jwks      |
      | $.token_endpoint         | http://localhost:8080/oauth2/token     |
      | $.authorization_endpoint | http://localhost:8080/oauth2/authorize |

  Scenario: JWK Set endpoint exposes the RSA signing key
    When a GET request has been made to "/oauth2/jwks" endpoint
    Then the response status is 200
    And the response contains
      | path          | value |
      | $.keys[0].kty | RSA   |

  Scenario: Accessing account management without authentication is unauthorized
    Given no authentication is provided
    When a GET request has been made to "/api/auth/users" endpoint
    Then the response status is 401
    And the response contains
      | path        | value                            |
      | $.errorCode | identity.authentication.required |

  @Operator
  Scenario: Operator may not access administrator-only account management
    When a GET request has been made to "/api/auth/users" endpoint
    Then the response status is 403
    And the response contains
      | path        | value                  |
      | $.errorCode | identity.access.denied |
