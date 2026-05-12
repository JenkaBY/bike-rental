package com.github.jenkaby.bikerental.rental.web.command.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * JSON Patch operation types according to RFC 6902.
 * Only operations supported for Rental updates are included.
 */
public enum JsonPatchOperation {
    /**
     * Replace operation: replaces the value at the target location with a new value.
     */
    REPLACE("replace");


    private final String value;

    JsonPatchOperation(String value) {
        this.value = value;
    }

    /**
     * Returns the JSON string representation of this operation.
     * Used for serialization to JSON.
     */
    @JsonValue
    public String getValue() {
        return value;
    }

    /**
     * Creates JsonPatchOperation from string value.
     * Used for deserialization from JSON.
     *
     * @param value the string value (e.g., "replace", "add")
     * @return JsonPatchOperation enum value
     * @throws IllegalArgumentException if value is not recognized
     */
    @JsonCreator
    public static JsonPatchOperation fromValue(String value) {
        if (value == null) {
            return null;
        }
        for (JsonPatchOperation operation : values()) {
            if (operation.value.equals(value)) {
                return operation;
            }
        }
        throw new IllegalArgumentException("Unknown JSON Patch operation: " + value);
    }
}
