package com.github.jenkaby.bikerental.componenttest.steps.finance;

import com.github.jenkaby.bikerental.componenttest.context.ScenarioContext;
import com.github.jenkaby.bikerental.componenttest.transformer.finance.TransactionDetailsResponseRowTransformer.ExpectedTransactionDetailsRow;
import com.github.jenkaby.bikerental.componenttest.transformer.finance.TransactionDetailsResponseRowTransformer.ExpectedTransactionEntryRow;
import com.github.jenkaby.bikerental.finance.web.query.dto.TransactionDetailsResponse;
import com.github.jenkaby.bikerental.finance.web.query.dto.TransactionDetailsResponse.TransactionDetailEntryResponse;
import io.cucumber.java.en.Then;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.SoftAssertions;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@RequiredArgsConstructor
public class TransactionDetailsWebSteps {

    private final ScenarioContext scenarioContext;

    @Then("the transaction details response contains")
    public void theTransactionDetailsResponseContains(List<ExpectedTransactionDetailsRow> rows) {
        assertThat(rows).as("exactly one expected details row").hasSize(1);
        var expected = rows.getFirst();
        var actual = scenarioContext.getResponseBody(TransactionDetailsResponse.class);
        log.info("Comparing transaction details actual: {} with expected: {}", actual, expected);

        var softly = new SoftAssertions();
        if (expected.customerId() != null) {
            softly.assertThat(actual.customerId().toString()).as("customerId").isEqualTo(expected.customerId());
        }
        if (expected.type() != null) {
            softly.assertThat(actual.type()).as("type").isEqualTo(expected.type());
        }
        assertMoney(softly, "amount", actual.amount(), expected.amount());
        assertMoney(softly, "deltas.wallet", actual.deltas().wallet(), expected.walletDelta());
        assertMoney(softly, "deltas.hold", actual.deltas().hold(), expected.holdDelta());
        assertMoney(softly, "deltas.external", actual.deltas().external(), expected.externalDelta());
        assertMoney(softly, "balances.wallet", actual.balances().wallet(), expected.walletBalance());
        assertMoney(softly, "balances.hold", actual.balances().hold(), expected.holdBalance());
        softly.assertAll();
    }

    @Then("the transaction details entries only contain")
    public void theTransactionDetailsEntriesOnlyContain(List<ExpectedTransactionEntryRow> rows) {
        var actual = scenarioContext.getResponseBody(TransactionDetailsResponse.class).entries();
        log.info("Comparing transaction details entries actual: {} with expected: {}", actual, rows);
        assertThat(actual).as("entries size").hasSize(rows.size());
        rows.forEach(expected -> assertSingleMatch(actual, expected));
    }

    private void assertSingleMatch(List<TransactionDetailEntryResponse> actual, ExpectedTransactionEntryRow expected) {
        var matches = actual.stream()
                .filter(entry -> entry.ledgerType().equals(expected.ledgerType())
                        && entry.direction().equals(expected.direction()))
                .toList();
        assertThat(matches)
                .as("exactly one entry matching %s/%s", expected.ledgerType(), expected.direction())
                .hasSize(1);
        var entry = matches.getFirst();
        var softly = new SoftAssertions();
        assertMoney(softly, "amount", entry.amount(), expected.amount());
        assertMoney(softly, "signedDelta", entry.signedDelta(), expected.signedDelta());
        if (expected.balanceAfter() != null) {
            assertMoney(softly, "balanceAfter", entry.balanceAfter(), expected.balanceAfter());
        }
        if (expected.systemLedger() != null) {
            softly.assertThat(entry.systemLedger()).as("systemLedger").isEqualTo(expected.systemLedger());
        }
        softly.assertAll();
    }

    private static void assertMoney(SoftAssertions softly, String field, BigDecimal actual,
                                    BigDecimal expected) {
        if (expected == null) {
            return;
        }
        if (actual == null) {
            softly.fail("%s expected %s but was null", field, expected);
            return;
        }
        softly.assertThat(actual.compareTo(expected))
                .as("%s (actual=%s expected=%s)", field, actual, expected)
                .isZero();
    }
}
