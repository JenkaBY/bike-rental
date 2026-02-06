package com.github.jenkaby.bikerental.componenttest.steps.finance;

import com.github.jenkaby.bikerental.componenttest.context.ScenarioContext;
import com.github.jenkaby.bikerental.finance.web.command.dto.RecordPaymentRequest;
import com.github.jenkaby.bikerental.finance.web.command.dto.RecordPaymentResponse;
import com.github.jenkaby.bikerental.finance.web.query.dto.PaymentResponse;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

@Slf4j
@RequiredArgsConstructor
public class PaymentWebSteps {

    public static final Comparator<PaymentResponse> DEFAULT_COMPORATOR = Comparator.comparing(PaymentResponse::id);
    private final ScenarioContext scenarioContext;

    @Given("the payment request is prepared with the following data")
    public void thePaymentRequestIsPreparedWithTheFollowingData(RecordPaymentRequest request) {
        log.info("Preparing payment request: {}", request);
        scenarioContext.setRequestBody(request);
    }

    @Then("the payment response only contains")
    public void thePaymentResponseOnlyContains(PaymentResponse expected) {
        var actual = Stream.of(scenarioContext.getResponseBody(PaymentResponse.class))
                .toList();

        var expectedResponses = List.of(expected);
        assertResult(expectedResponses, actual);
    }


    @Then("the record payment response is valid")
    public void thePaymentResponseOnlyContains() {
        var actual = scenarioContext.getResponseBody(RecordPaymentResponse.class);

        assertThat(actual).isNotNull();
        assertThat(actual.paymentId()).isNotNull();
        assertThat(actual.receiptNumber()).isNotNull();

        scenarioContext.setRequestedObjectId(actual.paymentId().toString());
        scenarioContext.addPersistedId(actual.paymentId());
    }


    @Then("the payment response only contains list of")
    public void thePaymentResponseOnlyContainsListOf(List<PaymentResponse> expected) {
        var actual = scenarioContext.getResponseAsList(PaymentResponse.class).stream()
                .sorted(DEFAULT_COMPORATOR)
                .toList();

        assertResult(expected, actual);
    }

    private void assertResult(List<PaymentResponse> expectedResponses, List<PaymentResponse> actual) {
        assertThat(actual.size()).as("Sizes are matched").isEqualTo(expectedResponses.size());

        var expectedAndSorted = expectedResponses.stream()
                .sorted(DEFAULT_COMPORATOR)
                .toList();
        assertThat(actual).zipSatisfy(expectedAndSorted, (act, exp) -> {
            log.info("Comparing equipment response actual: {} with expected: {}", act, exp);

            assertSoftly(softly -> {
                softly.assertThat(act.id()).as("Payment ID").isEqualTo(exp.id());
                softly.assertThat(act.amount()).as("Payment amount").isEqualByComparingTo(exp.amount());
                softly.assertThat(act.paymentMethod()).as("Payment method").isEqualTo(exp.paymentMethod());
                softly.assertThat(act.paymentType()).as("Payment status").isEqualTo(exp.paymentType());
                softly.assertThat(act.createdAt()).as("Payment createdAt")
                        .isCloseTo(exp.createdAt(), within(5, ChronoUnit.SECONDS));
                softly.assertThat(act.operatorId()).as("Payment operatorId").isEqualTo(exp.operatorId());
                softly.assertThat(act.receiptNumber()).as("Payment receiptNumber").isEqualTo(exp.receiptNumber());
            });
        });
    }
}
