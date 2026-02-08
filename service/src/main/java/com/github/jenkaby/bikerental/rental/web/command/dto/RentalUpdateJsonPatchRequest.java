package com.github.jenkaby.bikerental.rental.web.command.dto;

import com.github.jenkaby.bikerental.rental.web.command.dto.validation.ValidRentalPatchRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO for JSON Patch operations on Rental.
 * Represents an array of patch operations according to RFC 6902.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ValidRentalPatchRequest
public class RentalUpdateJsonPatchRequest {

    /**
     * Array of patch operations to apply to the rental.
     * Must contain at least one operation.
     */
    @NotNull(message = "Patch operations are required")
    @NotEmpty(message = "At least one patch operation is required")
    @Valid
    private List<@NotNull @Valid RentalPatchOperation> operations;
}
