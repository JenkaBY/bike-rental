package com.github.jenkaby.bikerental.equipment.domain.model.vo;

import com.github.jenkaby.bikerental.equipment.shared.domain.model.vo.Uid;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class UidTest {

    @Test
    void shouldCreateUidWithValidValue() {
        var uid = new Uid("ABC123");
        Assertions.assertThat(uid.value()).isEqualTo("ABC123");
    }

    @Test
    void shouldAllowNullUid() {
        var uid = new Uid(null);
        Assertions.assertThat(uid.value()).isNull();
    }

    @Test
    void shouldThrowExceptionForBlankValue() {
        Assertions.assertThatThrownBy(() -> new Uid("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("UID cannot be blank");
    }
}
