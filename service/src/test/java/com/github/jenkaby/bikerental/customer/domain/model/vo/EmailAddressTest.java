package com.github.jenkaby.bikerental.customer.domain.model.vo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EmailAddressTest {

    @Test
    void shouldCreateEmailAddressWithValidInput() {
        String email = "user@example.com";

        EmailAddress emailAddress = new EmailAddress(email);

        assertThat(emailAddress.value()).isEqualTo(email);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "user@example.com",
            "user.name@example.com",
            "user+tag@example.com",
            "user_name@example.com",
            "user-name@example.com",
            "user123@example.com",
            "user@subdomain.example.com",
            "user@example.co.uk"
    })
    void shouldAcceptValidEmailFormats(String email) {
        EmailAddress emailAddress = new EmailAddress(email);

        assertThat(emailAddress.value()).isEqualTo(email);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "invalid.email",
            "@example.com",
            "user@",
            "user@@example.com",
            "user@.com",
            "user@domain",
            "user @example.com",
            "user@exam ple.com"
    })
    void shouldThrowExceptionForInvalidEmailFormat(String email) {
        assertThatThrownBy(() -> new EmailAddress(email))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid email format");
    }

    @Test
    void shouldThrowExceptionWhenEmailIsTooLong() {
        String longEmail = "a".repeat(246) + "@test.com";

        assertThatThrownBy(() -> new EmailAddress(longEmail))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Email address is too long (max 254 characters)");
    }

    @Test
    void shouldAcceptEmailWithMaxLength() {
        String maxLengthEmail = "a".repeat(243) + "@test.com";

        EmailAddress emailAddress = new EmailAddress(maxLengthEmail);

        assertThat(emailAddress.value()).isEqualTo(maxLengthEmail);
    }

    @Test
    void shouldAllowNullEmail() {
        EmailAddress emailAddress = new EmailAddress(null);

        assertThat(emailAddress.value()).isNull();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "\t", "\n"})
    void shouldAllowBlankEmail(String email) {
        EmailAddress emailAddress = new EmailAddress(email);

        assertThat(emailAddress.value()).isEqualTo(email);
    }

    @Test
    void shouldBeEqualWhenEmailValuesAreTheSame() {
        EmailAddress email1 = new EmailAddress("user@example.com");
        EmailAddress email2 = new EmailAddress("user@example.com");

        assertThat(email1).isEqualTo(email2);
        assertThat(email1.hashCode()).isEqualTo(email2.hashCode());
    }

    @Test
    void shouldNotBeEqualWhenEmailValuesAreDifferent() {
        EmailAddress email1 = new EmailAddress("user1@example.com");
        EmailAddress email2 = new EmailAddress("user2@example.com");

        assertThat(email1).isNotEqualTo(email2);
    }
}
