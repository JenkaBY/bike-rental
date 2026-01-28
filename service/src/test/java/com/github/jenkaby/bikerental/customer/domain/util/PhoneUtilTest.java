package com.github.jenkaby.bikerental.customer.domain.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class PhoneUtilTest {

    @ParameterizedTest
    @CsvSource({
            "'+1 (555) 123-4567', '+15551234567'",
            "'+7 (999) 888-77-66', '+79998887766'",
            "'8 800 555 35 35', '88005553535'",
            "'+1.555.123.4567', '+15551234567'",
            "'79998887766', '79998887766'",
            "'+79998887766', '+79998887766'",
            "'7 999 888 77 66', '79998887766'",
            "'7-999-888-77-66', '79998887766'",
            "'+7(999)888-77-66', '+79998887766'",
            "'7.999.888.77.66', '79998887766'",
            "'+7 999 888 77 66', '+79998887766'",
            "'+7 (999) 888-77-66 ext. 123', '+79998887766123'",
            "'+7   999   888   77   66', '+79998887766'"
    })
    void shouldNormalizePhoneByRemovingSpecialCharacters(String input, String expected) {
        String result = PhoneUtil.normalize(input);

        assertThat(result).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({
            "'', ''",
            "'()- .', ''"
    })
    void shouldReturnEmptyStringForEmptyOrOnlySpecialCharacters(String input, String expected) {
        String result = PhoneUtil.normalize(input);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void shouldReturnNullWhenInputIsNull() {
        String result = PhoneUtil.normalize(null);

        assertThat(result).isNull();
    }
}
