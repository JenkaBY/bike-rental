package com.github.jenkaby.bikerental.componenttest.steps.equipment;


import com.github.jenkaby.bikerental.componenttest.config.db.repository.InsertableEquipmentTypeRepository;
import com.github.jenkaby.bikerental.equipment.infrastructure.persistence.entity.EquipmentTypeJpaEntity;
import com.github.jenkaby.bikerental.equipment.infrastructure.persistence.repository.EquipmentTypeJpaRepository;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.SoftAssertions;
import org.jspecify.annotations.NonNull;

import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@RequiredArgsConstructor
public class EquipmentTypeDbSteps {

    public static final Comparator<EquipmentTypeJpaEntity> DEFAULT_COMPARING = Comparator.comparing(EquipmentTypeJpaEntity::getSlug);
    private final InsertableEquipmentTypeRepository repository;
    private final EquipmentTypeJpaRepository jpaRepository;

    @Given("the following equipment types exist in the database")
    public void theFollowingEquipmentExists(List<EquipmentTypeJpaEntity> entities) {
        repository.insertAll(entities);
    }

    @Then("the following equipment type record(s) was/were persisted in db")
    public void theFollowingEquipmentTypesWerePersisted(List<EquipmentTypeJpaEntity> expected) {
        var slugs = expected.stream()
                .map(EquipmentTypeJpaEntity::getSlug)
                .collect(Collectors.toSet());
        var sortedActual = getEquipmentTypes(actual -> slugs.contains(actual.getSlug()));

        var sortedExpected = expected.stream()
                .sorted(DEFAULT_COMPARING)
                .toList();

        assertThat(sortedActual)
                .as("Sizes are matched")
                .hasSize(sortedExpected.size());

        assertThat(sortedActual)
                .zipSatisfy(sortedExpected, (actual, exp) -> {
                    log.info("Comparing actual equipment type: {}", actual);
                    log.info("Comparing expected equipment type: {}", exp);

                    var softly = new SoftAssertions();

                    softly.assertThat(actual.getId())
                            .as("Id should not be null")
                            .isNotNull();
                    softly.assertThat(actual.getSlug())
                            .as("Slug should match for slug: %s", exp.getSlug())
                            .isEqualTo(exp.getSlug());
                    softly.assertThat(actual.getName())
                            .as("Name should match for slug: %s", exp.getSlug())
                            .isEqualTo(exp.getName());
                    softly.assertThat(actual.getDescription())
                            .as("Description should match for slug: %s", exp.getSlug())
                            .isEqualTo(exp.getDescription());

                    softly.assertAll();
                });
    }

    private @NonNull List<EquipmentTypeJpaEntity> getEquipmentTypes(Predicate<EquipmentTypeJpaEntity> filter) {
        var allActual = jpaRepository.findAll();

        return allActual.stream()
                .filter(filter)
                .sorted(DEFAULT_COMPARING)
                .toList();
    }
}
