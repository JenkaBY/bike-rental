package com.github.jenkaby.bikerental.componenttest.steps.identity;

import com.github.jenkaby.bikerental.componenttest.context.ScenarioContext;
import com.github.jenkaby.bikerental.identity.application.config.JwtProperties;
import com.github.jenkaby.bikerental.identity.domain.model.Role;
import com.github.jenkaby.bikerental.identity.domain.repository.UserRepository;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Slf4j
public class IdentityAuthSteps {

    private final ScenarioContext scenarioContext;
    private final UserRepository userRepository;
    private final JwtProperties jwtProperties;
    private final NimbusJwtEncoder jwtEncoder;

    public IdentityAuthSteps(ScenarioContext scenarioContext,
                             UserRepository userRepository,
                             JwtProperties jwtProperties,
                             JWKSource<SecurityContext> jwkSource) {
        this.scenarioContext = scenarioContext;
        this.userRepository = userRepository;
        this.jwtProperties = jwtProperties;
        this.jwtEncoder = new NimbusJwtEncoder(jwkSource);
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
}
