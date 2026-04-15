package com.github.jenkaby.bikerental.componenttest.steps.rental;

import com.github.jenkaby.bikerental.componenttest.context.ScenarioContext;
import com.github.jenkaby.bikerental.rental.web.command.dto.RentalReturnResponse;
import com.github.jenkaby.bikerental.rental.web.command.dto.ReturnEquipmentRequest;
import com.github.jenkaby.bikerental.rental.web.query.dto.EquipmentItemResponse;
import com.github.jenkaby.bikerental.rental.web.query.dto.RentalResponse;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Condition;
import org.springframework.http.ResponseEntity;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;


@Slf4j
@RequiredArgsConstructor
public class RentalReturnWebSteps {

    private final ScenarioContext scenarioContext;
    private final RentalWebSteps rentalWebSteps;
    private final ObjectMapper objectMapper;

    @Given("the return equipment request is")
    public void theReturnEquipmentRequestIs(ReturnEquipmentRequest request) {
        log.info("Preparing return equipment request: rentalId={}, equipmentId={}, equipmentUid={}",
                request.rentalId(), request.equipmentIds(), request.equipmentUids());

        if (request.rentalId() == null && scenarioContext.getRequestedObjectId() != null) {
            var resolvedRequest = new ReturnEquipmentRequest(
                    Long.valueOf(scenarioContext.getRequestedObjectId()),
                    request.equipmentIds(),
                    request.equipmentUids(),
                    request.paymentMethod(),
                    request.operatorId()
            );
            scenarioContext.setRequestBody(resolvedRequest);
        } else {
            scenarioContext.setRequestBody(request);
        }
    }

    @Then("the rental return response contains rental")
    public void theRentalReturnResponseContains(RentalResponse expected) {
        var actual = scenarioContext.getResponseBody(RentalReturnResponse.class);

        var temp = scenarioContext.getResponse();
        String actualRentalStr = objectMapper.writeValueAsString(actual.rental());
        scenarioContext.setResponse(ResponseEntity.of(Optional.of(actualRentalStr)));

        this.rentalWebSteps.theRentalResponseOnlyContains(expected);

        scenarioContext.setResponse(temp);
    }

    @Then("the rental return response contains rental equipments")
    public void theRentalReturnResponseContains(List<EquipmentItemResponse> expected) {
        var actual = scenarioContext.getResponseBody(RentalReturnResponse.class);

        var temp = scenarioContext.getResponse();
        String actualRentalStr = objectMapper.writeValueAsString(actual.rental());
        scenarioContext.setResponse(ResponseEntity.of(Optional.of(actualRentalStr)));

        this.rentalWebSteps.rentalResponseContainsEquipments(expected);

        scenarioContext.setResponse(temp);
    }

    @Then("the rental return response {booleanDo} contain settlement info")
    public void theRentalReturnResponseContainsSettleInfo(Boolean shouldContain) {
        var actual = scenarioContext.getResponseBody(RentalReturnResponse.class);
        log.info("Validating rental return response: {}", actual);
        assertThat(actual.settlement()).is(new Condition<>(
                settlement -> shouldContain == (settlement != null),
                shouldContain ? "contains settlement info" : "does not contain settlement info"
        ));

    }
}

