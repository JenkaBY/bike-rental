Feature: Payment acceptance endpoints
  As an operator
  I want to record and query payments
  So that financial transactions are persisted and available for audit

  Scenario Outline: Record a payment
    Given the payment request is prepared with the following data
      | rentalId   | amount   | type   | method   | operator     |
      | <rentalId> | <amount> | <type> | <method> | <operatorId> |
    When a POST request has been made to "/api/payments" endpoint
    Then the response status is 201
    And the record payment response is valid
    And the following payment record was persisted in db
      | rentalId   | amount   | type   | method   | operator     |
      | <rentalId> | <amount> | <type> | <method> | <operatorId> |
    And the following payment received event was published
      | rentalId   | amount   | type   |
      | <rentalId> | <amount> | <type> |
    Examples:
      | rentalId | amount | type               | method     | operatorId |
      | 1001     | 10.00  | PREPAYMENT         | CASH       | OP1        |
      |          | 20.00  | ADDITIONAL_PAYMENT | ELECTRONIC | OP2        |

  Scenario Outline: Get payment by id
    Given the following payment record exists in db
      | id          | rentalId   | amount   | type   | method   | createdAt   | operator     | receipt   |
      | <paymentId> | <rentalId> | <amount> | <type> | <method> | <createdAt> | <operatorId> | <receipt> |
    When a GET request has been made to "/api/payments/{id}" endpoint with
      | {id}        |
      | <paymentId> |
    Then the response status is 200
    And the payment response only contains
      | id          | rentalId   | amount   | type   | method   | receipt   | operator     | createdAt   |
      | <paymentId> | <rentalId> | <amount> | <type> | <method> | <receipt> | <operatorId> | <createdAt> |
    Examples:
      | paymentId | rentalId | amount | type       | method | createdAt            | operatorId | receipt  |
      | PAY1      | 2001     | 15.00  | PREPAYMENT | CARD   | 2026-02-01T10:00:00Z | OP1        | auto-gen |

  Scenario Outline: Get payments by rental id
    Given the following payment records exist in db
      | id           | rentalId   | amount | type               | method     | createdAt            | operator   | receipt |
      | <paymentId1> | <rentalId> | 8.00   | ACCESSORY          | CASH       | 2026-02-02T09:00:00Z | <operator> | REC1    |
      | <paymentId2> | <rentalId> | 12.50  | ADDITIONAL_PAYMENT | ELECTRONIC | 2026-02-02T09:05:00Z | <operator> | REC2    |
      | <paymentId3> | 3002       | 13.50  | ADDITIONAL_PAYMENT | ELECTRONIC | 2026-02-02T09:06:00Z | <operator> | REC3    |
      | <paymentId4> |            | 14.50  | ADDITIONAL_PAYMENT | ELECTRONIC | 2026-02-02T09:07:00Z | <operator> | REC4    |
    When a GET request has been made to "/api/payments/by-rental/{rentalId}" endpoint with
      | {rentalId} |
      | <rentalId> |
    Then the response status is 200
    And the payment response only contains list of
      | id           | rentalId   | amount | type               | method     | receipt | createdAt            | operator   |
      | <paymentId1> | <rentalId> | 8.00   | ACCESSORY          | CASH       | REC1    | 2026-02-02T09:00:00Z | <operator> |
      | <paymentId2> | <rentalId> | 12.50  | ADDITIONAL_PAYMENT | ELECTRONIC | REC2    | 2026-02-02T09:05:00Z | <operator> |
    Examples:
      | paymentId1 | paymentId2 | paymentId3 | paymentId4 | rentalId | operator |
      | PAY1       | PAY2       | PAY3       | PAY4       | 3001     | OP1      |

  Scenario Outline: Get payments by rental id when no payments exist
    Given the following payment records exist in db
      | id           | rentalId | amount | type               | method     | createdAt            | operator   | receipt |
      | <paymentId1> | 3002     | 13.50  | ADDITIONAL_PAYMENT | ELECTRONIC | 2026-02-02T09:06:00Z | <operator> | REC3    |
    When a GET request has been made to "/api/payments/by-rental/{rentalId}" endpoint with
      | {rentalId} |
      | <rentalId> |
    Then the response status is 200
    And the response is empty list
    Examples:
      | paymentId1 | rentalId | operator |
      | PAY1       | 3001     | OP1      |

  Scenario: Get non-existing payment
    When a GET request has been made to "/api/payments/99999999-9999-9999-9999-999999999999" endpoint
    Then the response status is 404
    And the response contains
      | path     | value                                                                    |
      | $.title  | Not Found                                                                |
      | $.detail | Payment with identifier '99999999-9999-9999-9999-999999999999' not found |
