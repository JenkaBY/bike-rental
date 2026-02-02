package com.github.jenkaby.bikerental.equipment.domain.model.vo;

import com.github.jenkaby.bikerental.equipment.shared.domain.model.vo.SerialNumber;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class SerialNumberTest {

    @Test
    void shouldCreateSerialNumberWithValidValue() {
        var serial = new SerialNumber("BK-001");
        Assertions.assertThat(serial.value()).isEqualTo("BK-001");
    }

    @Test
    void shouldThrowExceptionForNullValue() {
        Assertions.assertThatThrownBy(() -> new SerialNumber(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot be null or blank");
    }

    @Test
    void shouldThrowExceptionForBlankValue() {
        Assertions.assertThatThrownBy(() -> new SerialNumber("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot be null or blank");
    }
}
