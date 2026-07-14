package com.github.jenkaby.bikerental.componenttest.steps.tariff;

import com.github.jenkaby.bikerental.componenttest.context.ScenarioContext;
import com.github.jenkaby.bikerental.tariff.web.query.dto.CostCalculationResponse;
import com.github.jenkaby.bikerental.tariff.web.query.dto.CostQuoteResponse;
import io.cucumber.java.en.Then;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.BigDecimalComparator;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@RequiredArgsConstructor
@Slf4j
public class RentalCostQuoteWebSteps {

    private final ScenarioContext scenarioContext;

    @Then("the cost quote response contains")
    public void theCostQuoteResponseContains(CostCalculationResponse expected) {
        var actual = scenarioContext.getResponseBody(CostQuoteResponse.class);
        log.info("Asserting cost quote response. Expected calculation: {}, Actual: {}", expected, actual.calculation());
        assertThat(actual.quoteId()).as("Quote ID").isNotNull();
        assertThat(actual.calculation()).usingRecursiveComparison()
                .withComparatorForType(BigDecimalComparator.BIG_DECIMAL_COMPARATOR, BigDecimal.class)
                .ignoringFields("equipmentBreakdowns")
                .isEqualTo(expected);
        scenarioContext.setQuoteId(actual.quoteId());
        scenarioContext.setRequestedObjectId(actual.quoteId().toString());
    }
}
