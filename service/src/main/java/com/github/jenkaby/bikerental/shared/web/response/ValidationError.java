package com.github.jenkaby.bikerental.shared.web.response;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public record ValidationError(@Nullable String field, @NonNull String code, @Nullable Object params) {
}
