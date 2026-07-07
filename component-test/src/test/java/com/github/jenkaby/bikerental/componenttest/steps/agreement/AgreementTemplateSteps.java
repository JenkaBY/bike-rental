package com.github.jenkaby.bikerental.componenttest.steps.agreement;

import com.github.jenkaby.bikerental.agreement.infrastructure.persistence.entity.AgreementTemplateJpaEntity;
import com.github.jenkaby.bikerental.agreement.web.command.dto.AgreementTemplateRequest;
import com.github.jenkaby.bikerental.agreement.web.query.dto.AgreementTemplateResponse;
import com.github.jenkaby.bikerental.agreement.web.query.dto.AgreementTemplateVariableResponse;
import com.github.jenkaby.bikerental.componenttest.config.db.repository.InsertableAgreementTemplateRepository;
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
public class AgreementTemplateSteps {

    private final InsertableAgreementTemplateRepository agreementTemplateRepository;
    private final ScenarioContext scenarioContext;

    @Given("agreement templates exist in the database with the following data")
    public void agreementTemplatesExistInTheDatabase(List<AgreementTemplateJpaEntity> templates) {
        log.debug("Seeding agreement templates: {}", templates);
        agreementTemplateRepository.insertAll(templates);
    }

    @Given("a single agreement template exists in the database with the following data")
    public void aSingleAgreementTemplateExistsInTheDatabase(AgreementTemplateJpaEntity template) {
        log.debug("Seeding agreement template: {}", template);
        var inserted = agreementTemplateRepository.insert(template);
        scenarioContext.setRequestedObjectId(inserted.getId().toString());
    }

    @Given("the agreement template request is")
    public void theAgreementTemplateRequestIs(AgreementTemplateRequest request) {
        log.info("Preparing agreement template request: {}", request);
        scenarioContext.setRequestBody(request);
    }

    @Then("the agreement template response contains")
    public void theAgreementTemplateResponseContains(AgreementTemplateResponse expected) {
        var actual = scenarioContext.getResponseBody(AgreementTemplateResponse.class);
        var softly = new SoftAssertions();
        softly.assertThat(actual.id()).as("Template id").isNotNull();
        if (expected.title() != null) {
            softly.assertThat(actual.title()).as("Title").isEqualTo(expected.title());
        }
        if (expected.content() != null) {
            softly.assertThat(actual.content()).as("Content").isEqualTo(expected.content());
        }
        if (expected.status() != null) {
            softly.assertThat(actual.status()).as("Status").isEqualTo(expected.status());
        }
        if (expected.versionNumber() != null) {
            softly.assertThat(actual.versionNumber()).as("Version number").isEqualTo(expected.versionNumber());
        }
        softly.assertAll();
        scenarioContext.setRequestedObjectId(actual.id().toString());
    }

    @Then("the agreement template variables response contains")
    public void theAgreementTemplateVariablesResponseContains(List<AgreementTemplateVariableResponse> expectedRows) {
        List<AgreementTemplateVariableResponse> actual = scenarioContext.getResponseAsList(AgreementTemplateVariableResponse.class);
        assertThat(actual).as("Template variable catalog size").hasSize(expectedRows.size());
        assertThat(actual).zipSatisfy(expectedRows, (row, expected) -> {
            assertThat(row.key()).as("key").isEqualTo(expected.key());
            assertThat(row.description()).as("description").isEqualTo(expected.description());
            assertThat(row.example()).as("example").isEqualTo(expected.example());
        });
    }
}
