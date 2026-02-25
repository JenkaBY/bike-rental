package com.github.jenkaby.bikerental.shared.application.service;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Locale;


public interface MessageService {

    @NonNull
    String getMessage(@NonNull String code);

    @NonNull
    String getMessage(@NonNull String code, @Nullable Object... args);

    @NonNull
    String getMessage(@NonNull String code, @NonNull Locale locale);

    @NonNull
    String getMessage(@NonNull String code, @NonNull Locale locale, @Nullable Object... args);
}
