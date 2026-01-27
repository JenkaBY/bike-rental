package com.github.jenkaby.bikerental.componenttest.transformer;

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
}
