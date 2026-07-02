package com.github.jenkaby.bikerental.identity.infrastructure.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Public client refresh token authentication provider")
class PublicClientRefreshTokenAuthenticationProviderTest {

    private static final String PUBLIC_CLIENT_ID = "bike-rental-admin";
    private static final String CONFIDENTIAL_CLIENT_ID = "confidential-client";

    private final InMemoryRegisteredClientRepository registeredClientRepository = new InMemoryRegisteredClientRepository(
            publicClient(), confidentialClient());
    private final PublicClientRefreshTokenAuthenticationProvider provider =
            new PublicClientRefreshTokenAuthenticationProvider(registeredClientRepository);

    @Test
    @DisplayName("Should authenticate a registered public client without a code_verifier")
    void shouldAuthenticatePublicClient() {
        var authentication = refreshTokenAuthentication(PUBLIC_CLIENT_ID);

        var actual = (OAuth2ClientAuthenticationToken) provider.authenticate(authentication);

        assertThat(actual.isAuthenticated()).isTrue();
        assertThat(actual.getRegisteredClient().getClientId()).isEqualTo(PUBLIC_CLIENT_ID);
        assertThat(actual.getClientAuthenticationMethod()).isEqualTo(ClientAuthenticationMethod.NONE);
    }

    @Test
    @DisplayName("Should abstain when the client authentication method is not NONE")
    void shouldAbstainForNonPublicMethod() {
        var authentication = new OAuth2ClientAuthenticationToken(PUBLIC_CLIENT_ID,
                ClientAuthenticationMethod.CLIENT_SECRET_BASIC, "secret",
                Map.of("grant_type", "refresh_token"));

        var actual = provider.authenticate(authentication);

        assertThat(actual).as("client_secret_basic is handled by ClientSecretAuthenticationProvider").isNull();
    }

    @Test
    @DisplayName("Should abstain when the grant type is not refresh_token")
    void shouldAbstainForNonRefreshTokenGrant() {
        var authentication = new OAuth2ClientAuthenticationToken(PUBLIC_CLIENT_ID, ClientAuthenticationMethod.NONE,
                null, Map.of("grant_type", "authorization_code", "code_verifier", "verifier"));

        var actual = provider.authenticate(authentication);

        assertThat(actual).as("authorization_code+PKCE stays with PublicClientAuthenticationProvider").isNull();
    }

    @Test
    @DisplayName("Should reject an unknown client_id")
    void shouldRejectUnknownClient() {
        var authentication = refreshTokenAuthentication("unknown-client");

        assertThatThrownBy(() -> provider.authenticate(authentication))
                .isInstanceOf(OAuth2AuthenticationException.class);
    }

    @Test
    @DisplayName("Should reject a client not registered for the NONE authentication method")
    void shouldRejectConfidentialClient() {
        var authentication = refreshTokenAuthentication(CONFIDENTIAL_CLIENT_ID);

        assertThatThrownBy(() -> provider.authenticate(authentication))
                .isInstanceOf(OAuth2AuthenticationException.class);
    }

    private static OAuth2ClientAuthenticationToken refreshTokenAuthentication(String clientId) {
        return new OAuth2ClientAuthenticationToken(clientId, ClientAuthenticationMethod.NONE, null,
                Map.of("grant_type", "refresh_token", "refresh_token", "the-refresh-token"));
    }

    private static RegisteredClient publicClient() {
        return RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId(PUBLIC_CLIENT_ID)
                .clientAuthenticationMethod(ClientAuthenticationMethod.NONE)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .redirectUri("http://localhost:4201/admin/")
                .clientSettings(ClientSettings.builder().requireProofKey(true).build())
                .build();
    }

    private static RegisteredClient confidentialClient() {
        return RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId(CONFIDENTIAL_CLIENT_ID)
                .clientSecret("secret")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .redirectUri("http://localhost:4202/admin/")
                .build();
    }
}
