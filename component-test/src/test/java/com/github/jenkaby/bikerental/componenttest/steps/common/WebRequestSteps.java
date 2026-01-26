package com.github.jenkaby.bikerental.componenttest.steps.common;

import com.github.jenkaby.bikerental.componenttest.context.ScenarioContext;
import com.github.jenkaby.bikerental.componenttest.model.JsonPathExpectation;
import com.jayway.jsonpath.JsonPath;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.Transpose;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.SoftAssertions;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@RequiredArgsConstructor
@Slf4j
public class WebRequestSteps {

    private final TestRestTemplate restClient;
    private final ScenarioContext scenarioContext;
    @LocalServerPort
    private final int port;

    private Instant requestStartTime;

    @Given("the request body is")
    public void theRequestBodyIs(String body) {
        scenarioContext.setRequestBody(body);
    }

    @Given("the request header {string} is {string}")
    public void theRequestHeaderIs(String headerName, String headerValue) {
        scenarioContext.replaceHeader(headerName, headerValue);
    }

    @Given("the {string} header is set to {string}")
    public void theHeaderIsSetTo(String headerName, String headerValue) {
        log.info("Setting header '{}' to '{}'", headerName, headerValue);
        scenarioContext.replaceHeader(headerName, headerValue);
    }

    @Given("the {string} header is removed")
    public void theHeaderIsRemoved(String headerName, String headerValue) {
        log.info("Removing header '{}'", headerName);
        scenarioContext.removeHeader(headerName);
    }

    @When("a {httpMethod} request has been made to {string} endpoint")
    public void requestHasBeenMadeToEndpoint(HttpMethod method, String endpoint) {
        requestHasBeenMadeToEndpointTimes(method, 1, endpoint, null);
    }

    @When("a {httpMethod} request has been made to {string} endpoint with query parameters")
    public void requestHasBeenMadeToEndpoint(HttpMethod method,
                                             String endpoint,
                                             @Transpose DataTable queryParams) {
        requestHasBeenMadeToEndpointTimes(method, 1, endpoint, queryParams);
    }

    @When("a {httpMethod} request has been made {int} times to {string} endpoint with query parameters")
    public void requestHasBeenMadeToEndpointTimes(HttpMethod method,
                                                  int times,
                                                  String endpoint,
                                                  @Nullable @Transpose DataTable queryParams) {

        var request = new HttpEntity<>(scenarioContext.getRequestBody(), HttpHeaders.readOnlyHttpHeaders(scenarioContext.getRequestHeaders()));
        var uriBuilder = UriComponentsBuilder.fromUriString("http://localhost:" + port + endpoint);
        Optional.ofNullable(queryParams)
                .map(DataTable::asMap)
                .orElse(Map.of())
                .forEach(uriBuilder::queryParam);
        var uri = uriBuilder.build().toUri();

        requestStartTime = Instant.now();
        var response = IntStream.range(0, times).mapToObj(i -> restClient.exchange(uri, method, request, String.class))
                .peek(resp -> log.info("Response : {}", resp))
                .toList().getLast();
        log.debug("Last Response : {}", response);
        scenarioContext.setResponse(response);
    }

    @Then("the response status is {int}")
    public void theResponseStatusIs(int expectedStatus) {
        var actualStatusCode = scenarioContext.getResponse().getStatusCode();

        assertThat(actualStatusCode).isEqualTo(HttpStatusCode.valueOf(expectedStatus));
    }

    @Then("the response contains")
    public void theResponseContains(List<JsonPathExpectation> expectations) {
        var documentContext = JsonPath.parse(scenarioContext.getStringResponseBody());
        var softly = new SoftAssertions();
        expectations.forEach(exp -> {
            var jsonPath = JsonPath.compile(exp.path());
            Object actual = documentContext.read(jsonPath);
            softly.assertThat(actual).isEqualTo(exp.value());
        });
        softly.assertAll();
    }

    @Given("a prepared payload is")
    public void aPreparedPayloadIs(String payload) {
        acceptJsonPayload();
        scenarioContext.setRequestBody(payload);
    }

    private void acceptJsonPayload() {
        scenarioContext.replaceHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        scenarioContext.replaceHeader("Accept", MediaType.APPLICATION_JSON_VALUE);
    }

    @Then("the response contains a UUID at {string}")
    public void theResponseContainsUuidAt(String jsonPath) {
        var documentContext = JsonPath.parse(scenarioContext.getStringResponseBody());
        String value = documentContext.read(jsonPath);
        assertThat(value)
                .as("Value at %s should be a valid UUID", jsonPath)
                .matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$");
        // Verify it can be parsed as UUID
        assertThat(UUID.fromString(value)).isNotNull();
    }

    @Then("the response time is less than {int} milliseconds")
    public void theResponseTimeIsLessThanMilliseconds(int maxMillis) {
        Instant responseTime = Instant.now();
        long actualMillis = Duration.between(requestStartTime, responseTime).toMillis();
        assertThat(actualMillis)
                .as("Response time should be less than %d ms but was %d ms", maxMillis, actualMillis)
                .isLessThan(maxMillis);
    }
}
