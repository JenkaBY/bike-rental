<task_file_template>

# Task 015: Create `AgreementPdfSteps` (binary-capable preview step + PDFBox assertions)

> **Applied Skill:** `.claude/rules/component-tests.md` / `spring-boot-java-cucumber` — steps in
> `steps/agreement/`, constructor-injected collaborators, AssertJ only, happy-path scenarios. The generic
> `WebRequestSteps` captures the response body as `String` (corrupting binary PDF bytes), so binary responses need a
> dedicated step that requests `byte[].class` via the same `TestRestTemplate` + `@LocalServerPort` mechanism.

## 1. Objective

Add step definitions that: POST the prepared JSON payload to the preview endpoint requesting `Accept: application/pdf`,
capture the raw `byte[]` body and status, then assert the response status, content-type, that the body parses as a PDF
whose extracted text contains an expected (Cyrillic) phrase, and that the page count exceeds a given number. Depends on
Task 014 (PDFBox on the test classpath) and Task 011 (endpoint).

## 2. File to Modify / Create

* **File Path:** `component-test/src/test/java/com/github/jenkaby/bikerental/componenttest/steps/agreement/AgreementPdfSteps.java`
* **Action:** Create New File

## 3. Code Implementation

Create the file with EXACTLY this content:

```java
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
```

> Binary discipline: the body MUST be requested as `byte[].class` (never `String.class`) or the PDF bytes corrupt.
> PDFBox 3.x loads bytes via `org.apache.pdfbox.Loader.loadPDF(byte[])` (NOT the removed `PDDocument.load`).
> This step reuses the same `TestRestTemplate` + `@LocalServerPort` machinery as `WebRequestSteps`; it does not touch
> `ScenarioContext.response` (which is typed `ResponseEntity<String>`).

## 4. Validation Steps

Execute the following command (assumes the DB is already up). Do NOT run the full application server.

```bash
./gradlew :component-test:test "-Dspring.profiles.active=test"
```

</task_file_template>
