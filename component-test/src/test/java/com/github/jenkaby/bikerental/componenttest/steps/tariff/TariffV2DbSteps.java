package com.github.jenkaby.bikerental.componenttest.steps.tariff;

import com.github.jenkaby.bikerental.componenttest.config.WebConfig;
import com.github.jenkaby.bikerental.componenttest.config.db.repository.InsertableTariffV2Repository;
import com.github.jenkaby.bikerental.componenttest.context.ScenarioContext;
import com.github.jenkaby.bikerental.tariff.infrastructure.persistence.entity.TariffV2JpaEntity;
import com.github.jenkaby.bikerental.tariff.infrastructure.persistence.repository.TariffV2JpaRepository;
import io.cucumber.java.en.Given;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class TariffV2DbSteps {

    private static final Comparator<TariffV2JpaEntity> DEFAULT_COMPARATOR = Comparator.comparing(TariffV2JpaEntity::getEquipmentType)
            .thenComparing(TariffV2JpaEntity::getValidFrom);
    private final InsertableTariffV2Repository insertRepository;
    private final TariffV2JpaRepository jpaRepository;
    private final ScenarioContext scenarioContext;


    @SuppressWarnings("unchecked")
    @Given("the following tariff v2 record(s) exist(s) in db")
    public void theFollowingTariffsV2Exist(List<TariffV2JpaEntity> entities) {
        var paramsContext = scenarioContext.getPricingParamsContext();
        entities.forEach(e ->
                e.setParams(WebConfig.DEFAULT_OBJECT_MAPPER.convertValue(paramsContext.get(e.getId()), Map.class)));
        log.debug("Inserting tariffs: {}", entities);
        insertRepository.insertAll(entities);
    }

}
