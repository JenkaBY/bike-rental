package com.github.jenkaby.bikerental.equipment.domain.model;

import com.github.jenkaby.bikerental.equipment.domain.exception.InvalidStatusTransitionException;
import com.github.jenkaby.bikerental.equipment.shared.domain.model.vo.SerialNumber;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class EquipmentTest {

    @Test
    void setInitialStatus_shouldAssignStatus() {
        var initialStatus = mock(EquipmentStatus.class);

        var equipment = Equipment.builder()
                .id(1L)
                .serialNumber(new SerialNumber("BK-001"))
                .typeSlug("bicycle")
                .build();

        equipment.setInitialStatus(initialStatus);

        Assertions.assertThat(equipment.getStatus()).isEqualTo(initialStatus);
    }

    @Test
    void changeStatusTo_shouldUpdateWhenTransitionAllowed() {
        var currentStatus = mock(EquipmentStatus.class);
        var newStatus = mock(EquipmentStatus.class);

        when(currentStatus.canTransitionTo(newStatus)).thenReturn(true);

        var equipment = Equipment.builder()
                .id(1L)
                .serialNumber(new SerialNumber("BK-001"))
                .typeSlug("bicycle")
                .status(currentStatus)
                .build();

        equipment.changeStatusTo(newStatus);

        Assertions.assertThat(equipment.getStatus()).isEqualTo(newStatus);
        verify(currentStatus).canTransitionTo(newStatus);
    }

    @Test
    void changeStatusTo_shouldThrowWhenTransitionNotAllowed() {
        var currentStatus = mock(EquipmentStatus.class);
        var newStatus = mock(EquipmentStatus.class);

        when(currentStatus.canTransitionTo(newStatus)).thenReturn(false);

        var equipment = Equipment.builder()
                .id(1L)
                .serialNumber(new SerialNumber("BK-001"))
                .typeSlug("bicycle")
                .status(currentStatus)
                .build();

        Assertions.assertThatThrownBy(() -> equipment.changeStatusTo(newStatus))
                .isInstanceOf(InvalidStatusTransitionException.class);

        // Ensure status was not changed
        Assertions.assertThat(equipment.getStatus()).isEqualTo(currentStatus);
        verify(currentStatus).canTransitionTo(newStatus);
    }
}
