package com.github.jenkaby.bikerental.componenttest.steps.tariff;

import com.github.jenkaby.bikerental.componenttest.context.ScenarioContext;
import com.github.jenkaby.bikerental.componenttest.steps.common.WebRequestSteps;
import com.github.jenkaby.bikerental.tariff.web.command.dto.TariffRequest;
import com.github.jenkaby.bikerental.tariff.web.query.dto.TariffResponse;
import com.github.jenkaby.bikerental.tariff.web.query.dto.TariffSelectionResponse;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

@Slf4j
@RequiredArgsConstructor
public class TariffWebSteps {

    public static final Comparator<TariffResponse> DEFAULT_COMPARATOR = Comparator.comparing(TariffResponse::name).thenComparing(TariffResponse::validFrom);
    private final ScenarioContext scenarioContext;
    private final WebRequestSteps webRequestSteps;

    @Given("the tariff request is prepared with the following data")
    public void theTariffRequestIsPrepared(TariffRequest request) {
        log.info("Preparing tariff request: {}", request);
        scenarioContext.setRequestBody(request);
    }

    @Then("the tariff response only contains")
    public void theTariffResponseContains(TariffResponse expectedResponse) {
        var actual = Stream.of(scenarioContext.getResponseBody(TariffResponse.class))
                .toList();
        var expectedResponses = List.of(expectedResponse);
        assertResult(expectedResponses, actual);
    }

    @Then("the tariff selection response contains")
    public void theTariffSelectionResponseContains(TariffSelectionResponse expected) {
        var actual = scenarioContext.getResponseBody(TariffSelectionResponse.class);

        log.info("Comparing tariff selection response actual: {} with expected: {}", actual, expected);
        assertSoftly(softly -> {
            softly.assertThat(actual.id()).as("ID matches").isEqualTo(expected.id());
            softly.assertThat(actual.name()).as("Name matches").isEqualTo(expected.name());
            softly.assertThat(actual.equipmentType()).as("Equipment type matches").isEqualTo(expected.equipmentType());
            softly.assertThat(actual.price()).as("Price matches").isEqualByComparingTo(expected.price());
            softly.assertThat(actual.period()).as("Period matches").isEqualTo(expected.period());
        });
    }

    @Then("the tariff response only contains page of")
    public void theTariffResponseContainsPageOf(List<TariffResponse> expectedResponses) {
        var actual = scenarioContext.getResponseAsPage(TariffResponse.class).items().stream()
                .sorted(DEFAULT_COMPARATOR)
                .toList();

        assertResult(expectedResponses, actual);
    }

    @Then("the tariff response only contains list of")
    public void theTariffResponseContainsListOf(List<TariffResponse> expectedResponses) {
        var actual = scenarioContext.getResponseAsList(TariffResponse.class).stream()
                .sorted(DEFAULT_COMPARATOR)
                .toList();

        assertResult(expectedResponses, actual);
    }

    private static void assertResult(List<TariffResponse> expectedResponses, List<TariffResponse> actual) {
        assertThat(actual.size()).as("Sizes are matched").isEqualTo(expectedResponses.size());

        var expectedAndSorted = expectedResponses.stream()
                .sorted(DEFAULT_COMPARATOR).toList();
        assertThat(actual).zipSatisfy(expectedAndSorted, (act, exp) -> {
            log.info("Comparing equipment response actual: {} with expected: {}", act, exp);

            assertSoftly(softly -> {
                softly.assertThat(act.id()).as("ID matches").isNotNull();
                softly.assertThat(act.name()).as("Name matches").isEqualTo(exp.name());
                softly.assertThat(act.equipmentTypeSlug()).as("Equipment type slug matches").isEqualTo(exp.equipmentTypeSlug());
                softly.assertThat(act.basePrice()).as("Base price matches").isEqualTo(exp.basePrice());
                softly.assertThat(act.halfHourPrice()).as("Half hour price matches").isEqualTo(exp.halfHourPrice());
                softly.assertThat(act.hourPrice()).as("Hour price matches").isEqualTo(exp.hourPrice());
                softly.assertThat(act.dayPrice()).as("Day price matches").isEqualTo(exp.dayPrice());
                softly.assertThat(act.hourDiscountedPrice()).as("Hour discounted price matches").isEqualTo(exp.hourDiscountedPrice());
                softly.assertThat(act.validFrom()).as("Valid from matches").isEqualTo(exp.validFrom());
                softly.assertThat(act.validTo()).as("Valid to matches").isEqualTo(exp.validTo());
                softly.assertThat(act.status()).as("Status matches").isEqualTo(exp.status());
            });
        });
    }

    @Given("the tariff being updated is")
    public void theTariffBeingUpdatedIs(TariffRequest request) {
        scenarioContext.setRequestBody(request);
        webRequestSteps.requestHasBeenMadeToEndpoint(HttpMethod.POST, "/api/tariffs");
        var response = scenarioContext.getResponseBody(TariffResponse.class);
        scenarioContext.setRequestedObjectId(response.id().toString());
    }
}
