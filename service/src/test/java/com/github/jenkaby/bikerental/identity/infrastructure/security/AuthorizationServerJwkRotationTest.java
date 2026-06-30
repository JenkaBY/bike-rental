package com.github.jenkaby.bikerental.identity.infrastructure.security;

import com.github.jenkaby.bikerental.users.JwtProperties;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKMatcher;
import com.nimbusds.jose.jwk.JWKSelector;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.security.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Authorization Server JWK rotation")
class AuthorizationServerJwkRotationTest {

    private static final String ACTIVE_KEY_ID = "bike-rental-identity-active";
    private static final String PREVIOUS_KEY_ID = "bike-rental-identity-previous";

    private final AuthorizationServerConfig config = new AuthorizationServerConfig();
    private final ResourceLoader resourceLoader = new DefaultResourceLoader();

    @Test
    @DisplayName("Should publish active and previous key ids in the JWK set")
    void shouldPublishActiveAndPreviousKeyIds(@TempDir Path keysDir) throws Exception {
        GeneratedKey active = writeKeyPair(keysDir, "active");
        GeneratedKey previous = writeKeyPair(keysDir, "previous");
        JwtProperties properties = propertiesWith(ACTIVE_KEY_ID, active,
                new JwtProperties.PreviousKey(PREVIOUS_KEY_ID, previous.publicLocation()));

        JWKSet actual = jwkSetOf(config.jwkSource(properties, resourceLoader));

        assertThat(actual.getKeys())
                .as("JWK set exposes both the active and the retired key id")
                .extracting(JWK::getKeyID)
                .containsExactlyInAnyOrder(ACTIVE_KEY_ID, PREVIOUS_KEY_ID);
    }

    @Test
    @DisplayName("Should keep the private part only for the active signing key")
    void shouldKeepPrivatePartOnlyForActiveKey(@TempDir Path keysDir) throws Exception {
        GeneratedKey active = writeKeyPair(keysDir, "active");
        GeneratedKey previous = writeKeyPair(keysDir, "previous");
        JwtProperties properties = propertiesWith(ACTIVE_KEY_ID, active,
                new JwtProperties.PreviousKey(PREVIOUS_KEY_ID, previous.publicLocation()));

        JWKSet actual = jwkSetOf(config.jwkSource(properties, resourceLoader));

        assertThat(actual.getKeyByKeyId(ACTIVE_KEY_ID).isPrivate())
                .as("active key carries the private part for signing")
                .isTrue();
        assertThat(actual.getKeyByKeyId(PREVIOUS_KEY_ID).isPrivate())
                .as("retired key is verification-only")
                .isFalse();
        assertThat(actual.toPublicJWKSet().getKeys())
                .as("the public JWK set served at /oauth2/jwks never leaks private material")
                .noneMatch(JWK::isPrivate)
                .extracting(JWK::getKeyID)
                .containsExactlyInAnyOrder(ACTIVE_KEY_ID, PREVIOUS_KEY_ID);
    }

    @Test
    @DisplayName("Should expose only the generated key when no key locations are configured")
    void shouldExposeGeneratedKeyWhenNoLocationsConfigured() {
        JwtProperties properties = new JwtProperties("http://localhost:8080",
                Duration.ofMinutes(15), Duration.ofDays(14), ACTIVE_KEY_ID, "uid",
                "", "", List.of());

        JWKSet actual = jwkSetOf(config.jwkSource(properties, resourceLoader));

        assertThat(actual.getKeys())
                .as("blank key locations fall back to a single ephemeral signing key")
                .singleElement()
                .satisfies(key -> {
                    assertThat(key.getKeyID()).isEqualTo(ACTIVE_KEY_ID);
                    assertThat(key.isPrivate()).isTrue();
                });
    }

    @Test
    @DisplayName("Should sign with the active key id and verify across the whole key set")
    void shouldSignWithActiveKeyAndVerify(@TempDir Path keysDir) throws Exception {
        GeneratedKey active = writeKeyPair(keysDir, "active");
        GeneratedKey previous = writeKeyPair(keysDir, "previous");
        JWKSource<SecurityContext> jwkSource = config.jwkSource(propertiesWith(ACTIVE_KEY_ID, active,
                new JwtProperties.PreviousKey(PREVIOUS_KEY_ID, previous.publicLocation())), resourceLoader);
        var encoder = new NimbusJwtEncoder(jwkSource);
        var header = JwsHeader.with(SignatureAlgorithm.RS256).keyId(ACTIVE_KEY_ID).build();

        var token = encoder.encode(JwtEncoderParameters.from(header, claims())).getTokenValue();

        var actual = OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource).decode(token);
        assertThat(actual.getSubject()).isEqualTo("admin");
        assertThat(actual.getHeaders()).containsEntry("kid", ACTIVE_KEY_ID);
    }

    @Test
    @DisplayName("Should fail to sign when the key id is not stamped and several keys are present")
    void shouldFailToSignWhenKeyIdAmbiguous(@TempDir Path keysDir) throws Exception {
        GeneratedKey active = writeKeyPair(keysDir, "active");
        GeneratedKey previous = writeKeyPair(keysDir, "previous");
        JWKSource<SecurityContext> jwkSource = config.jwkSource(propertiesWith(ACTIVE_KEY_ID, active,
                new JwtProperties.PreviousKey(PREVIOUS_KEY_ID, previous.publicLocation())), resourceLoader);
        var encoder = new NimbusJwtEncoder(jwkSource);
        var headerWithoutKeyId = JwsHeader.with(SignatureAlgorithm.RS256).build();

        assertThatThrownBy(() -> encoder.encode(JwtEncoderParameters.from(headerWithoutKeyId, claims())))
                .as("without a stamped kid the encoder cannot choose between multiple keys")
                .isInstanceOf(JwtException.class);
    }

    @Test
    @DisplayName("Should still verify tokens signed by the retired key after rotation")
    void shouldVerifyTokensSignedByRetiredKey(@TempDir Path keysDir) throws Exception {
        GeneratedKey active = writeKeyPair(keysDir, "active");
        GeneratedKey previous = writeKeyPair(keysDir, "previous");
        JWKSource<SecurityContext> rotatedJwkSource = config.jwkSource(propertiesWith(ACTIVE_KEY_ID, active,
                new JwtProperties.PreviousKey(PREVIOUS_KEY_ID, previous.publicLocation())), resourceLoader);
        var tokenFromRetiredKey = signWith(previous, PREVIOUS_KEY_ID);

        var actual = OAuth2AuthorizationServerConfiguration.jwtDecoder(rotatedJwkSource).decode(tokenFromRetiredKey);

        assertThat(actual.getSubject())
                .as("a token minted before rotation still verifies against the rotated JWK set")
                .isEqualTo("admin");
    }

    private JwtProperties propertiesWith(String activeKeyId, GeneratedKey active, JwtProperties.PreviousKey previousKey) {
        return new JwtProperties("http://localhost:8080", Duration.ofMinutes(15), Duration.ofDays(14),
                activeKeyId, "uid", active.privateLocation(), active.publicLocation(), List.of(previousKey));
    }

    private String signWith(GeneratedKey key, String keyId) {
        var signingKey = new RSAKey.Builder((RSAPublicKey) key.keyPair().getPublic())
                .privateKey((RSAPrivateKey) key.keyPair().getPrivate())
                .keyID(keyId)
                .build();
        var encoder = new NimbusJwtEncoder(new ImmutableJWKSet<>(new JWKSet(signingKey)));
        var header = JwsHeader.with(SignatureAlgorithm.RS256).keyId(keyId).build();
        return encoder.encode(JwtEncoderParameters.from(header, claims())).getTokenValue();
    }

    private static JwtClaimsSet claims() {
        var now = Instant.now();
        return JwtClaimsSet.builder()
                .issuer("http://localhost:8080")
                .subject("admin")
                .issuedAt(now)
                .expiresAt(now.plus(15, ChronoUnit.MINUTES))
                .build();
    }

    private static JWKSet jwkSetOf(JWKSource<SecurityContext> jwkSource) {
        try {
            List<JWK> keys = jwkSource.get(new JWKSelector(new JWKMatcher.Builder().build()), null);
            return new JWKSet(keys);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to read JWK set from source", ex);
        }
    }

    private static GeneratedKey writeKeyPair(Path dir, String name) throws Exception {
        var generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        var keyPair = generator.generateKeyPair();
        var privateFile = dir.resolve(name + "-private.pem");
        var publicFile = dir.resolve(name + "-public.pem");
        Files.writeString(privateFile, toPem("PRIVATE KEY", keyPair.getPrivate().getEncoded()));
        Files.writeString(publicFile, toPem("PUBLIC KEY", keyPair.getPublic().getEncoded()));
        return new GeneratedKey(keyPair, privateFile.toUri().toString(), publicFile.toUri().toString());
    }

    private static String toPem(String type, byte[] der) {
        var base64 = Base64.getMimeEncoder(64, "\n".getBytes(StandardCharsets.US_ASCII)).encodeToString(der);
        return "-----BEGIN " + type + "-----\n" + base64 + "\n-----END " + type + "-----\n";
    }

    private record GeneratedKey(KeyPair keyPair, String privateLocation, String publicLocation) {
    }
}
