package com.github.jenkaby.bikerental.identity.infrastructure.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.oauth2.server.authorization.oidc.authentication.OidcLogoutAuthenticationToken;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Stateless OIDC logout request converter")
class StatelessOidcLogoutAuthenticationConverterTest {

    private final StatelessOidcLogoutAuthenticationConverter converter = new StatelessOidcLogoutAuthenticationConverter();

    @Test
    @DisplayName("Should build an anonymous, session-less logout token so sub/sid checks are skipped")
    void shouldBuildAnonymousSessionLessToken() {
        var request = new MockHttpServletRequest();
        request.addParameter("id_token_hint", "the-id-token");
        request.addParameter("post_logout_redirect_uri", "http://localhost:4200/admin/");
        request.addParameter("state", "xyz");

        var actual = (OidcLogoutAuthenticationToken) converter.convert(request);

        assertThat(actual.isPrincipalAuthenticated())
                .as("an unauthenticated principal makes the provider skip the sub/sid validation")
                .isFalse();
        assertThat(actual.getSessionId()).as("no session binding is carried").isNull();
        assertThat(actual.getIdTokenHint()).isEqualTo("the-id-token");
        assertThat(actual.getPostLogoutRedirectUri()).isEqualTo("http://localhost:4200/admin/");
        assertThat(actual.getState()).isEqualTo("xyz");
    }

    @Test
    @DisplayName("Should not convert a request without an id_token_hint")
    void shouldNotConvertWithoutIdTokenHint() {
        var request = new MockHttpServletRequest();
        request.addParameter("post_logout_redirect_uri", "http://localhost:4200/admin/");

        var actual = converter.convert(request);

        assertThat(actual).as("nothing to authenticate without an id_token_hint").isNull();
    }
}
