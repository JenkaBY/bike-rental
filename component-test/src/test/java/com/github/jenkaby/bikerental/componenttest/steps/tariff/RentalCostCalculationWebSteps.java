package com.github.jenkaby.bikerental.componenttest.steps.tariff;

import com.github.jenkaby.bikerental.componenttest.context.ScenarioContext;
import com.github.jenkaby.bikerental.tariff.web.query.dto.CostCalculationRequest;
import com.github.jenkaby.bikerental.tariff.web.query.dto.CostCalculationResponse;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.BigDecimalComparator;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

@RequiredArgsConstructor
@Slf4j
public class RentalCostCalculationWebSteps {

    public static final Comparator<CostCalculationResponse.EquipmentCostBreakdownResponse> DEFAULT_COMPARATOR = Comparator.comparing(CostCalculationResponse.EquipmentCostBreakdownResponse::equipmentType);
    private final ScenarioContext scenarioContext;

    @Given("the rental request is prepared with the following data")
    public void theRentalRequestIsPreparedWithTheFollowingData(CostCalculationRequest request) {
        log.info("Set rental cost calculation request body: {}", request);
        scenarioContext.setRequestBody(request);
    }

    @Then("the rental cost calculation response only contains")
    public void theRentalCostCalculationResponseContains(CostCalculationResponse expected) {
        var actual = scenarioContext.getResponseBody(CostCalculationResponse.class);
        log.info("Asserting rental cost calculation response. Expected: {}, Actual: {}", expected, actual);
        assertThat(actual).usingRecursiveComparison()
                .withComparatorForType(BigDecimalComparator.BIG_DECIMAL_COMPARATOR, BigDecimal.class)
                .ignoringFields("equipmentBreakdowns")
                .isEqualTo(expected);
    }

    @And("the rental cost calculation response only contains the breakdown with the following data")
    public void theRentalCostCalculationResponseOnlyContainsTheBreakdownWithTheFollowingData(List<CostCalculationResponse.EquipmentCostBreakdownResponse> expectedBreakdowns) {
        var expected = expectedBreakdowns.stream()
                .sorted(DEFAULT_COMPARATOR)
                .toList();
        var actual = scenarioContext.getResponseBody(CostCalculationResponse.class).equipmentBreakdowns().stream()
                .sorted(DEFAULT_COMPARATOR)
                .toList();
        log.info("Asserting rental cost calculation response breakdowns. Expected: {}, Actual: {}", expected, actual);
        assertThat(actual).hasSameSizeAs(expected);
        assertThat(actual).zipSatisfy(expected, this::assertSingleBreakDown);
    }

    private void assertSingleBreakDown(CostCalculationResponse.EquipmentCostBreakdownResponse actual,
                                       CostCalculationResponse.EquipmentCostBreakdownResponse expected) {
        assertSoftly(softly -> {

            if (expected.equipmentType() != null) {
                softly.assertThat(actual.equipmentType()).isEqualTo(expected.equipmentType());
            } else {
                softly.assertThat(actual.equipmentType()).isNotNull();
            }

            if (expected.tariffId() != null) {
                softly.assertThat(actual.tariffId()).isEqualTo(expected.tariffId());
            } else {
                softly.assertThat(actual.tariffId()).isNotNull();
            }

            if (expected.tariffName() != null) {
                softly.assertThat(actual.tariffName()).isEqualTo(expected.tariffName());
            } else {
                softly.assertThat(actual.tariffName()).isNotNull();
            }

            if (expected.pricingType() != null) {
                softly.assertThat(actual.pricingType()).isEqualTo(expected.pricingType());
            } else {
                softly.assertThat(actual.pricingType()).isNotNull();
            }

            if (expected.itemCost() != null) {
                softly.assertThat(actual.itemCost()).isEqualByComparingTo(expected.itemCost());
            } else {
                softly.assertThat(actual.itemCost()).isNotNull();
            }

            if (expected.billedDurationMinutes() != null) {
                softly.assertThat(actual.billedDurationMinutes()).isEqualTo(expected.billedDurationMinutes());
            } else {
                softly.assertThat(actual.billedDurationMinutes()).isNotNull();
            }
            if (expected.overtimeMinutes() != null) {
                softly.assertThat(actual.overtimeMinutes()).isEqualTo(expected.overtimeMinutes());
            } else {
                softly.assertThat(actual.overtimeMinutes()).isNotNull();
            }

            if (expected.forgivenMinutes() != null) {
                softly.assertThat(actual.forgivenMinutes()).isEqualTo(expected.forgivenMinutes());
            } else {
                softly.assertThat(actual.forgivenMinutes()).isNotNull();
            }

            if (expected.calculationBreakdown() != null) {
                if (expected.calculationBreakdown().getMessage() != null) {
                    softly.assertThat(actual.calculationBreakdown().getMessage())
                            .isEqualTo(expected.calculationBreakdown().getMessage());
                } else {
                    softly.assertThat(actual.calculationBreakdown().getMessage()).isNotNull();
                }

                if (expected.calculationBreakdown().getBreakdownPatternCode() != null) {
                    softly.assertThat(actual.calculationBreakdown().getBreakdownPatternCode())
                            .isEqualTo(expected.calculationBreakdown().getBreakdownPatternCode());
                } else {
                    softly.assertThat(actual.calculationBreakdown().getBreakdownPatternCode()).isNotNull();
                }
            }
        });
    }

}
