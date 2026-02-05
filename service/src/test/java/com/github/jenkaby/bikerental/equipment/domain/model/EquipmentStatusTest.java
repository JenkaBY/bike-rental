package com.github.jenkaby.bikerental.equipment.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("EquipmentStatus")
class EquipmentStatusTest {

    @Test
    @DisplayName("isAllowedTransitionTo - positive case")
    void isAllowedTransitionTo_positive() {
        var fromStatus = EquipmentStatus.builder()
                .slug("available")
                .allowedTransitions(Set.of("in-use", "maintenance"))
                .build();

        var toStatus = EquipmentStatus.builder()
                .slug("in-use")
                .allowedTransitions(Set.of())
                .build();

        assertThat(fromStatus.canTransitionTo(toStatus)).isTrue();
    }

    @Test
    @DisplayName("isAllowedTransitionTo - negative case")
    void isAllowedTransitionTo_negative() {
        var fromStatus = EquipmentStatus.builder()
                .slug("available")
                .allowedTransitions(Set.of("in-use", "maintenance"))
                .build();

        var toStatus = EquipmentStatus.builder()
                .slug("retired")
                .allowedTransitions(Set.of())
                .build();

        assertThat(fromStatus.canTransitionTo(toStatus)).isFalse();
    }
}
