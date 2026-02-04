package com.github.jenkaby.bikerental.componenttest.steps.equipment;

import com.github.jenkaby.bikerental.componenttest.config.db.repository.InsertableEquipmentRepository;
import com.github.jenkaby.bikerental.equipment.infrastructure.persistence.entity.EquipmentJpaEntity;
import com.github.jenkaby.bikerental.equipment.infrastructure.persistence.repository.EquipmentJpaRepository;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.SoftAssertions;
import org.jspecify.annotations.NonNull;
import org.springframework.test.context.ContextConfiguration;

import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@ContextConfiguration
@RequiredArgsConstructor
public class EquipmentDbSteps {

    public static final Comparator<EquipmentJpaEntity> BY_SERIAL_NUMBER = Comparator.comparing(EquipmentJpaEntity::getSerialNumber);
    private final InsertableEquipmentRepository insertRepository;
    private final EquipmentJpaRepository jpaRepository;


    @Given("the following equipment record(s) exist(s) in db")
    public void theFollowingEquipmentExists(List<EquipmentJpaEntity> entities) {
        log.debug("Inserting equipments: {}", entities);
        insertRepository.insertAll(entities);
    }

    @Then("the following equipment record(s) was/were persisted in db")
    public void theFollowingEquipmentsWerePersisted(List<EquipmentJpaEntity> expected) {
        var serialNums = expected.stream()
                .map(EquipmentJpaEntity::getSerialNumber)
                .collect(Collectors.toSet());
        var sortedActual = getEquipments(actual -> serialNums.contains(actual.getSerialNumber()));

        var sortedExpected = expected.stream()
                .sorted(BY_SERIAL_NUMBER)
                .toList();

        assertThat(sortedActual)
                .as("Sizes are matched")
                .hasSize(sortedExpected.size());

        assertThat(sortedActual)
                .zipSatisfy(sortedExpected, (actual, exp) -> {
                    log.info("Comparing actual equipment: {}", actual);
                    log.info("Comparing expected equipment: {}", exp);

                    var softly = new SoftAssertions();
                    if (exp.getId() != null) {
                        softly.assertThat(actual.getId())
                                .as("Id should match for serialNumberValue: %s", exp.getSerialNumber())
                                .isEqualTo(exp.getId());
                    } else {
                        softly.assertThat(actual.getId())
                                .as("Id should not be null for serialNumberValue: %s", exp.getSerialNumber())
                                .isNotNull();
                    }
                    softly.assertThat(actual.getSerialNumber())
                            .as("Serial number should match")
                            .isEqualTo(exp.getSerialNumber());
                    softly.assertThat(actual.getUid())
                            .as("UID should match for serialNumberValue: %s", exp.getSerialNumber())
                            .isEqualTo(exp.getUid());
                    softly.assertThat(actual.getStatusSlug())
                            .as("Status slug should match for serialNumberValue: %s", exp.getSerialNumber())
                            .isEqualTo(exp.getStatusSlug());
                    softly.assertThat(actual.getTypeSlug())
                            .as("Equipment type slug should match for serialNumberValue: %s", exp.getSerialNumber())
                            .isEqualTo(exp.getTypeSlug());
                    softly.assertThat(actual.getModel())
                            .as("Model should match for serialNumberValue: %s", exp.getSerialNumber())
                            .isEqualTo(exp.getModel());
                    softly.assertThat(actual.getCommissionedAt())
                            .as("Commissioned date should match for serialNumberValue: %s", exp.getSerialNumber())
                            .isEqualTo(exp.getCommissionedAt());
                    softly.assertThat(actual.getCondition())
                            .as("Condition should match for serialNumberValue: %s", exp.getSerialNumber())
                            .isEqualTo(exp.getCondition());
                    softly.assertAll();
                });


    }

    private @NonNull List<EquipmentJpaEntity> getEquipments(Predicate<EquipmentJpaEntity> filter) {
        var allActual = jpaRepository.findAll();

        return allActual.stream()
                .filter(filter)
                .sorted(BY_SERIAL_NUMBER)
                .toList();
    }

}
