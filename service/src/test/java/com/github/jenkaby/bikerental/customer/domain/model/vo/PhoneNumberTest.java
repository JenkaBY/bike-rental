package com.github.jenkaby.bikerental.customer.domain.model.vo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PhoneNumberTest {

    @ParameterizedTest
    @CsvSource({
            "'+1 (555) 123-4567', '+15551234567'",
            "'+7 (999) 888-77-66', '+79998887766'",
            "'8 800 555 35 35', '88005553535'",
            "'+1.555.123.4567', '+15551234567'",
            "'+7(999)888-77-66 ext.123', '+79998887766123'",
            "'79998887766', '79998887766'",
            "'+79998887766', '+79998887766'"
    })
    void shouldNormalizePhoneNumberCorrectly(String input, String expectedNormalized) {
        PhoneNumber phoneNumber = new PhoneNumber(input);

        assertThat(phoneNumber.value()).isEqualTo(expectedNormalized);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "\t", "\n", "()- "})
    void shouldThrowExceptionWhenPhoneIsInvalid(String phone) {
        assertThatThrownBy(() -> new PhoneNumber(phone))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Phone number cannot be empty");
    }

    @Test
    void shouldThrowExceptionWhenPhoneIsNull() {
        assertThatThrownBy(() -> new PhoneNumber(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Phone number cannot be empty");
    }


    @Test
    void shouldBeEqualWhenPhoneValuesAreTheSameAfterNormalization() {
        PhoneNumber phone1 = new PhoneNumber("+7 (999) 888-77-66");
        PhoneNumber phone2 = new PhoneNumber("+79998887766");

        assertThat(phone1).isEqualTo(phone2);
        assertThat(phone1.hashCode()).isEqualTo(phone2.hashCode());
    }

    @Test
    void shouldNotBeEqualWhenPhoneValuesAreDifferent() {
        PhoneNumber phone1 = new PhoneNumber("+79998887766");
        PhoneNumber phone2 = new PhoneNumber("+79998887765");

        assertThat(phone1).isNotEqualTo(phone2);
    }
}
