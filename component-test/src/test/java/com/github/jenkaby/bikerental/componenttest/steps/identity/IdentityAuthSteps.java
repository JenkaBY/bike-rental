package com.github.jenkaby.bikerental.componenttest.steps.identity;

import com.github.jenkaby.bikerental.componenttest.context.ScenarioContext;
import com.github.jenkaby.bikerental.users.JwtProperties;
import com.github.jenkaby.bikerental.users.domain.model.Role;
import com.github.jenkaby.bikerental.users.domain.repository.UserRepository;
import com.jayway.jsonpath.JsonPath;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.util.LinkedMultiValueMap;

import java.security.Principal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class IdentityAuthSteps {

    private final ScenarioContext scenarioContext;
    private final UserRepository userRepository;
    private final JwtProperties jwtProperties;
    private final NimbusJwtEncoder jwtEncoder;
    private final OAuth2AuthorizationService authorizationService;
    private final RegisteredClientRepository registeredClientRepository;
    private final TestRestTemplate restClient;
    private final int port;

    public IdentityAuthSteps(ScenarioContext scenarioContext,
                             UserRepository userRepository,
                             JwtProperties jwtProperties,
                             JWKSource<SecurityContext> jwkSource,
                             OAuth2AuthorizationService authorizationService,
                             RegisteredClientRepository registeredClientRepository,
                             TestRestTemplate restClient,
                             @LocalServerPort int port) {
        this.scenarioContext = scenarioContext;
        this.userRepository = userRepository;
        this.jwtProperties = jwtProperties;
        this.jwtEncoder = new NimbusJwtEncoder(jwkSource);
        this.authorizationService = authorizationService;
        this.registeredClientRepository = registeredClientRepository;
        this.restClient = restClient;
        this.port = port;
    }

    @Before("@Admin")
    public void authenticateAsAdmin() {
        authenticate("admin", UUID.randomUUID(), List.of(Role.ADMIN.name()));
    }

    @Before("@Operator")
    public void authenticateAsOperator() {
        authenticate("operator", UUID.randomUUID(), List.of(Role.OPERATOR.name()));
    }

    @Given("the user {string} is authenticated")
    public void theUserIsAuthenticated(String username) {
        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("No seeded user with username " + username));
        var roles = user.getRoles().stream().map(Role::name).toList();
        authenticate(username, user.getId(), roles);
    }

    @Given("no authentication is provided")
    public void noAuthenticationIsProvided() {
        scenarioContext.removeHeader(HttpHeaders.AUTHORIZATION);
        scenarioContext.replaceHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
    }

    private void authenticate(String subject, UUID userId, List<String> roles) {
        var token = mintAccessToken(subject, userId, roles);
        scenarioContext.replaceHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        scenarioContext.replaceHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        scenarioContext.replaceHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
    }

    private String mintAccessToken(String subject, UUID userId, List<String> roles) {
        var now = Instant.now();
        var claims = JwtClaimsSet.builder()
                .issuer(jwtProperties.issuer())
                .subject(subject)
                .issuedAt(now)
                .expiresAt(now.plus(1, ChronoUnit.HOURS))
                .claim("roles", roles)
                .claim(jwtProperties.userIdClaim(), userId.toString())
                .build();
        var header = JwsHeader.with(SignatureAlgorithm.RS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    @Given("a refresh token has been issued for the SPA client {string}")
    public void aRefreshTokenHasBeenIssuedForTheSpaClient(String clientId) {
        var registeredClient = findOrRegisterPublicSpaClient(clientId);
        var issuedAt = Instant.now();
        var accessToken = new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, UUID.randomUUID().toString(),
                issuedAt, issuedAt.plus(1, ChronoUnit.HOURS));
        var refreshTokenValue = UUID.randomUUID().toString();
        var refreshToken = new OAuth2RefreshToken(refreshTokenValue, issuedAt, issuedAt.plus(30, ChronoUnit.DAYS));
        var principal = UsernamePasswordAuthenticationToken.authenticated("operator", null, List.of());
        var authorization = OAuth2Authorization.withRegisteredClient(registeredClient)
                .principalName("operator")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizedScopes(registeredClient.getScopes())
                .attribute(Principal.class.getName(), principal)
                .token(accessToken)
                .token(refreshToken)
                .build();
        authorizationService.save(authorization);
        scenarioContext.setOauth2ClientId(clientId);
        scenarioContext.setOauth2RefreshToken(refreshTokenValue);
    }

    private RegisteredClient findOrRegisterPublicSpaClient(String clientId) {
        var existing = registeredClientRepository.findByClientId(clientId);
        var builder = existing != null ? RegisteredClient.from(existing) : RegisteredClient.withId(UUID.randomUUID().toString());
        var registeredClient = builder
                .clientId(clientId)
                .clientAuthenticationMethod(ClientAuthenticationMethod.NONE)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .redirectUri("http://localhost:4200/login/callback")
                .scopes(scopes -> {
                    scopes.clear();
                    scopes.add("profile");
                })
                .clientSettings(ClientSettings.builder()
                        .requireProofKey(true)
                        .requireAuthorizationConsent(false)
                        .build())
                .tokenSettings(TokenSettings.builder()
                        .reuseRefreshTokens(true)
                        .build())
                .build();
        registeredClientRepository.save(registeredClient);
        return registeredClient;
    }

    @When("the refresh token is exchanged at the {string} endpoint")
    public void theRefreshTokenIsExchangedAtTheEndpoint(String endpoint) {
        var body = new LinkedMultiValueMap<String, String>();
        body.add(OAuth2ParameterNames.GRANT_TYPE, AuthorizationGrantType.REFRESH_TOKEN.getValue());
        body.add(OAuth2ParameterNames.CLIENT_ID, scenarioContext.getOauth2ClientId());
        body.add(OAuth2ParameterNames.REFRESH_TOKEN, scenarioContext.getOauth2RefreshToken());
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        var uri = "http://localhost:" + port + endpoint;
        var response = restClient.postForEntity(uri, new HttpEntity<>(body, headers), String.class);
        log.info("Response : {}", response);
        scenarioContext.setResponse(response);
    }

    @Then("the response contains a new access token")
    public void theResponseContainsANewAccessToken() {
        var documentContext = JsonPath.parse(scenarioContext.getStringResponseBody());
        String accessToken = documentContext.read("$.access_token");
        assertThat(accessToken).isNotBlank();
    }
}
