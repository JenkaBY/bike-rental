package com.github.jenkaby.bikerental.componenttest.steps.equipment;


import com.github.jenkaby.bikerental.componenttest.config.db.repository.InsertableEquipmentStatusRepository;
import com.github.jenkaby.bikerental.equipment.infrastructure.persistence.entity.EquipmentStatusJpaEntity;
import io.cucumber.java.en.Given;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class EquipmentStatusDbSteps {

    private final InsertableEquipmentStatusRepository repository;

    @Given("the following equipment statues exist in the database")
    public void theFollowingEquipmentExists(List<EquipmentStatusJpaEntity> entities) {
        repository.insertAll(entities);
    }
}
