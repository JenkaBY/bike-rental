# Typical task

You need to validate several of entity fields in the `@Then` step.

DO NOT DO THIS

```java
// transformer
public class FinancialAccountEntityTransformer {

    public record FinancialAccountExpectation(
            BigDecimal availableBalance,
            BigDecimal onHoldBalance
    ) {}

    @DataTableType
    public FinancialAccountExpectation transform(Map<String, String> entry) {
        return new FinancialAccountExpectation(
                new BigDecimal(entry.get("availableBalance")),
                new BigDecimal(entry.get("onHoldBalance"))
        );
    }
}

// Steps
@RequiredArgsConstructor
public class FinancialAccountDbSteps {

    private final JdbcClient jdbcClient;
    private final ScenarioContext scenarioContext;

    @Then("the financial account for the last created customer matches")
    public void financialAccountMatchesExpected(FinancialAccountExpectation expected) {
        UUID customerId = extractCustomerIdFromResponse();

        Map<String, Object> row = jdbcClient.sql(
                        "SELECT available_balance, on_hold_balance " +
                        "FROM financial_accounts WHERE customer_id = :customerId")
                .param("customerId", customerId)
                .query()
                .singleRow();

        assertThat(row).as("financial_accounts row for customer %s", customerId).isNotNull();
        assertThat((BigDecimal) row.get("available_balance"))
                .as("available_balance")
                .isEqualByComparingTo(expected.availableBalance());
        assertThat((BigDecimal) row.get("on_hold_balance"))
                .as("on_hold_balance")
                .isEqualByComparingTo(expected.onHoldBalance());
    }

    private UUID extractCustomerIdFromResponse() {
        var body = scenarioContext.getResponseBody(Map.class);
        Object rawId = body.get("id");
        assertThat(rawId).as("customer id in response body").isNotNull();
        return UUID.fromString(rawId.toString());
    }
}

```

DO this. CORRECT usage

```java
// transformer
public class FinancialAccountEntityTransformer {


    @DataTableType
    public FinancialAccountEntity paymentJpaEntity(Map<String, String> entry) {
        var id = Optional.ofNullable(entry.get("id")).map(Aliases::getPaymentId).orElse(null);
        var rentalId = DataTableHelper.toLong(entry, "rentalId");
        var amount = DataTableHelper.toBigDecimal(entry, "amount");
        var paymentType = DataTableHelper.getStringOrNull(entry, "type");
        var paymentMethod = DataTableHelper.getStringOrNull(entry, "method");
        var createdAt = Optional.ofNullable(DataTableHelper.toInstant(entry, "createdAt")).orElse(Instant.now());
        var operatorId = Aliases.getValue(entry.get("operator"));
        var receiptNumber = DataTableHelper.getStringOrNull(entry, "receipt");

        return FinancialAccountEntity.builder()
                .id(id)
                .rentalId(rentalId)
                .amount(amount)
                .paymentType(paymentType)
                .paymentMethod(paymentMethod)
                .createdAt(createdAt)
                .receiptNumber(receiptNumber)
                .operatorId(operatorId)
                .build();
    }
}

// Steps
@RequiredArgsConstructor
public class FinancialAccountDbSteps {

    private final FinancialAccountJpaRepository repository;
    private final ScenarioContext scenarioContext;

    @Then("the financial account(s) were/was persisted in DB")
    public void financialAccountMatchesExpected(List<FinancialAccountEntity> expected) {
        List<FinancialAccountEntity> actualList = repository.findAll();

        // sort before comparing
        var sortedActual = actualList.stream()
                .sorted(RENTAL_COMPARATOR)
                .toList();

        var sortedExpected = expected.stream()
                .sorted(RENTAL_COMPARATOR)
                .toList();

        assertThat(sortedActual)
                .as("Sizes are matched")
                .hasSize(sortedExpected.size());
        // use zipSatisfy for assertations of list of objects 
        assertThat(sortedActual).zipSatisfy(sortedExpected, (actual, exp) -> {
            log.info("Comparing actual rental: {}", actual);
            log.info("Comparing expected rental: {}", exp);

            var softly = new SoftAssertions();
            softly.assertThat(actual.amount()).isEqualByComparingTo(expected.amount());
            // other assertations ommited
            softly.assertAll();
        }        
    }
}

```