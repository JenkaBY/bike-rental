package com.github.jenkaby.bikerental.componenttest.steps.agreement;

import com.github.jenkaby.bikerental.componenttest.context.ScenarioContext;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@RequiredArgsConstructor
public class AgreementPdfSteps {

    private final TestRestTemplate restClient;
    private final ScenarioContext scenarioContext;
    @LocalServerPort
    private final int port;

    private ResponseEntity<byte[]> pdfResponse;

    @When("a POST request for a PDF has been made to {string} endpoint")
    public void aPostRequestForPdfHasBeenMade(String endpoint) {
        scenarioContext.replaceHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_PDF_VALUE);
        var headers = HttpHeaders.readOnlyHttpHeaders(scenarioContext.getRequestHeaders());
        var request = new HttpEntity<>(scenarioContext.getRequestBody(), headers);
        var uri = "http://localhost:" + port + endpoint;
        pdfResponse = restClient.exchange(uri, HttpMethod.POST, request, byte[].class);
        log.info("PDF response status: {}, body length: {}", pdfResponse.getStatusCode(),
                pdfResponse.getBody() == null ? 0 : pdfResponse.getBody().length);
    }

    @Then("the PDF response status is {int}")
    public void thePdfResponseStatusIs(int expectedStatus) {
        assertThat(pdfResponse.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(expectedStatus));
    }

    @Then("the PDF response content type is {string}")
    public void thePdfResponseContentTypeIs(String expectedContentType) {
        var actual = pdfResponse.getHeaders().getContentType();
        assertThat(actual).isNotNull();
        assertThat(actual.toString()).isEqualTo(expectedContentType);
    }

    @Then("the PDF body is a valid document containing text {string}")
    public void thePdfBodyContainsText(String expectedPhrase) throws IOException {
        var body = pdfResponse.getBody();
        assertThat(body).as("PDF body").isNotNull();
        assertThat(body.length).as("PDF body length").isPositive();
        try (PDDocument document = Loader.loadPDF(body)) {
            var text = new PDFTextStripper().getText(document);
            assertThat(text).contains(expectedPhrase);
        }
    }

    @Then("the PDF body has more than {int} page")
    public void thePdfBodyHasMoreThanPages(int minimumPages) throws IOException {
        var body = pdfResponse.getBody();
        assertThat(body).as("PDF body").isNotNull();
        try (PDDocument document = Loader.loadPDF(body)) {
            assertThat(document.getNumberOfPages()).isGreaterThan(minimumPages);
        }
    }
}
