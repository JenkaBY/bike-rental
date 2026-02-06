package com.github.jenkaby.bikerental.equipment.domain.model;

import com.github.jenkaby.bikerental.equipment.domain.exception.InvalidStatusTransitionException;
import com.github.jenkaby.bikerental.equipment.domain.service.StatusTransitionPolicy;
import com.github.jenkaby.bikerental.equipment.shared.domain.model.vo.SerialNumber;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class EquipmentTest {

    @Test
    void changeStatusTo_shouldUpdateWhenTransitionAllowed() {
        var currentStatus = "old";
        var newStatus = "new";
        var policy = mock(StatusTransitionPolicy.class);
        doNothing().when(policy).validateTransition(currentStatus, newStatus);
        var equipment = Equipment.builder()
                .id(1L)
                .serialNumber(new SerialNumber("BK-001"))
                .typeSlug("bicycle")
                .statusSlug(currentStatus)
                .build();

        equipment.changeStatusTo(newStatus, policy);

        Assertions.assertThat(equipment.getStatusSlug()).isEqualTo(newStatus);
        verify(policy).validateTransition(currentStatus, newStatus);
    }

    @Test
    void changeStatusTo_shouldThrowWhenTransitionNotAllowed() {
        var currentStatus = "old";
        var newStatus = "new";
        var policy = mock(StatusTransitionPolicy.class);
        doThrow(new InvalidStatusTransitionException("any", currentStatus, newStatus))
                .when(policy).validateTransition(currentStatus, newStatus);
        var equipment = Equipment.builder()
                .id(1L)
                .serialNumber(new SerialNumber("BK-001"))
                .typeSlug("bicycle")
                .statusSlug(currentStatus)
                .build();

        Assertions.assertThatThrownBy(() -> equipment.changeStatusTo(newStatus, policy))
                .isInstanceOf(InvalidStatusTransitionException.class);

        // Ensure status was not changed
        Assertions.assertThat(equipment.getStatusSlug()).isEqualTo(currentStatus);
        verify(policy).validateTransition(currentStatus, newStatus);
    }
}
