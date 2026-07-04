package com.github.jenkaby.bikerental.componenttest.steps.agreement;

import com.github.jenkaby.bikerental.agreement.infrastructure.persistence.entity.AgreementTemplateJpaEntity;
import com.github.jenkaby.bikerental.componenttest.config.db.repository.InsertableAgreementTemplateRepository;
import com.github.jenkaby.bikerental.componenttest.context.ScenarioContext;
import com.jayway.jsonpath.JsonPath;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

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

    @When("the created agreement template id is stored as 'requestedObjectId'")
    public void theCreatedAgreementTemplateIdIsStoredAsRequestedObjectId() {
        var id = JsonPath.parse(scenarioContext.getStringResponseBody()).read("$.id").toString();
        scenarioContext.setRequestedObjectId(id);
    }
}
