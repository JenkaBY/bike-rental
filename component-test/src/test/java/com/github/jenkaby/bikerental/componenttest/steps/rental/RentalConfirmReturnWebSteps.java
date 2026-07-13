package com.github.jenkaby.bikerental.componenttest.steps.rental;

import com.github.jenkaby.bikerental.componenttest.context.ScenarioContext;
import com.github.jenkaby.bikerental.rental.web.command.dto.ConfirmReturnRequest;
import io.cucumber.java.en.Given;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class RentalConfirmReturnWebSteps {

    private final ScenarioContext scenarioContext;

    @Given("the confirm return request is")
    public void theConfirmReturnRequestIs(ConfirmReturnRequest request) {
        var resolvedRequest = new ConfirmReturnRequest(scenarioContext.getQuoteId(), request.operatorId());
        log.info("Preparing confirm return request: quoteId={}, operatorId={}",
                resolvedRequest.quoteId(), resolvedRequest.operatorId());
        scenarioContext.setRequestBody(resolvedRequest);
    }
}
