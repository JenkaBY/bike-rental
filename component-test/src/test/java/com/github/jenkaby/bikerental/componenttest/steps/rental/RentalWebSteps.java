package com.github.jenkaby.bikerental.componenttest.steps.rental;

import com.github.jenkaby.bikerental.componenttest.context.ScenarioContext;
import com.github.jenkaby.bikerental.componenttest.transformer.EquipmentCostBreakdownTransformer;
import com.github.jenkaby.bikerental.componenttest.transformer.EquipmentItemResponseTransformer;
import com.github.jenkaby.bikerental.rental.web.command.dto.RentalPatchOperation;
import com.github.jenkaby.bikerental.rental.web.command.dto.RentalRequest;
import com.github.jenkaby.bikerental.rental.web.command.dto.RentalUpdateJsonPatchRequest;
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
    public void aRentalRequestWithTheFollowingData(RentalRequest request) {
        log.info("Preparing rental request with data: {}", request);
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
        if (expected.overdueMinutes() != null) {
            assertThat(actual.overdueMinutes())
                    .as("Overdue minutes")
                    .isEqualTo(expected.overdueMinutes());
        }
        if (expected.plannedDurationMinutes() != null) {
            assertThat(actual.plannedDurationMinutes())
                    .as("Planned duration minutes")
                    .isEqualTo(expected.plannedDurationMinutes());
        }
        if (expected.actualDurationMinutes() != null) {
            assertThat(actual.actualDurationMinutes())
                    .as("Actual duration minutes")
                    .isEqualTo(expected.actualDurationMinutes());
        }
        softly.assertAll();
    }



    @Then("the rental response only contains")
    public void theRentalResponseOnlyContains(RentalResponse expectedRental) {
        var actualRental = scenarioContext.getResponseBody(RentalResponse.class);
        assertRentalResponse(actualRental, expectedRental);
        scenarioContext.setRequestedObjectId(actualRental.id().toString());
    }

    @Then("the rental response only contains rental equipments")
    public void theRentalResponseOnlyContains(List<EquipmentItemResponseTransformer.EquipmentItemResponseTransformerHolder> holders) {
        var expected = holders.stream()
                .map(EquipmentItemResponseTransformer.EquipmentItemResponseTransformerHolder::equipmentItemResponse)
                .toList();
        rentalResponseContainsEquipments(expected);
    }

    public void rentalResponseContainsEquipments(List<EquipmentItemResponse> expected) {
        var actualRental = scenarioContext.getResponseBody(RentalResponse.class);
        assertRentalEquipmentItemResponse(actualRental.equipmentItems(), expected);
        scenarioContext.setRequestedObjectId(actualRental.id().toString());
    }

    void assertEquipmentBreakdowns(List<EquipmentItemResponse> equipmentItems,
            List<EquipmentCostBreakdownTransformer.EquipmentCostBreakdownAssertionHolder> expected) {
        assertThat(equipmentItems).as("Equipment items in rental response").isNotNull();
        expected.forEach(expectedHolder -> {
            var matchedItem = equipmentItems.stream()
                    .filter(item -> expectedHolder.equipmentId().equals(item.equipmentId()))
                    .findFirst()
                    .orElseThrow(() -> new AssertionError("No equipment item found for id: " + expectedHolder.equipmentId()));
            var actualBreakdown = matchedItem.breakdown();
            var expectedBreakdown = expectedHolder.breakdown();
            assertSoftly(softly -> {
                softly.assertThat(actualBreakdown)
                        .as("Breakdown for equipment %d should not be null", expectedHolder.equipmentId())
                        .isNotNull();
                if (actualBreakdown == null) {
                    return;
                }
                if (expectedBreakdown.pricingType() != null) {
                    softly.assertThat(actualBreakdown.pricingType())
                            .as("pricingType for equipment %d", expectedHolder.equipmentId())
                            .isEqualTo(expectedBreakdown.pricingType());
                }
                if (expectedBreakdown.tariffName() != null) {
                    softly.assertThat(actualBreakdown.tariffName())
                            .as("tariffName for equipment %d", expectedHolder.equipmentId())
                            .isEqualTo(expectedBreakdown.tariffName());
                }
                if (expectedBreakdown.billedDurationMinutes() != null) {
                    softly.assertThat(actualBreakdown.billedDurationMinutes())
                            .as("billedDurationMinutes for equipment %d", expectedHolder.equipmentId())
                            .isEqualTo(expectedBreakdown.billedDurationMinutes());
                }
                if (expectedBreakdown.overtimeMinutes() != null) {
                    softly.assertThat(actualBreakdown.overtimeMinutes())
                            .as("overtimeMinutes for equipment %d", expectedHolder.equipmentId())
                            .isEqualTo(expectedBreakdown.overtimeMinutes());
                }
                if (expectedBreakdown.itemCost() != null) {
                    softly.assertThat(actualBreakdown.itemCost())
                            .as("itemCost for equipment %d", expectedHolder.equipmentId())
                            .isEqualByComparingTo(expectedBreakdown.itemCost());
                }
                if (expectedBreakdown.calculationBreakdown() != null
                        && expectedBreakdown.calculationBreakdown().breakdownPatternCode() != null) {
                    softly.assertThat(actualBreakdown.calculationBreakdown())
                            .as("calculationBreakdown should not be null for equipment %d", expectedHolder.equipmentId())
                            .isNotNull();
                    if (actualBreakdown.calculationBreakdown() != null) {
                        softly.assertThat(actualBreakdown.calculationBreakdown().breakdownPatternCode())
                                .as("breakdownPatternCode for equipment %d", expectedHolder.equipmentId())
                                .isEqualTo(expectedBreakdown.calculationBreakdown().breakdownPatternCode());
                    }
                }
            });
        });
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
        if (expected.specialPrice() != null) {
            softly.assertThat(actual.specialPrice())
                    .as("Special price")
                    .isEqualByComparingTo(expected.specialPrice());
        }
        if (expected.discountPercent() != null) {
            softly.assertThat(actual.discountPercent())
                    .as("Discount percent")
                    .isEqualTo(expected.discountPercent());
        }
        if (expected.finalCost() != null) {
            softly.assertThat(actual.finalCost())
                    .as("Final cost")
                    .isEqualByComparingTo(expected.finalCost());
        }
        softly.assertAll();
    }
}
