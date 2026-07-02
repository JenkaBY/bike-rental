package com.github.jenkaby.bikerental.identity.infrastructure.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("OAuth2 error authentication entry point")
class OAuth2ErrorAuthenticationEntryPointTest {

    private final OAuth2ErrorAuthenticationEntryPoint entryPoint = new OAuth2ErrorAuthenticationEntryPoint();

    @Test
    @DisplayName("Should write the OAuth2 error as JSON instead of redirecting")
    void shouldWriteOAuth2ErrorAsJson() throws Exception {
        var request = new MockHttpServletRequest();
        var response = new MockHttpServletResponse();
        var exception = new OAuth2AuthenticationException(
                new OAuth2Error(OAuth2ErrorCodes.INVALID_CLIENT, "Client authentication failed: client_id", null));

        entryPoint.commence(request, response, exception);

        assertThat(response.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
        assertThat(response.getRedirectedUrl()).as("no HTML redirect for a machine client").isNull();
        assertThat(response.getContentType()).startsWith("application/json");
        assertThat(response.getContentAsString()).contains("\"error\":\"invalid_client\"");
    }

    @Test
    @DisplayName("Should fall back to a generic invalid_request error for non-OAuth2 authentication exceptions")
    void shouldFallBackToGenericErrorForOtherExceptions() throws Exception {
        var request = new MockHttpServletRequest();
        var response = new MockHttpServletResponse();
        var exception = new InsufficientAuthenticationException("Full authentication is required");

        entryPoint.commence(request, response, exception);

        assertThat(response.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
        assertThat(response.getContentAsString()).contains("\"error\":\"invalid_request\"");
    }
}
