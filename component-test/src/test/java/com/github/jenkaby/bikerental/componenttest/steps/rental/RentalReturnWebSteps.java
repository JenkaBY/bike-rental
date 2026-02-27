package com.github.jenkaby.bikerental.componenttest.steps.rental;

import com.github.jenkaby.bikerental.componenttest.context.ScenarioContext;
import com.github.jenkaby.bikerental.componenttest.model.RentalReturnExpectation;
import com.github.jenkaby.bikerental.rental.web.command.dto.RentalReturnResponse;
import com.github.jenkaby.bikerental.rental.web.command.dto.ReturnEquipmentRequest;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

@Slf4j
@RequiredArgsConstructor
public class RentalReturnWebSteps {

    private final ScenarioContext scenarioContext;

    @Given("the return equipment request is")
    public void theReturnEquipmentRequestIs(ReturnEquipmentRequest request) {
        log.info("Preparing return equipment request: rentalId={}, equipmentId={}, equipmentUid={}",
                request.rentalId(), request.equipmentId(), request.equipmentUid());

        // Resolve {requestedObjectId} placeholder in rentalId from scenario context
        if (request.rentalId() == null && scenarioContext.getRequestedObjectId() != null) {
            var resolvedRequest = new ReturnEquipmentRequest(
                    Long.valueOf(scenarioContext.getRequestedObjectId()),
                    request.equipmentId(),
                    request.equipmentUid(),
                    request.paymentMethod(),
                    request.operatorId()
            );
            scenarioContext.setRequestBody(resolvedRequest);
        } else {
            scenarioContext.setRequestBody(request);
        }
    }

    @Then("the rental return response contains")
    public void theRentalReturnResponseContains(RentalReturnExpectation expected) {
        var actual = scenarioContext.getResponseBody(RentalReturnResponse.class);
        log.info("Validating rental return response: status={}, finalCost={}, additionalPayment={}",
                actual.rental() != null ? actual.rental().status() : null,
                actual.cost() != null ? actual.cost().totalCost() : null,
                actual.additionalPayment());

        assertSoftly(softly -> {
            if (expected.status() != null) {
                softly.assertThat(actual.rental())
                        .as("Rental in return response")
                        .isNotNull();
                softly.assertThat(actual.rental().status())
                        .as("Rental status")
                        .isEqualTo(expected.status());
            }
            if (expected.baseCost() != null) {
                softly.assertThat(actual.cost().baseCost())
                        .as("Base cost")
                        .isEqualByComparingTo(expected.baseCost());
            }
            if (expected.overtimeCost() != null) {
                softly.assertThat(actual.cost().overtimeCost())
                        .as("Overtime cost")
                        .isEqualByComparingTo(expected.overtimeCost());
            }
            if (expected.finalCost() != null) {
                softly.assertThat(actual.cost())
                        .as("Cost breakdown in return response")
                        .isNotNull();
                softly.assertThat(actual.cost().totalCost())
                        .as("Final total cost")
                        .isEqualByComparingTo(expected.finalCost());
            }
            if (expected.actualMinutes() != null) {
                softly.assertThat(actual.cost().actualMinutes())
                        .as("Actual minutes")
                        .isEqualTo(expected.actualMinutes());
            }
            if (expected.plannedMinutes() != null) {
                softly.assertThat(actual.cost().plannedMinutes())
                        .as("Planned minutes")
                        .isEqualTo(expected.plannedMinutes());
            }
            if (expected.overtimeMinutes() != null) {
                softly.assertThat(actual.cost().overtimeMinutes())
                        .as("Overtime minutes")
                        .isEqualTo(expected.overtimeMinutes());
            }
            if (expected.forgivenessApplied() != null) {
                softly.assertThat(actual.cost().forgivenessApplied())
                        .as("Forgiveness applied")
                        .isEqualTo(expected.forgivenessApplied());
            }
            if (expected.additionalPayment() != null) {
                softly.assertThat(actual.additionalPayment())
                        .as("Additional payment")
                        .isEqualByComparingTo(expected.additionalPayment());
            }
        });

        // Save rental id to context for subsequent event validation
        if (actual.rental() != null && actual.rental().id() != null) {
            scenarioContext.setRequestedObjectId(actual.rental().id().toString());
        }
    }
}

