# Aliases usage

For complex ids like `UUID` type, to hide it on the feature files. We use `Aliases` utility class. It holds presaved
ids:

```java
public class Aliases {
    private static final Map<String, String> ALIASES;
    static {
        ALIASES = new HashMap<>();
//            Customer ids group starts with 11111111-
        ALIASES.put("CUS1", "11111111-1111-1111-1111-111111111111");
//        Operator ids group starts with 22222222-
        ALIASES.put("OP1", "22222222-2222-2222-2222-222222222221");
//       Payment ids group starts with 33333333-
        ALIASES.put("PAY1", "33333333-3333-3333-3333-333333333331");
    }
    //...
}
```

To use it in transformers:

```java
public class PaymentJpaEntityTransformer {
    @DataTableType
    public PaymentJpaEntity paymentJpaEntity(Map<String, String> entry) {
        var id = Optional.ofNullable(entry.get("id")).map(Aliases::getPaymentId).orElse(null); // first usage
//... ommited for breivity
        var operatorId = Aliases.getValue(entry.get("operator")); // the second usage
        return PaymentJpaEntity.builder()
        .id(id)
//... ommited for breivity
        .operatorId(operatorId)
        .build();
    }
```

the corresponding scenario might look like:

```gherkin
  Scenario: Get payments by rental id when no payments exist
Given the following payment records exist in db
| id   | rentalId | amount | type               | method     | createdAt            | operator   | receipt |
| PAY1 | 3002     | 13.50  | ADDITIONAL_PAYMENT | ELECTRONIC | 2026-02-02T09:06:00Z | OP1        | REC3    |
When a GET request has been made to "/api/payments/by-rental/{rentalId}" endpoint with
<!-- ommited for breivity -->
```