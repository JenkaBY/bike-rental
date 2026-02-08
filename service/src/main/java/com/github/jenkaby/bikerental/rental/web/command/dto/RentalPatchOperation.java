package com.github.jenkaby.bikerental.rental.web.command.dto;

import com.github.jenkaby.bikerental.rental.domain.model.RentalStatus;
import com.github.jenkaby.bikerental.rental.web.command.dto.validation.ValidRentalPatchOperation;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a single JSON Patch operation (RFC 6902).
 * Used for validating patch operations before applying them to Rental.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ValidRentalPatchOperation
public class RentalPatchOperation {

    /**
     * Operation type. Allowed values: "replace", "add".
     * - "replace": Replace existing value
     * - "add": Add new value (same as replace for our use case)
     */
    @NotNull(message = "Operation 'op' is required")
    private JsonPatchOperation op;

    /**
     * JSON Pointer path to the field to update.
     * Must start with "/" and reference a valid Rental field.
     * Allowed paths:
     * - /customerId
     * - /equipmentId
     * - /tariffId
     * - /duration
     * - /startTime
     * - /status
     */
    @NotBlank(message = "Path is required")
    private String path;

    /**
     * Value to set. Required for "replace" and "add" operations.
     * Type depends on the path:
     * - /customerId: UUID string
     * - /equipmentId: Long number
     * - /tariffId: Long number
     * - /duration: ISO-8601 duration string (e.g., "PT2H")
     * - /startTime: ISO-8601 date-time string (e.g., "2026-02-07T10:00:00")
     * - /status: {@link RentalStatus} enum value (e.g., "ACTIVE", "DRAFT", "COMPLETED", "CANCELLED")
     */
    private Object value;
}
