package com.github.jenkaby.bikerental.equipment.domain.model;

import com.github.jenkaby.bikerental.equipment.shared.domain.model.vo.SerialNumber;
import com.github.jenkaby.bikerental.equipment.shared.domain.model.vo.Uid;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

class EquipmentTest {

    @Test
    void shouldCreateEquipmentWithValidData() {
        var equipment = Equipment.builder()
                .id(1L)
                .serialNumber(new SerialNumber("BK-001"))
                .uid(new Uid("ABC123"))
                .typeSlug("bicycle")
                .statusSlug("available")
                .model("Mountain Bike")
                .commissionedAt(LocalDate.of(2023, 1, 1))
                .condition("Good condition")
                .build();

        Assertions.assertThat(equipment.getId()).isEqualTo(1L);
        Assertions.assertThat(equipment.getSerialNumber().value()).isEqualTo("BK-001");
        Assertions.assertThat(equipment.getUid().value()).isEqualTo("ABC123");
        Assertions.assertThat(equipment.getTypeSlug()).isEqualTo("bicycle");
        Assertions.assertThat(equipment.getStatusSlug()).isEqualTo("available");
        Assertions.assertThat(equipment.getModel()).isEqualTo("Mountain Bike");
        Assertions.assertThat(equipment.getCommissionedAt()).isEqualTo(LocalDate.of(2023, 1, 1));
        Assertions.assertThat(equipment.getCondition()).isEqualTo("Good condition");
    }

    @Test
    void shouldAllowNullValuesForOptionalFields() {
        var equipment = Equipment.builder()
                .id(1L)
                .serialNumber(new SerialNumber("BK-001"))
                .typeSlug("bicycle")
                .statusSlug("available")
                .build();

        Assertions.assertThat(equipment.getUid()).isNull();
        Assertions.assertThat(equipment.getModel()).isNull();
        Assertions.assertThat(equipment.getCommissionedAt()).isNull();
        Assertions.assertThat(equipment.getCondition()).isNull();
    }
}
