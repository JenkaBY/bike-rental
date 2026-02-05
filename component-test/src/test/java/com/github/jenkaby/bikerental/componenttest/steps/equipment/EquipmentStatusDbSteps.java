package com.github.jenkaby.bikerental.componenttest.steps.equipment;


import com.github.jenkaby.bikerental.componenttest.config.db.repository.InsertableEquipmentStatusRepository;
import com.github.jenkaby.bikerental.equipment.infrastructure.persistence.entity.EquipmentStatusJpaEntity;
import com.github.jenkaby.bikerental.equipment.infrastructure.persistence.repository.EquipmentStatusJpaRepository;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.SoftAssertions;
import org.jspecify.annotations.NonNull;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@RequiredArgsConstructor
public class EquipmentStatusDbSteps {

    public static final Comparator<EquipmentStatusJpaEntity> DEFAULT_COMPARING = Comparator.comparing(EquipmentStatusJpaEntity::getSlug);
    private final InsertableEquipmentStatusRepository repository;
    private final EquipmentStatusJpaRepository jpaRepository;

    @Given("the following equipment statues exist in the database")
    public void theFollowingEquipmentExists(List<EquipmentStatusJpaEntity> entities) {
        repository.insertAll(entities);
    }

    @Transactional(readOnly = true)
    @Then("the following equipment status record(s) was/were persisted in db")
    public void theFollowingEquipmentStatusesWerePersisted(List<EquipmentStatusJpaEntity> expected) {
        var serialNums = expected.stream()
                .map(EquipmentStatusJpaEntity::getSlug)
                .collect(Collectors.toSet());
        var sortedActual = getEquipments(actual -> serialNums.contains(actual.getSlug()));

        var sortedExpected = expected.stream()
                .sorted(DEFAULT_COMPARING)
                .toList();

        assertThat(sortedActual)
                .as("Sizes are matched")
                .hasSize(sortedExpected.size());

        assertThat(sortedActual)
                .zipSatisfy(sortedExpected, (actual, exp) -> {
                    log.debug("Comparing actual equipment: {}", actual);
                    log.debug("Comparing expected equipment: {}", exp);

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
                    softly.assertThat(actual.getAllowedTransitionSlugs())
                            .as("Allowed transitions should match: %s", exp.getAllowedTransitionSlugs())
                            .isEqualTo(exp.getAllowedTransitionSlugs());
                    softly.assertAll();
                });
    }

    private @NonNull List<EquipmentStatusJpaEntity> getEquipments(Predicate<EquipmentStatusJpaEntity> filter) {
        var allActual = jpaRepository.findAll();

        return allActual.stream()
                .filter(filter)
                .sorted(DEFAULT_COMPARING)
                .toList();
    }
}
