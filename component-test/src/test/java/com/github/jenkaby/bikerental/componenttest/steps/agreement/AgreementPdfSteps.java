package com.github.jenkaby.bikerental.componenttest.steps.agreement;

import com.github.jenkaby.bikerental.agreement.web.command.dto.AgreementPdfPreviewRequest;
import com.github.jenkaby.bikerental.componenttest.context.ScenarioContext;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@RequiredArgsConstructor
public class AgreementPdfSteps {

    private final ScenarioContext scenarioContext;

    @Given("the agreement pdf preview request is")
    public void theAgreementPdfPreviewRequestIs(AgreementPdfPreviewRequest request) {
        log.info("Preparing agreement pdf preview request with title: {}", request.title());
        scenarioContext.setRequestBody(request);
    }

    @Then("the PDF body is a valid document containing text {string}")
    public void thePdfBodyContainsText(String expectedPhrase) throws IOException {
        var body = scenarioContext.getBinaryResponse().getBody();
        assertThat(body).as("PDF body").isNotNull();
        assertThat(body.length).as("PDF body length").isPositive();
        try (PDDocument document = Loader.loadPDF(body)) {
            var text = new PDFTextStripper().getText(document);
            assertThat(text).contains(expectedPhrase);
        }
    }

    @Then("the PDF body has more than {int} page")
    public void thePdfBodyHasMoreThanPages(int minimumPages) throws IOException {
        var body = scenarioContext.getBinaryResponse().getBody();
        assertThat(body).as("PDF body").isNotNull();
        try (PDDocument document = Loader.loadPDF(body)) {
            assertThat(document.getNumberOfPages()).isGreaterThan(minimumPages);
        }
    }

    @Then("the PDF body is a valid document not containing text {string}")
    public void thePdfBodyDoesNotContainText(String unexpectedPhrase) throws IOException {
        var body = scenarioContext.getBinaryResponse().getBody();
        assertThat(body).as("PDF body").isNotNull();
        try (PDDocument document = Loader.loadPDF(body)) {
            var text = new PDFTextStripper().getText(document);
            assertThat(text).doesNotContain(unexpectedPhrase);
        }
    }

    @Then("the PDF body is a valid document matching pattern {string}")
    public void thePdfBodyMatchesPattern(String pattern) throws IOException {
        var body = scenarioContext.getBinaryResponse().getBody();
        assertThat(body).as("PDF body").isNotNull();
        try (PDDocument document = Loader.loadPDF(body)) {
            var text = new PDFTextStripper().getText(document);
            assertThat(text).containsPattern(pattern);
        }
    }
}
