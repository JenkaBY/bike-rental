package com.github.jenkaby.bikerental.rental.infrastructure.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.UUID;


@Slf4j
@Component
public class PatchValueParser {

    private final ObjectMapper objectMapper;

    public PatchValueParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public UUID parseUUID(Object value) {
        if (value instanceof String str) {
            return UUID.fromString(str);
        }
        return objectMapper.convertValue(value, UUID.class);
    }

    public Integer parseInt(Object value) {
        if (value instanceof Number num) {
            return num.intValue();
        }
        if (value instanceof String str) {
            return Integer.valueOf(str);
        }
        return objectMapper.convertValue(value, Integer.class);
    }

    public List<Long> parseListOfLong(Object value) {
        log.info("Value is class: {} and value: {}", value.getClass(), value);
        if (value instanceof List<?> list) {
            return list.stream()
                    .filter(item -> item instanceof Number)
                    .map(item -> ((Number) item).longValue())
                    .toList();
        }
        var listLongType = objectMapper.getTypeFactory().constructCollectionType(List.class, Long.class);
        if (value instanceof String str) {
            return objectMapper.readValue(str, listLongType);
        }
        return objectMapper.convertValue(value, listLongType);
    }

    public String parseString(Object value) {
        if (value instanceof String str) {
            return str;
        }
        return objectMapper.convertValue(value, String.class);
    }
}
