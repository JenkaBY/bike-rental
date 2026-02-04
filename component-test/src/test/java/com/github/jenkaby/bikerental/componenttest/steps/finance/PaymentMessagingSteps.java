package com.github.jenkaby.bikerental.componenttest.steps.finance;

import com.github.jenkaby.bikerental.componenttest.context.MessageStore;
import com.github.jenkaby.bikerental.componenttest.context.ScenarioContext;
import com.github.jenkaby.bikerental.finance.PaymentReceived;
import io.cucumber.java.en.Then;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.awaitility.Awaitility.await;

@Slf4j
@RequiredArgsConstructor
public class PaymentMessagingSteps {

    private static final Comparator<PaymentReceived> DEFAULT_COMPARATOR = Comparator.comparing(PaymentReceived::paymentId);
    private final MessageStore messageStore;
    private final ScenarioContext scenarioContext;

    @Then("the following payment received event was published")
    public void theFollowingPaymentMessageWasPublished(PaymentReceived expectedMessage) {
        List<PaymentReceived> expected = List.of(
                expectedMessage.toBuilder()
                        .paymentId(UUID.fromString(scenarioContext.getModifiedObjectId()))
                        .build()
        );

        Function<PaymentReceived, Object> paymentId = PaymentReceived::paymentId;
        Set<Object> ids = expected.stream()
                .map(paymentId)
                .collect(Collectors.toSet());

        await()
                .atMost(Duration.ofSeconds(3))
                .pollInterval(Duration.ofMillis(100))
                .untilAsserted(() -> {
                    List<PaymentReceived> actualMessages = messageStore.getEventsFor(PaymentReceived.class, ids, paymentId).stream()
                            .sorted(DEFAULT_COMPARATOR)
                            .toList();
                    assertThat(actualMessages.size()).as("Number of published PaymentReceived events").isEqualTo(expected.size());

                    var expectedAndSorted = expected.stream()
                            .sorted(DEFAULT_COMPARATOR)
                            .toList();

                    assertThat(actualMessages).zipSatisfy(expectedAndSorted, this::assertSingleResult);
                });


    }

    private void assertSingleResult(PaymentReceived act, PaymentReceived exp) {
        log.info("Comparing equipment response actual: {} with expected: {}", act, exp);
        assertSoftly(softly -> {
            softly.assertThat(act.paymentId()).isEqualTo(exp.paymentId());
            softly.assertThat(act).usingRecursiveComparison().ignoringFields("paymentId")
                    .withComparatorForType((a, e) -> {
                        log.debug("[TEST] Comparing timestamps, actual: {}, expected: {}", a, e);
                        log.debug("[TEST] Comparing timestamps, durations {}s", Duration.between(a, e).abs().getSeconds());
                        return Duration.between(a, e).abs().getSeconds() > 5 ? -1 : 0;
                    }, Instant.class)
                    .isEqualTo(exp);
        });
    }
}
