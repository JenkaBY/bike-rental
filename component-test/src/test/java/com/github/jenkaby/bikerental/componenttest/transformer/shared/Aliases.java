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
//        Operator ids group starts with 22222222-
        ALIASES.put("OP1", "22222222-2222-2222-2222-222222222221");
        ALIASES.put("OP2", "22222222-2222-2222-2222-222222222222");
//       Payment ids group starts with 33333333-
        ALIASES.put("PAY1", "33333333-3333-3333-3333-333333333331");
        ALIASES.put("PAY2", "33333333-3333-3333-3333-333333333332");
        ALIASES.put("PAY3", "33333333-3333-3333-3333-333333333333");
        ALIASES.put("PAY4", "33333333-3333-3333-3333-333333333334");
    }

    public static @Nullable UUID getCustomerId(@NonNull String alias) {
        return Optional.ofNullable(ALIASES.get(alias))
                .map(UUID::fromString)
                .orElse(null);
    }

    public static @Nullable UUID getPaymentId(@NonNull String alias) {
        return Optional.ofNullable(ALIASES.get(alias))
                .map(UUID::fromString)
                .orElse(null);
    }

    public static @Nullable String getValue(@NonNull String alias) {
        return ALIASES.get(alias);
    }

    public static @Nullable String getValueOrDefault(@NonNull String alias) {
        return ALIASES.getOrDefault(alias, alias);
    }

    public static @Nullable String getOperatorId(String alias) {
        return Optional.ofNullable(ALIASES.get(alias))
                .orElse(null);
    }
}
