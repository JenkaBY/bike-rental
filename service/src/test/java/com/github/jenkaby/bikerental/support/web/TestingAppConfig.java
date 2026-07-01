package com.github.jenkaby.bikerental.support.web;

import com.github.jenkaby.bikerental.shared.config.CorsProperties;
import com.github.jenkaby.bikerental.shared.infrastructure.port.uuid.UuidCreatorAdapter;
import com.github.jenkaby.bikerental.shared.web.advice.BaseValidationErrorMapper;
import com.github.jenkaby.bikerental.shared.web.advice.CorrelationIdProvider;
import com.github.jenkaby.bikerental.tariff.web.command.validation.TariffV2PricingValidator;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.MockMvcBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.ConfigurableMockMvcBuilder;
import org.springframework.web.client.ApiVersionInserter;

@TestConfiguration
@EnableConfigurationProperties(CorsProperties.class)
@Import({UuidCreatorAdapter.class, BaseValidationErrorMapper.class, CorrelationIdProvider.class, TariffV2PricingValidator.class})
public class TestingAppConfig {

    @Bean
    SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        http.securityMatcher("/**")
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .csrf(csrf -> csrf.disable());
        return http.build();
    }

    @TestConfiguration
    public static class MvcConfig implements MockMvcBuilderCustomizer {

        @Override
        public void customize(ConfigurableMockMvcBuilder<?> builder) {
            builder.apiVersionInserter(ApiVersionInserter.useMediaTypeParam("application/vnd.bikerental.v1+json"))
                    .alwaysDo(MockMvcResultHandlers.print());
        }
    }
}
