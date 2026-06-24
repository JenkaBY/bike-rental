package com.github.jenkaby.bikerental.identity.infrastructure.security;

import com.github.jenkaby.bikerental.identity.application.config.AuthorityProperties;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class WebSecurityConfig {

    @Bean
    @Order(2)
    SecurityFilterChain apiSecurityFilterChain(HttpSecurity http,
                                               JwtAuthenticationConverter jwtAuthenticationConverter,
                                               ProblemDetailAuthenticationEntryPoint authenticationEntryPoint,
                                               ProblemDetailAccessDeniedHandler accessDeniedHandler) throws Exception {
        http.securityMatcher("/api/**")
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/users/**").hasRole("ADMIN")
                        .requestMatchers("/api/auth/me", "/api/auth/password").authenticated()
                        .anyRequest().permitAll())
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .oauth2ResourceServer(resourceServer -> resourceServer
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter))
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler));
        return http.build();
    }

    @Bean
    @Order(3)
    SecurityFilterChain loginSecurityFilterChain(HttpSecurity http,
                                                 GoogleOidcUserService googleOidcUserService,
                                                 RequestCache requestCache,
                                                 ObjectProvider<ClientRegistrationRepository> clientRegistrationRepository) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login", "/error", "/oauth2/**", "/login/oauth2/**",
                                "/actuator/**", "/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
                        .anyRequest().authenticated())
                .requestCache(cache -> cache.requestCache(requestCache))
                .formLogin(Customizer.withDefaults());
        if (clientRegistrationRepository.getIfAvailable() != null) {
            http.oauth2Login(oauth2 -> oauth2
                    .userInfoEndpoint(userInfo -> userInfo.oidcUserService(googleOidcUserService)));
        }
        return http.build();
    }

    @Bean
    RequestCache oauth2AuthorizeRequestCache() {
        var requestCache = new HttpSessionRequestCache();
        requestCache.setRequestMatcher(
                PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.GET, "/oauth2/authorize"));
        return requestCache;
    }

    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter(AuthorityProperties authorityProperties) {
        var authoritiesConverter = new JwtGrantedAuthoritiesConverter();
        authoritiesConverter.setAuthoritiesClaimName("roles");
        authoritiesConverter.setAuthorityPrefix(authorityProperties.rolePrefix());
        var converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);
        return converter;
    }
}
