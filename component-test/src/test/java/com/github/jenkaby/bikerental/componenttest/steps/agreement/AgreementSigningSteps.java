package com.github.jenkaby.bikerental.componenttest.steps.agreement;

import com.github.jenkaby.bikerental.agreement.web.command.dto.SignAgreementRequest;
import com.github.jenkaby.bikerental.agreement.web.command.dto.SignatureCreatedResponse;
import com.github.jenkaby.bikerental.agreement.web.query.dto.SignatureSummaryResponse;
import com.github.jenkaby.bikerental.componenttest.context.ScenarioContext;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.SoftAssertions;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@RequiredArgsConstructor
public class AgreementSigningSteps {

    private final ScenarioContext scenarioContext;

    @Given("the sign agreement request is")
    public void theSignAgreementRequestIs(SignAgreementRequest request) {
        log.info("Preparing sign agreement request: templateId={}, rentalVersion={}, operatorId={}",
                request.templateId(), request.rentalVersion(), request.operatorId());
        scenarioContext.setRequestBody(request);
    }

    @Then("the signature response contains a signature id and signed timestamp")
    public void theSignatureResponseContains() {
        var actual = scenarioContext.getResponseBody(SignatureCreatedResponse.class);
        var softly = new SoftAssertions();
        softly.assertThat(actual.signatureId()).as("Signature id").isNotNull();
        softly.assertThat(actual.signedAt()).as("Signed at").isNotNull();
        softly.assertAll();
    }

    @Then("the signature list has size {int}")
    public void theSignatureListHasSize(int expectedSize) {
        List<SignatureSummaryResponse> actual = scenarioContext.getResponseAsList(SignatureSummaryResponse.class);
        assertThat(actual).as("Signature summary list").hasSize(expectedSize);
    }

    @Then("the signature list contains")
    public void theSignatureListContains(List<SignatureSummaryResponse> expectedRows) {
        List<SignatureSummaryResponse> actual = scenarioContext.getResponseAsList(SignatureSummaryResponse.class);
        var softly = new SoftAssertions();
        softly.assertThat(actual).as("Signature summary list size").hasSize(expectedRows.size());
        for (int i = 0; i < expectedRows.size(); i++) {
            var expected = expectedRows.get(i);
            var row = actual.get(i);
            softly.assertThat(row.signatureId()).as("signatureId[%d]", i).isNotNull();
            if (expected.templateId() != null) {
                softly.assertThat(row.templateId()).as("templateId[%d]", i).isEqualTo(expected.templateId());
            }
            if (expected.templateVersionNumber() != null) {
                softly.assertThat(row.templateVersionNumber()).as("templateVersionNumber[%d]", i)
                        .isEqualTo(expected.templateVersionNumber());
            }
            softly.assertThat(row.signedAt()).as("signedAt[%d]", i).isNotNull();
        }
        softly.assertAll();
    }
}
