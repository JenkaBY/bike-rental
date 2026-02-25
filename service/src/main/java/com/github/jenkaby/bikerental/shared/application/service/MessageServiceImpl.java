package com.github.jenkaby.bikerental.shared.application.service;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.util.Locale;


@Service
public class MessageServiceImpl implements MessageService {

    private final MessageSource messageSource;

    public MessageServiceImpl(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @Override
    public @NonNull String getMessage(@NonNull String code) {
        Locale locale = getCurrentLocale();
        return messageSource.getMessage(code, null, locale);
    }

    @Override
    public @NonNull String getMessage(@NonNull String code, @Nullable Object... args) {
        Locale locale = getCurrentLocale();
        return messageSource.getMessage(code, args, locale);
    }

    @Override
    public @NonNull String getMessage(@NonNull String code, @NonNull Locale locale) {
        return messageSource.getMessage(code, null, locale);
    }

    @Override
    public @NonNull String getMessage(@NonNull String code, @NonNull Locale locale, @Nullable Object... args) {
        return messageSource.getMessage(code, args, locale);
    }

    private Locale getCurrentLocale() {
        return LocaleContextHolder.getLocale();
    }
}
