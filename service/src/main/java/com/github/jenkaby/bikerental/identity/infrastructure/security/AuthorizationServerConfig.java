package com.github.jenkaby.bikerental.identity.infrastructure.security;

import com.github.jenkaby.bikerental.identity.application.config.AuthorityProperties;
import com.github.jenkaby.bikerental.identity.application.config.JwtProperties;
import com.github.jenkaby.bikerental.identity.domain.repository.UserRepository;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.authorization.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.converter.RsaKeyConverters;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.*;
import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

@Configuration
public class AuthorizationServerConfig {

    @Bean
    @Order(1)
    SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
        var authorizationServerConfigurer = new OAuth2AuthorizationServerConfigurer();
        http.securityMatcher(authorizationServerConfigurer.getEndpointsMatcher())
                .with(authorizationServerConfigurer, server -> server.oidc(Customizer.withDefaults()))
                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                .csrf(csrf -> csrf.ignoringRequestMatchers(authorizationServerConfigurer.getEndpointsMatcher()))
                .exceptionHandling(exceptions -> exceptions.defaultAuthenticationEntryPointFor(
                        new LoginUrlAuthenticationEntryPoint("/login"),
                        new MediaTypeRequestMatcher(MediaType.TEXT_HTML)))
                .oauth2ResourceServer(resourceServer -> resourceServer.jwt(Customizer.withDefaults()));
        return http.build();
    }

    @Bean
    RegisteredClientRepository registeredClientRepository(JdbcOperations jdbcOperations) {
        return new JdbcRegisteredClientRepository(jdbcOperations);
    }

    @Bean
    OAuth2AuthorizationService authorizationService(JdbcOperations jdbcOperations,
                                                    RegisteredClientRepository registeredClientRepository) {
        return new JdbcOAuth2AuthorizationService(jdbcOperations, registeredClientRepository);
    }

    @Bean
    OAuth2AuthorizationConsentService authorizationConsentService(JdbcOperations jdbcOperations,
                                                                  RegisteredClientRepository registeredClientRepository) {
        return new JdbcOAuth2AuthorizationConsentService(jdbcOperations, registeredClientRepository);
    }

    @Bean
    OAuth2TokenCustomizer<JwtEncodingContext> tokenCustomizer(UserRepository userRepository,
                                                              AuthorityProperties authorityProperties,
                                                              JwtProperties jwtProperties) {
        return context -> {
            if (!OAuth2TokenType.ACCESS_TOKEN.equals(context.getTokenType())) {
                return;
            }
            var rolePrefix = authorityProperties.rolePrefix();
            var roles = context.getPrincipal().getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .filter(authority -> authority.startsWith(rolePrefix))
                    .map(authority -> authority.substring(rolePrefix.length()))
                    .toList();
            context.getClaims().claim("roles", roles);
            if (context.getPrincipal().getPrincipal() instanceof IdentityUserPrincipal principal) {
                context.getClaims().claim("must_change_password", principal.isMustChangePassword());
            }
            var principalName = context.getPrincipal().getName();
            userRepository.findByUsername(principalName)
                    .or(() -> userRepository.findByEmail(principalName))
                    .ifPresent(user -> context.getClaims().claim(jwtProperties.userIdClaim(), user.getId().toString()));
        };
    }

    @Bean
    AuthorizationServerSettings authorizationServerSettings(JwtProperties jwtProperties) {
        return AuthorizationServerSettings.builder()
                .issuer(jwtProperties.issuer())
                .build();
    }

    @Bean
    JWKSource<SecurityContext> jwkSource(JwtProperties jwtProperties, ResourceLoader resourceLoader) {
        RSAKey rsaKey = buildRsaKey(jwtProperties, resourceLoader);
        return new ImmutableJWKSet<>(new JWKSet(rsaKey));
    }

    @Bean
    JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
        return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
    }

    private RSAKey buildRsaKey(JwtProperties jwtProperties, ResourceLoader resourceLoader) {
        if (jwtProperties.privateKeyLocation() != null && jwtProperties.publicKeyLocation() != null) {
            return loadRsaKey(jwtProperties, resourceLoader);
        }
        return generateRsaKey(jwtProperties.keyId());
    }

    private RSAKey loadRsaKey(JwtProperties jwtProperties, ResourceLoader resourceLoader) {
        try (InputStream privateKeyStream = resourceLoader.getResource(jwtProperties.privateKeyLocation()).getInputStream();
             InputStream publicKeyStream = resourceLoader.getResource(jwtProperties.publicKeyLocation()).getInputStream()) {
            RSAPrivateKey privateKey = RsaKeyConverters.pkcs8().convert(privateKeyStream);
            RSAPublicKey publicKey = RsaKeyConverters.x509().convert(publicKeyStream);
            return new RSAKey.Builder(publicKey)
                    .privateKey(privateKey)
                    .keyID(jwtProperties.keyId())
                    .build();
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to load RSA key material for JWT signing", ex);
        }
    }

    private RSAKey generateRsaKey(String keyId) {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            return new RSAKey.Builder((RSAPublicKey) keyPair.getPublic())
                    .privateKey((RSAPrivateKey) keyPair.getPrivate())
                    .keyID(keyId)
                    .build();
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to generate RSA key material for JWT signing", ex);
        }
    }
}
