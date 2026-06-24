package com.github.jenkaby.bikerental.identity.infrastructure.security;

import com.github.jenkaby.bikerental.identity.application.config.AuthorityProperties;
import com.github.jenkaby.bikerental.users.JwtProperties;
import com.github.jenkaby.bikerental.users.UserAuthFacade;
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
import org.springframework.security.jackson.SecurityJacksonModules;
import org.springframework.security.oauth2.server.authorization.jackson.OAuth2AuthorizationServerJacksonModule;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.authorization.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.converter.RsaKeyConverters;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.keygen.Base64StringKeyGenerator;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.authorization.*;
import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.token.*;

import java.time.Instant;
import java.util.Base64;
import java.util.List;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.savedrequest.RequestCache;
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
    SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http,
                                                               RequestCache requestCache) throws Exception {
        var authorizationServerConfigurer = new OAuth2AuthorizationServerConfigurer();
        http.securityMatcher(authorizationServerConfigurer.getEndpointsMatcher())
                .with(authorizationServerConfigurer, server -> server.oidc(Customizer.withDefaults()))
                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                .csrf(csrf -> csrf.ignoringRequestMatchers(authorizationServerConfigurer.getEndpointsMatcher()))
                .cors(Customizer.withDefaults())
                .requestCache(cache -> cache.requestCache(requestCache))
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
        var validatorBuilder = BasicPolymorphicTypeValidator.builder()
                .allowIfSubType(Object.class);
        var classLoader = JdbcOAuth2AuthorizationService.class.getClassLoader();
        var jsonMapper = JsonMapper.builder()
                .addModules(SecurityJacksonModules.getModules(classLoader, validatorBuilder))
                .addModule(new OAuth2AuthorizationServerJacksonModule())
                .addMixIn(IdentityUserPrincipal.class, IdentityUserPrincipalMixin.class)
                .build();
        var rowMapper = new JdbcOAuth2AuthorizationService.JsonMapperOAuth2AuthorizationRowMapper(
                registeredClientRepository, jsonMapper);
        var service = new JdbcOAuth2AuthorizationService(jdbcOperations, registeredClientRepository);
        service.setAuthorizationRowMapper(rowMapper);
        return service;
    }

    @Bean
    OAuth2AuthorizationConsentService authorizationConsentService(JdbcOperations jdbcOperations,
                                                                  RegisteredClientRepository registeredClientRepository) {
        return new JdbcOAuth2AuthorizationConsentService(jdbcOperations, registeredClientRepository);
    }

    @Bean
    OAuth2TokenCustomizer<JwtEncodingContext> tokenCustomizer(UserAuthFacade userAuthFacade,
                                                              AuthorityProperties authorityProperties,
                                                              JwtProperties jwtProperties) {
        return context -> {
            if (!OAuth2TokenType.ACCESS_TOKEN.equals(context.getTokenType())) {
                return;
            }
            var principalName = context.getPrincipal().getName();
            var account = userAuthFacade.findByUsername(principalName)
                    .or(() -> userAuthFacade.findByEmail(principalName));
            account.ifPresentOrElse(
                    user -> {
                        context.getClaims().claim("roles", user.roles().stream().toList());
                        context.getClaims().claim("must_change_password", user.mustChangePassword());
                        context.getClaims().claim(jwtProperties.userIdClaim(), user.id().toString());
                    },
                    () -> context.getClaims().claim("roles", rolesFromAuthorities(context, authorityProperties)));
        };
    }

    private static List<String> rolesFromAuthorities(JwtEncodingContext context, AuthorityProperties authorityProperties) {
        var rolePrefix = authorityProperties.rolePrefix();
        return context.getPrincipal().getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(authority -> authority.startsWith(rolePrefix))
                .map(authority -> authority.substring(rolePrefix.length()))
                .toList();
    }

    @Bean
    OAuth2TokenGenerator<OAuth2Token> tokenGenerator(JWKSource<SecurityContext> jwkSource,
                                                     OAuth2TokenCustomizer<JwtEncodingContext> tokenCustomizer) {
        var jwtGenerator = new JwtGenerator(new NimbusJwtEncoder(jwkSource));
        jwtGenerator.setJwtCustomizer(tokenCustomizer);
        var keyGenerator = new Base64StringKeyGenerator(Base64.getUrlEncoder().withoutPadding(), 96);
        OAuth2TokenGenerator<OAuth2RefreshToken> refreshTokenGenerator = context -> {
            if (!OAuth2TokenType.REFRESH_TOKEN.equals(context.getTokenType())) {
                return null;
            }
            if (!context.getRegisteredClient().getAuthorizationGrantTypes()
                    .contains(AuthorizationGrantType.REFRESH_TOKEN)) {
                return null;
            }
            var issuedAt = Instant.now();
            var refreshTokenTimeToLive = context.getRegisteredClient().getTokenSettings().getRefreshTokenTimeToLive();
            var expiresAt = issuedAt.plus(refreshTokenTimeToLive);
            return new OAuth2RefreshToken(keyGenerator.generateKey(), issuedAt, expiresAt);
        };
        return new DelegatingOAuth2TokenGenerator(jwtGenerator, new OAuth2AccessTokenGenerator(), refreshTokenGenerator);
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
