package com.github.jenkaby.bikerental.componenttest.steps.rental;

import com.github.jenkaby.bikerental.componenttest.context.ScenarioContext;
import com.github.jenkaby.bikerental.rental.web.command.dto.AddRentalEquipmentRequest;
import io.cucumber.java.en.Given;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class RentalAddEquipmentWebSteps {

    private final ScenarioContext scenarioContext;

    @Given("the add equipment request is")
    public void theAddEquipmentRequestIs(AddRentalEquipmentRequest request) {
        log.info("Preparing add equipment request: {}", request);
        scenarioContext.setRequestBody(request);
    }
}
