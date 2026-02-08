package com.github.jenkaby.bikerental.rental.application.util;

import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;


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

    public Long parseLong(Object value) {
        if (value instanceof Number num) {
            return num.longValue();
        }
        if (value instanceof String str) {
            return Long.parseLong(str);
        }
        return objectMapper.convertValue(value, Long.class);
    }

    public Duration parseDuration(Object value) {
        if (value instanceof String str) {
            return Duration.parse(str);
        }
        return objectMapper.convertValue(value, Duration.class);
    }

    public LocalDateTime parseLocalDateTime(Object value) {
        if (value instanceof String str) {
            return LocalDateTime.parse(str);
        }
        return objectMapper.convertValue(value, LocalDateTime.class);
    }

    public String parseString(Object value) {
        if (value instanceof String str) {
            return str;
        }
        return objectMapper.convertValue(value, String.class);
    }
}
