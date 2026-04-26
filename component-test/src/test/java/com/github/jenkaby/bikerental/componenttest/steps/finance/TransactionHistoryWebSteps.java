package com.github.jenkaby.bikerental.componenttest.steps.finance;

import com.github.jenkaby.bikerental.componenttest.context.ScenarioContext;
import com.github.jenkaby.bikerental.finance.web.query.dto.CustomerTransactionResponse;
import io.cucumber.java.en.Then;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.SoftAssertions;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@RequiredArgsConstructor
public class TransactionHistoryWebSteps {

    private static final Comparator<CustomerTransactionResponse> COMPARING_BY_RECORDED_AT =
            Comparator.comparing(e -> e.recordedAt().toString());

    private final ScenarioContext scenarioContext;

    @Then("the transaction history response only contains entries of")
    public void theTransactionHistoryResponseOnlyContainsEntriesOf(List<CustomerTransactionResponse> expectedEntries) {
        var actualEntries = scenarioContext.getResponseAsPage(CustomerTransactionResponse.class).items()
                .stream().sorted(COMPARING_BY_RECORDED_AT).toList();
        log.info("Comparing transaction history actual: {} with expected: {}", actualEntries, expectedEntries);
        assertThat(actualEntries)
                .as("Transaction history list size")
                .hasSize(expectedEntries.size());
        assertThat(actualEntries).zipSatisfy(
                expectedEntries.stream().sorted(COMPARING_BY_RECORDED_AT).toList(),
                this::validateEntry);
    }

    @Then("the transaction history entries are ordered by recordedAt descending")
    public void theTransactionHistoryEntriesAreOrderedByRecordedAtDescending() {
        var body = scenarioContext.getResponseBody(Map.class);
        @SuppressWarnings("unchecked")
        var items = (List<Map<String, Object>>) body.get("items");

        assertThat(items).isNotEmpty();

        for (int i = 0; i < items.size() - 1; i++) {
            var current = (String) items.get(i).get("recordedAt");
            var next = (String) items.get(i + 1).get("recordedAt");
            assertThat(current).as("entry[%d].recordedAt >= entry[%d].recordedAt", i, i + 1)
                    .isGreaterThanOrEqualTo(next);
        }
    }

    private void validateEntry(CustomerTransactionResponse actual, CustomerTransactionResponse expected) {
        log.info("Comparing transaction entry actual: {} with expected: {}", actual, expected);
        var softly = new SoftAssertions();
        softly.assertThat(actual.amount()).as("amount").isEqualByComparingTo(expected.amount());
        softly.assertThat(actual.type()).as("type").isEqualTo(expected.type());
        if (expected.paymentMethod() != null) {
            softly.assertThat(actual.paymentMethod()).as("paymentMethod").isEqualTo(expected.paymentMethod());
        }
        if (expected.reason() != null) {
            softly.assertThat(actual.reason()).as("reason").isEqualTo(expected.reason());
        }
        if (expected.sourceType() != null) {
            softly.assertThat(actual.sourceType()).as("sourceType").isEqualTo(expected.sourceType());
        }
        if (expected.sourceId() != null) {
            softly.assertThat(actual.sourceId()).as("sourceId").isEqualTo(expected.sourceId());
        }
        softly.assertAll();
    }
}
