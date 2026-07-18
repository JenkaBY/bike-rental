package com.github.jenkaby.bikerental.componenttest.steps.finance;

import com.github.jenkaby.bikerental.componenttest.context.ScenarioContext;
import com.github.jenkaby.bikerental.componenttest.transformer.finance.TransactionResponseRowTransformer.ExpectedTransactionRow;
import com.github.jenkaby.bikerental.finance.web.query.dto.TransactionSummaryResponse;
import com.github.jenkaby.bikerental.finance.web.query.dto.TransactionSummaryResponse.TransactionEntryResponse;
import io.cucumber.java.en.Then;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@RequiredArgsConstructor
public class TransactionsWebSteps {

    private final ScenarioContext scenarioContext;

    @Then("the transactions response only contains")
    public void theTransactionsResponseOnlyContains(List<ExpectedTransactionRow> expectedRows) {
        var actual = scenarioContext.getResponseAsPage(TransactionSummaryResponse.class).items();
        log.info("Comparing transactions actual: {} with expected: {}", actual, expectedRows);
        assertThat(actual).as("transactions size").hasSize(expectedRows.size());
        expectedRows.forEach(expected -> assertSingleMatch(actual, expected));
    }

    @Then("the transactions are ordered by recordedAt descending")
    public void theTransactionsAreOrderedByRecordedAtDescending() {
        var body = scenarioContext.getResponseBody(Map.class);
        @SuppressWarnings("unchecked")
        var items = (List<Map<String, Object>>) body.get("items");
        assertThat(items).as("transactions present").isNotEmpty();
        for (int i = 0; i < items.size() - 1; i++) {
            var current = (String) items.get(i).get("recordedAt");
            var next = (String) items.get(i + 1).get("recordedAt");
            assertThat(current).as("item[%d].recordedAt >= item[%d].recordedAt", i, i + 1)
                    .isGreaterThanOrEqualTo(next);
        }
    }

    private void assertSingleMatch(List<TransactionSummaryResponse> actual, ExpectedTransactionRow expected) {
        var matches = actual.stream()
                .filter(transaction -> matches(transaction, expected))
                .toList();
        assertThat(matches)
                .as("exactly one transaction matching %s", expected)
                .hasSize(1);
        var ledgerTypes = matches.getFirst().entries().stream()
                .map(TransactionEntryResponse::ledgerType)
                .collect(Collectors.toSet());
        assertThat(ledgerTypes)
                .as("ledger legs of transaction %s", expected)
                .containsAll(expected.ledgerTypes());
    }

    private boolean matches(TransactionSummaryResponse transaction, ExpectedTransactionRow expected) {
        if (expected.customerId() != null && !transaction.customerId().toString().equals(expected.customerId())) {
            return false;
        }
        if (!Objects.equals(transaction.type(), expected.type())) {
            return false;
        }
        if (expected.amount() != null && transaction.amount().compareTo(expected.amount()) != 0) {
            return false;
        }
        if (expected.sourceType() != null && !Objects.equals(transaction.sourceType(), expected.sourceType())) {
            return false;
        }
        return expected.sourceId() == null || Objects.equals(transaction.sourceId(), expected.sourceId());
    }
}
