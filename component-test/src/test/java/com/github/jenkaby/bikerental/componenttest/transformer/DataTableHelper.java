package com.github.jenkaby.bikerental.componenttest.transformer;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import java.util.stream.Stream;

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

    public static Instant toInstant(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return localDateTime.atZone(ZoneId.systemDefault()).toInstant();
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

    public static Instant parseLocalDateTimeToInstant(Map<String, String> entry, String field) {
        var localDateTime = toLocalDateTime(entry, field);
        return toInstant(localDateTime);
    }

    public static Set<String> getSetOrDefault(Map<String, String> entry, String field, Set<String> defaultValue) {
        if ("null".equals(entry.get(field))) {
            return null;
        }
        return Optional.ofNullable(entry.get(field))
                .map(s -> Set.of(s.split(",")))
                .orElse(defaultValue);
    }

    public static Duration toDuration(Map<String, String> entry, String field) {
        var value = entry.get(field);
        if (value == null) {
            return null;
        }
        if ("null".equals(entry.get(field))) {
            return null;
        }
        var durationString = entry.get(field);
        return Duration.parse(durationString);
    }

    public static Boolean toBooleanOrNull(Map<String, String> entry, String field) {
        var value = getStringOrNull(entry, field);
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return Boolean.parseBoolean(value.trim());
    }

    public static List<Long> toLongList(Map<String, String> entry, String field) {
        var value = getStringOrNull(entry, field);
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return Stream.of(value.split(","))
                .map(String::trim)
                .map(Long::valueOf)
                .toList();
    }
}
