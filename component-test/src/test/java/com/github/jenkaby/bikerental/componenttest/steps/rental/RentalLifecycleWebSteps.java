package com.github.jenkaby.bikerental.componenttest.steps.rental;

import com.github.jenkaby.bikerental.componenttest.context.ScenarioContext;
import com.github.jenkaby.bikerental.rental.web.command.dto.RentalLifecycleRequest;
import io.cucumber.java.en.Given;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class RentalLifecycleWebSteps {

    private final ScenarioContext scenarioContext;

    @Given("the lifecycle request is")
    public void theLifecycleRequestIs(RentalLifecycleRequest request) {
        log.info("Preparing lifecycle request: {}", request);
        scenarioContext.setRequestBody(request);
    }
}