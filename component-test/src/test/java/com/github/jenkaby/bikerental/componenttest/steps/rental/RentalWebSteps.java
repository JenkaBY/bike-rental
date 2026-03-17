package com.github.jenkaby.bikerental.componenttest.steps.rental;

import com.github.jenkaby.bikerental.componenttest.context.ScenarioContext;
import com.github.jenkaby.bikerental.componenttest.transformer.EquipmentItemResponseTransformer;
import com.github.jenkaby.bikerental.rental.web.command.dto.*;
import com.github.jenkaby.bikerental.rental.web.query.dto.EquipmentItemResponse;
import com.github.jenkaby.bikerental.rental.web.query.dto.RentalResponse;
import com.github.jenkaby.bikerental.rental.web.query.dto.RentalSummaryResponse;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.SoftAssertions;

import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

@Slf4j
@RequiredArgsConstructor
public class RentalWebSteps {

    private static final Comparator<EquipmentItemResponse> COMPARING_EQUIPMENT_ITEM_BY_ID = Comparator.comparing(EquipmentItemResponse::equipmentId);
    public static final Comparator<RentalSummaryResponse> COMPARING_BY_ID = Comparator.comparing(RentalSummaryResponse::id);
    private final ScenarioContext scenarioContext;

    @Given("the rental update request is")
    public void theRentalUpdateRequestBodyIs(List<RentalPatchOperation> ops) {
        log.info("Preparing rental update request with {} operations: {}", ops.size(), ops);
        RentalUpdateJsonPatchRequest body = new RentalUpdateJsonPatchRequest(ops);
        scenarioContext.setRequestBody(body);
    }

    @Given("a rental request with the following data")
    public void aRentalRequestWithTheFollowingData(CreateRentalRequest request) {
        log.info("Preparing rental request with data: {}", request);
        scenarioContext.setRequestBody(request);
    }

    @Given("the prepayment request is")
    public void thePrepaymentRequestIs(RecordPrepaymentRequest request) {
        log.info("Preparing prepayment request: {}", request);
        scenarioContext.setRequestBody(request);
    }

    @Then("the rental summary response only contains page of")
    public void theRentalResponseOnlyContainsPageOf(List<RentalSummaryResponse> expectedRentals) {
        var actualRentals = scenarioContext.getResponseAsPage(RentalSummaryResponse.class).items().stream().sorted(COMPARING_BY_ID).toList();
        log.info("Comparing rental response list actual: {} with expected: {}", actualRentals, expectedRentals);
        assertThat(actualRentals)
                .as("Rental response list size")
                .hasSize(expectedRentals.size());
        assertThat(actualRentals).zipSatisfy(expectedRentals.stream().sorted(COMPARING_BY_ID).toList(), this::validateSummary);
    }

    private void validateSummary(RentalSummaryResponse actual, RentalSummaryResponse expected) {
        log.info("Comparing rental summary response actual: {} with expected: {}", actual, expected);
        var softly = new SoftAssertions();
        softly.assertThat(actual.id())
                .as("Rental ID")
                .isEqualTo(expected.id());
        softly.assertThat(actual.customerId())
                .as("Customer ID")
                .isEqualTo(expected.customerId());
        softly.assertThat(actual.equipmentIds())
                .as("Equipment IDs")
                .isEqualTo(expected.equipmentIds());
        softly.assertThat(actual.status())
                .as("Status")
                .isEqualTo(expected.status());
        if (expected.startedAt() != null) {
            softly.assertThat(actual.startedAt())
                    .as("Started at")
                    .isCloseTo(expected.startedAt(), within(5, ChronoUnit.SECONDS));
        }
        if (expected.expectedReturnAt() != null) {
            softly.assertThat(actual.expectedReturnAt())
                    .as("Expected return at")
                    .isCloseTo(expected.expectedReturnAt(), within(5, ChronoUnit.SECONDS));
        }
        assertThat(actual.overdueMinutes())
                .as("Overdue minutes")
                .isEqualTo(expected.overdueMinutes());
        softly.assertAll();
    }

    @Then("the prepayment response contains")
    public void thePrepaymentRequestIs(PrepaymentResponse expected) {
        log.info("Preparing prepayment response: {}", expected);
        var actual = scenarioContext.getResponseBody(PrepaymentResponse.class);
        assertSoftly(softly -> {
            softly.assertThat(actual.paymentId()).as("Payment ID matches").isNotNull();
            softly.assertThat(actual.amount()).as("Amount matches").isEqualByComparingTo(expected.amount());
            softly.assertThat(actual.paymentMethod()).as("Payment method matches").isEqualTo(expected.paymentMethod());
            softly.assertThat(actual.receiptNumber()).as("Receipt number matches").isNotBlank();
            softly.assertThat(actual.createdAt()).as("Created at matches").isCloseTo(expected.createdAt(), within(5, ChronoUnit.SECONDS));
        });

        if (actual.paymentId() != null) {
            log.info("Saving paymentId {} to scenario context for later validation", actual.paymentId());
            scenarioContext.setRequestedObjectId(actual.paymentId().toString());
        }
    }

    @Then("the rental response only contains")
    public void theRentalResponseOnlyContains(RentalResponse expectedRental) {
        var actualRental = scenarioContext.getResponseBody(RentalResponse.class);
        assertRentalResponse(actualRental, expectedRental);
        scenarioContext.setRequestedObjectId(actualRental.id().toString());
    }

    @Then("the rental response only contains rental equipments")
    public void theRentalResponseOnlyContains(List<EquipmentItemResponseTransformer.EquipmentItemResponseTransformerHolder> holders) {
        var actualRental = scenarioContext.getResponseBody(RentalResponse.class);
        var expected = holders.stream()
                .map(EquipmentItemResponseTransformer.EquipmentItemResponseTransformerHolder::equipmentItemResponse)
                .toList();
        assertRentalEquipmentItemResponse(actualRental.equipmentItems(), expected);
        scenarioContext.setRequestedObjectId(actualRental.id().toString());
    }

    private void assertRentalEquipmentItemResponse(List<EquipmentItemResponse> actual, List<EquipmentItemResponse> expected) {
        log.info("Comparing equipment items actual: {} with expected: {}", actual, expected);
        assertThat(actual)
                .as("Equipment items in rental response")
                .isNotNull()
                .hasSize(expected.size());
        var sortedActual = actual.stream().sorted(COMPARING_EQUIPMENT_ITEM_BY_ID).toList();
        var sortedExpected = expected.stream().sorted(COMPARING_EQUIPMENT_ITEM_BY_ID).toList();
        assertThat(sortedActual).zipSatisfy(sortedExpected, (item, expectedItem) ->
                assertSoftly(softly -> {
                    softly.assertThat(item.equipmentId())
                            .as("Equipment ID")
                            .isEqualTo(expectedItem.equipmentId());
                    if (expectedItem.equipmentUid() != null) {
                        softly.assertThat(item.equipmentUid())
                                .as("Equipment UID")
                                .isEqualTo(expectedItem.equipmentUid());
                    }
                    if (expectedItem.tariffId() != null) {
                        softly.assertThat(item.tariffId())
                                .as("Tariff ID")
                                .isEqualTo(expectedItem.tariffId());
                    }
                    if (expectedItem.status() != null) {
                        softly.assertThat(item.status())
                                .as("Equipment item status")
                                .isEqualTo(expectedItem.status());
                    }
                    if (expectedItem.estimatedCost() != null) {
                        softly.assertThat(item.estimatedCost())
                                .as("Estimated cost")
                                .isEqualByComparingTo(expectedItem.estimatedCost());
                    }
                    if (expectedItem.finalCost() != null) {
                        softly.assertThat(item.finalCost())
                                .as("Final cost")
                                .isEqualByComparingTo(expectedItem.finalCost());
                    }
                })
        );
    }

    private void assertRentalResponse(RentalResponse actual, RentalResponse expected) {
        log.info("Comparing rental response actual: {} with expected: {}", actual, expected);
        var softly = new SoftAssertions();

        if (expected.id() != null) {
            softly.assertThat(actual.id())
                    .as("Rental ID")
                    .isEqualTo(expected.id());
        } else {
            softly.assertThat(actual.id())
                    .as("Rental ID should not be null")
                    .isNotNull();
        }

        softly.assertThat(actual.customerId())
                .as("Customer ID")
                .isEqualTo(expected.customerId());
        softly.assertThat(actual.status())
                .as("Status")
                .isEqualTo(expected.status());
        if (expected.startedAt() != null) {
            softly.assertThat(actual.startedAt())
                    .as("Started at")
                    .isCloseTo(expected.startedAt(), within(5, ChronoUnit.SECONDS));
        }
        if (expected.expectedReturnAt() != null) {
            softly.assertThat(actual.expectedReturnAt())
                    .as("Expected return at")
                    .isCloseTo(expected.expectedReturnAt(), within(5, ChronoUnit.SECONDS));
        }
        if (expected.actualReturnAt() != null) {
            softly.assertThat(actual.actualReturnAt())
                    .as("Actual return at")
                    .isEqualTo(expected.actualReturnAt());
        }
        softly.assertThat(actual.plannedDurationMinutes())
                .as("Planned duration minutes")
                .isEqualTo(expected.plannedDurationMinutes());
        softly.assertThat(actual.actualDurationMinutes())
                .as("Actual duration minutes")
                .isEqualTo(expected.actualDurationMinutes());
        if (expected.estimatedCost() != null) {
            softly.assertThat(actual.estimatedCost())
                    .as("Estimated cost")
                    .isEqualByComparingTo(expected.estimatedCost());
        }
        if (expected.finalCost() != null) {
            softly.assertThat(actual.finalCost())
                    .as("Final cost")
                    .isEqualByComparingTo(expected.finalCost());
        }
        softly.assertAll();
    }
}
