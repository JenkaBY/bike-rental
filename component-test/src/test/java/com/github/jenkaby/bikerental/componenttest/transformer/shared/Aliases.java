package com.github.jenkaby.bikerental.componenttest.transformer.shared;

import lombok.experimental.UtilityClass;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@UtilityClass
public class Aliases {

    private static final Map<String, String> ALIASES;

    static {
        ALIASES = new HashMap<>();
//            Customer ids group starts with 11111111-
        ALIASES.put("CUS1", "11111111-1111-1111-1111-111111111111");
        ALIASES.put("CUS2", "11111111-1111-1111-1111-111111111112");
        ALIASES.put("CUS3", "11111111-1111-1111-1111-111111111113");
        ALIASES.put("CUS4", "11111111-1111-1111-1111-111111111114");
        ALIASES.put("CUS5", "11111111-1111-1111-1111-111111111115");
    }

    public static @Nullable UUID getCustomerId(@NonNull String alias) {
        return Optional.ofNullable(ALIASES.get(alias))
                .map(UUID::fromString)
                .orElse(null);
    }

    public static @Nullable String getValue(@NonNull String alias) {
        return ALIASES.get(alias);
    }
}
