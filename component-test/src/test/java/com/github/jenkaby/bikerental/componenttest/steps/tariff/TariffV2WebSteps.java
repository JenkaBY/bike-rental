package com.github.jenkaby.bikerental.componenttest.steps.tariff;

import com.github.jenkaby.bikerental.componenttest.context.ScenarioContext;
import com.github.jenkaby.bikerental.componenttest.model.TariffV2RequestBuilder;
import com.github.jenkaby.bikerental.componenttest.steps.common.WebRequestSteps;
import com.github.jenkaby.bikerental.componenttest.transformer.PricingParamsRequestTransformer;
import com.github.jenkaby.bikerental.tariff.web.query.dto.PricingParams;
import com.github.jenkaby.bikerental.tariff.web.query.dto.TariffV2Response;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

@Slf4j
@RequiredArgsConstructor
public class TariffV2WebSteps {

    private static final String API_V2_TARIFFS = "/api/tariffs";
    public static final Comparator<TariffV2Response> DEFAULT_COMPORATOR = Comparator.comparing(TariffV2Response::name)
            .thenComparing(TariffV2Response::equipmentType)
            .thenComparing(TariffV2Response::validFrom);

    private final ScenarioContext scenarioContext;
    private final WebRequestSteps webRequestSteps;

    @Given("the pricing params for tariff request are")
    public void thePricingParamsForTariffRequestAre(PricingParamsRequestTransformer.PricingParamsRequestHolder holder) {
        scenarioContext.setPricingParams(holder.params());
    }

    @Given("the pricing params list for tariff request is")
    public void thePricingParamsForTariffRequestAre(List<PricingParamsRequestTransformer.PricingParamsRequestHolder> holders) {
        scenarioContext.setPricingParamsHolders(holders);
    }

    @Given("the tariff v2 request is prepared with the following data")
    public void theTariffV2RequestIsPrepared(TariffV2RequestBuilder builder) {
        var request = builder.toRequest(scenarioContext.getPricingParams());
        log.info("Preparing tariff v2 request: {}", request);
        scenarioContext.setRequestBody(request);
    }

    @Given("the tariff v2 being created is")
    public void theTariffV2BeingCreatedIs(TariffV2RequestBuilder builder) {
        var request = builder.toRequest((PricingParams) scenarioContext.getRequestBody());
        log.info("The tariff v2 request {}", request);
        scenarioContext.setRequestBody(request);
        webRequestSteps.requestHasBeenMadeToEndpoint(HttpMethod.POST, API_V2_TARIFFS);
        var response = scenarioContext.getResponseBody(TariffV2Response.class);
        scenarioContext.setRequestedObjectId(response.id().toString());
    }

    @Then("the tariff v2 response only contains")
    public void theTariffV2ResponseContains(TariffV2Response expected) {
        var actual = scenarioContext.getResponseBody(TariffV2Response.class);
        log.info("Comparing tariff v2 response actual: {} with expected: {}", actual, expected);
        assertSingleTariff(expected, actual);
        scenarioContext.setRequestedObjectId(actual.id().toString());
    }

    @Then("the tariff v2 response contains list of")
    public void theTariffV2ResponseContainsListOf(List<TariffV2Response> unsorted) {
        var actual = scenarioContext.getResponseAsList(TariffV2Response.class).stream()
                .sorted(DEFAULT_COMPORATOR)
                .toList();
        var expected = unsorted.stream()
                .sorted(DEFAULT_COMPORATOR)
                .toList();

        log.info("Comparing tariff v2 response actual: {} with expected: {}", actual, expected);
        assertThat(actual).size().isEqualTo(expected.size());

        assertThat(actual).zipSatisfy(expected, (act, exp) -> assertSingleTariff(exp, act));
    }

    private void assertSingleTariff(TariffV2Response expected, TariffV2Response actual) {
        assertSoftly(softly -> {
            softly.assertThat(actual.id()).as("ID").isNotNull();
            softly.assertThat(actual.name()).as("Name").isEqualTo(expected.name());
            softly.assertThat(actual.equipmentType()).as("Equipment type slug").isEqualTo(expected.equipmentType());
            softly.assertThat(actual.pricingType()).as("Pricing type").isEqualTo(expected.pricingType());
            softly.assertThat(actual.status()).as("Status").isEqualTo(expected.status());
            softly.assertThat(actual.validFrom()).as("Valid from").isEqualTo(expected.validFrom());
            softly.assertThat(actual.validTo()).as("Valid to").isEqualTo(expected.validTo());
            var pricingParams = scenarioContext.getPricingParams();
            if (pricingParams != null) {
                softly.assertThat(actual.params()).as("Pricing params")
                        .usingRecursiveComparison()
                        .withComparatorForType(Comparator.nullsFirst(Comparator.naturalOrder()), BigDecimal.class)
                        .isEqualTo(pricingParams);
            }
        });
    }

    @Then("the tariff v2 selection response contains totalCost {string} and calculationBreakdown {string}")
    public void theTariffV2SelectionResponseContains(String expectedTotalCost, String expectedBreakdown) {
        var actual = scenarioContext.getResponseBody(com.github.jenkaby.bikerental.tariff.web.query.dto.TariffSelectionV2Response.class);
        assertThat(actual.totalCost().toString()).as("totalCost").isEqualTo(expectedTotalCost);
        assertThat(actual.calculationBreakdown().getMessage()).as("calculationBreakdown").isEqualTo(expectedBreakdown);
    }
}
