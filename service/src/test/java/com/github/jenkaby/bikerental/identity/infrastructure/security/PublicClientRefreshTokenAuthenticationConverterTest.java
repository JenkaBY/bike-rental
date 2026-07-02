package com.github.jenkaby.bikerental.identity.infrastructure.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Public client refresh token request converter")
class PublicClientRefreshTokenAuthenticationConverterTest {

    private final PublicClientRefreshTokenAuthenticationConverter converter = new PublicClientRefreshTokenAuthenticationConverter();

    @Test
    @DisplayName("Should build a NONE-method token for a public client refresh_token request")
    void shouldConvertPublicClientRefreshTokenRequest() {
        var request = new MockHttpServletRequest();
        request.addParameter("grant_type", "refresh_token");
        request.addParameter("client_id", "bike-rental-admin");
        request.addParameter("refresh_token", "the-refresh-token");

        var actual = (OAuth2ClientAuthenticationToken) converter.convert(request);

        assertThat(actual.getPrincipal()).isEqualTo("bike-rental-admin");
        assertThat(actual.getClientAuthenticationMethod()).isEqualTo(ClientAuthenticationMethod.NONE);
        assertThat(actual.getAdditionalParameters())
                .as("client_id is stripped, everything else is forwarded for the provider to authenticate")
                .containsEntry("grant_type", "refresh_token")
                .containsEntry("refresh_token", "the-refresh-token")
                .doesNotContainKey("client_id");
    }

    @Test
    @DisplayName("Should not convert an authorization_code request")
    void shouldNotConvertAuthorizationCodeRequest() {
        var request = new MockHttpServletRequest();
        request.addParameter("grant_type", "authorization_code");
        request.addParameter("client_id", "bike-rental-admin");
        request.addParameter("code", "the-code");
        request.addParameter("code_verifier", "the-verifier");

        var actual = converter.convert(request);

        assertThat(actual).as("PKCE authorization_code requests stay with the default converter").isNull();
    }

    @Test
    @DisplayName("Should not convert a refresh_token request carrying a client_secret")
    void shouldNotConvertWhenClientSecretPresent() {
        var request = new MockHttpServletRequest();
        request.addParameter("grant_type", "refresh_token");
        request.addParameter("client_id", "confidential-client");
        request.addParameter("client_secret", "the-secret");
        request.addParameter("refresh_token", "the-refresh-token");

        var actual = converter.convert(request);

        assertThat(actual).as("confidential clients must be authenticated by their own converter").isNull();
    }

    @Test
    @DisplayName("Should not convert a refresh_token request carrying an Authorization header")
    void shouldNotConvertWhenAuthorizationHeaderPresent() {
        var request = new MockHttpServletRequest();
        request.addParameter("grant_type", "refresh_token");
        request.addParameter("client_id", "confidential-client");
        request.addParameter("refresh_token", "the-refresh-token");
        request.addHeader("Authorization", "Basic dGhlLWlkOnRoZS1zZWNyZXQ=");

        var actual = converter.convert(request);

        assertThat(actual).as("HTTP Basic client authentication takes precedence").isNull();
    }

    @Test
    @DisplayName("Should reject a refresh_token request without a client_id")
    void shouldRejectRequestWithoutClientId() {
        var request = new MockHttpServletRequest();
        request.addParameter("grant_type", "refresh_token");
        request.addParameter("refresh_token", "the-refresh-token");

        assertThatThrownBy(() -> converter.convert(request))
                .isInstanceOf(OAuth2AuthenticationException.class);
    }
}
