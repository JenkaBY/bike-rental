package com.github.jenkaby.bikerental.shared.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.UUID;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UuidMapper {

    default String toString(UUID uuid) {
        return uuid != null ? uuid.toString() : null;
    }

    default UUID toUuid(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
