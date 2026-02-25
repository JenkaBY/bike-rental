package com.github.jenkaby.bikerental.shared.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;


@Configuration
public class MessageSourceConfig {

    @Bean
    Locale defaultLocale(AppProperties appProperties) {
        return Locale.forLanguageTag(appProperties.defaultLocale());
    }

    @Bean
    public MessageSource messageSource(Locale defaultLocale) {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("messages");
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setDefaultLocale(defaultLocale);
        messageSource.setFallbackToSystemLocale(true);
        messageSource.setUseCodeAsDefaultMessage(true);
        return messageSource;
    }

    @Bean
    public LocaleResolver localeResolver(Locale defaultLocale) {
        AcceptHeaderLocaleResolver localeResolver = new AcceptHeaderLocaleResolver();
        localeResolver.setDefaultLocale(defaultLocale);
        List<Locale> supportedLocales = Arrays.asList(
                Locale.forLanguageTag("ru"),
                defaultLocale
        );
        localeResolver.setSupportedLocales(supportedLocales);

        return localeResolver;
    }
}
