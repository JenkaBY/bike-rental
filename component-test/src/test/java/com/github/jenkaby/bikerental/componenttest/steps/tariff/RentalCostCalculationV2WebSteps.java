package com.github.jenkaby.bikerental.componenttest.steps.tariff;

import com.github.jenkaby.bikerental.componenttest.context.ScenarioContext;
import com.github.jenkaby.bikerental.componenttest.model.CostCalculationV2RequestBuilder;
import com.github.jenkaby.bikerental.tariff.web.query.dto.CostCalculationV2Request;
import io.cucumber.java.en.Given;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@RequiredArgsConstructor
@Slf4j
public class RentalCostCalculationV2WebSteps {

    private final ScenarioContext scenarioContext;

    @Given("the equipment items for cost calculation request are")
    public void theEquipmentItemsForCostCalculationRequestAre(List<CostCalculationV2Request.EquipmentItemRequest> equipmentItems) {
        scenarioContext.setEquipmentItems(equipmentItems);
    }

    @Given("the rental cost calculation request is prepared with the following data")
    public void theRentalCostCalculationRequestIsPrepared(CostCalculationV2RequestBuilder builder) {
        var request = builder.toRequest(scenarioContext.getEquipmentItems());
        log.info("Set V2 rental cost calculation request body: {}", request);
        scenarioContext.setRequestBody(request);
    }
}
