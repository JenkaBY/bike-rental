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
//       FinanceAccount ids group starts with 44444444-
        ALIASES.put("ACC_S", "00000000-0000-0000-0000-000000000001");
        ALIASES.put("ACC2", "44444444-3333-3333-3333-333333333332");
        ALIASES.put("ACC3", "44444444-3333-3333-3333-333333333333");
        ALIASES.put("ACC4", "44444444-3333-3333-3333-333333333334");
        //      Customer SubLedger ids group starts with 5555555-
        ALIASES.put("L_C_W1", "55555555-3333-3333-3333-333333333331");
        ALIASES.put("L_C_H1", "55555555-3333-3333-3333-333333333332");
        ALIASES.put("L_C_W2", "55555555-3333-3333-3333-333333333333");
        ALIASES.put("L_C_H2", "55555555-3333-3333-3333-333333333334");
        ALIASES.put("L_C_W3", "55555555-3333-3333-3333-333333333335");
        ALIASES.put("L_C_H3", "55555555-3333-3333-3333-333333333336");
        ALIASES.put("L_C_W4", "55555555-3333-3333-3333-333333333337");
        ALIASES.put("L_C_H4", "55555555-3333-3333-3333-333333333338");
        //      System SubLedger ids group starts with 00000000-
        ALIASES.put("L_S_CASH", "10000000-0000-0001-0000-000000000001");
        ALIASES.put("L_S_CARD", "10000000-0000-0001-0000-000000000002");
        ALIASES.put("L_S_TRAN", "10000000-0000-0001-0000-000000000003");
        ALIASES.put("L_S_REV",  "10000000-0000-0001-0000-000000000004");
        ALIASES.put("L_S_ADJ",  "10000000-0000-0001-0000-000000000005");
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
