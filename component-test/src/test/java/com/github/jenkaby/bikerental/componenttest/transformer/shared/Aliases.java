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
        ALIASES = new HashMap<>(64);
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
        ALIASES.put("ACC1", "44444444-3333-3333-3333-333333333331");
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
        ALIASES.put("L_S_REV", "10000000-0000-0001-0000-000000000004");
        ALIASES.put("L_S_ADJ", "10000000-0000-0001-0000-000000000005");
        // Idempotency keys
        ALIASES.put("IDK1", "44444444-4444-4444-4444-444444444441");
        ALIASES.put("IDK2", "44444444-4444-4444-4444-444444444442");
        ALIASES.put("IDK3", "44444444-4444-4444-4444-444444444443");
        ALIASES.put("IDK4", "44444444-4444-4444-4444-444444444444");
        ALIASES.put("IDK5", "44444444-4444-4444-4444-444444444445");
        // Transaction ids group starts with 66666666-
        ALIASES.put("TX1", "66666666-6666-6666-6666-666666666661");
        ALIASES.put("TX2", "66666666-6666-6666-6666-666666666662");
        ALIASES.put("TX3", "66666666-6666-6666-6666-666666666663");
        ALIASES.put("TX4", "66666666-6666-6666-6666-666666666664");
        ALIASES.put("TX5", "66666666-6666-6666-6666-666666666665");
        ALIASES.put("TX6", "66666666-6666-6666-6666-666666666666");
        // Transaction entreis ids group starts with 77777777-
        ALIASES.put("TRE1", "77777777-7777-7777-7777-777777777771");
        ALIASES.put("TRE2", "77777777-7777-7777-7777-777777777772");
        ALIASES.put("TRE3", "77777777-7777-7777-7777-777777777773");
        ALIASES.put("TRE4", "77777777-7777-7777-7777-777777777774");
        ALIASES.put("TRE5", "77777777-7777-7777-7777-777777777775");
        ALIASES.put("TRE6", "77777777-7777-7777-7777-777777777776");
        ALIASES.put("TRE7", "77777777-7777-7777-7777-777777777777");
        ALIASES.put("TRE8", "77777777-7777-7777-7777-777777777778");
        ALIASES.put("TRE9", "77777777-7777-7777-7777-777777777779");
        // Source / Rental aliases — stored as Long string because rental.id is Long
        ALIASES.put("RENT1", "1001");
        ALIASES.put("RENT2", "1002");
    }

    public static @Nullable UUID getUuid(@NonNull String alias) {
        return Optional.ofNullable(ALIASES.get(alias))
                .map(UUID::fromString)
                .orElse(null);
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
