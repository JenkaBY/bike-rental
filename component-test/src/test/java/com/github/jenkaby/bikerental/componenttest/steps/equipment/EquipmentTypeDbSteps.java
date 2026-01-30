package com.github.jenkaby.bikerental.componenttest.steps.equipment;


import com.github.jenkaby.bikerental.componenttest.config.db.repository.InsertableEquipmentTypeRepository;
import com.github.jenkaby.bikerental.equipment.infrastructure.persistence.entity.EquipmentTypeJpaEntity;
import io.cucumber.java.en.Given;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class EquipmentTypeDbSteps {

    private final InsertableEquipmentTypeRepository repository;

    @Given("the following equipment types exist in the database")
    public void theFollowingEquipmentExists(List<EquipmentTypeJpaEntity> entities) {
        repository.insertAll(entities);
    }
}
