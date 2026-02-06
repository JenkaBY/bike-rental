package com.github.jenkaby.bikerental.componenttest.transformer;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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

    public static Integer toInt(Map<String, String> entry, String field) {
        var value = entry.get(field);
        if (value == null) {
            return null;
        }
        value = value.trim();
        if (value.isEmpty()) {
            return null;
        }

        return Integer.valueOf(value);
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

    public static LocalDateTime toLocalDateTime(Map<String, String> entry, String field) {
        var value = getStringOrNull(entry, field);
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return LocalDateTime.parse(value.trim());
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

    public static Set<String> getSetOrDefault(Map<String, String> entry, String field, Set<String> defaultValue) {
        if ("null".equals(entry.get(field))) {
            return null;
        }
        return Optional.ofNullable(entry.get(field))
                .map(s -> Set.of(s.split(",")))
                .orElse(defaultValue);
    }
}
