package com.github.jenkaby.bikerental.equipment.web.command.dto;

import com.github.jenkaby.bikerental.shared.web.support.Slug;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record EquipmentRequest(

        @NotBlank(message = "Serial number is required")
        @Size(max = 50, message = "Serial number must not exceed 50 characters")
        String serialNumber,

        @Size(max = 100, message = "UID must not exceed 100 characters")
        String uid,

        @Slug
        String typeSlug,

        @Slug
        String statusSlug,

        @Size(max = 200, message = "Model must not exceed 200 characters")
        String model,

        LocalDate commissionedAt,

        String condition
) {
}
