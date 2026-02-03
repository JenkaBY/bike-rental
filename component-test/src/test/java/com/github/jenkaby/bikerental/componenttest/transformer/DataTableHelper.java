package com.github.jenkaby.bikerental.componenttest.transformer;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class DataTableHelper {


    public static UUID getUuidOrDefault(Map<String, String> table, String field, UUID defaultValue) {
        return Optional.ofNullable(table.get(field))
                .map(UUID::fromString)
                .orElse(defaultValue);
    }

    public static String getStringOrNull(Map<String, String> map, String key) {
        return "null".equals(map.get(key)) ? null : map.get(key);
    }

    public static Long toLong(Map<String, String> entry, String field) {
        var value = entry.get(field);
        if (value == null) {
            return null;
        }
        value = value.trim();
        if (value.isEmpty()) {
            return null;
        }

        return Long.valueOf(value);
    }

    public static BigDecimal toBigDecimal(Map<String, String> entry, String field) {
        var value = entry.get(field);
        if (value == null) {
            return null;
        }
        value = value.trim();
        if (value.isEmpty()) {
            return null;
        }

        return new BigDecimal(value);
    }

    public static LocalDate toLocalDate(Map<String, String> entry, String field) {
        var value = entry.get(field);
        if (value == null) {
            return null;
        }
        value = value.trim();
        if (value.isEmpty()) {
            return null;
        }
        return LocalDate.parse(value);
    }

    public static Instant toInstant(Map<String, String> entry, String field) {
        var value = entry.get(field);
        if (value == null) {
            return null;
        }
        value = value.trim();
        if (value.isEmpty()) {
            return null;
        }
        return Instant.parse(value);
    }
}
