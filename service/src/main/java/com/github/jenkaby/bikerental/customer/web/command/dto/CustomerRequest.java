package com.github.jenkaby.bikerental.customer.web.command.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;

@Schema(description = "Request body for creating or updating a customer profile")
public record CustomerRequest(
        @Schema(description = "Customer phone number", example = "+79161234567")
        @NotBlank(message = "Phone is required")
        @Pattern(regexp = "^\\+?[0-9\\-\\s()]+$", message = "Phone format is invalid")
        String phone,

        @Schema(description = "First name", example = "Ivan")
        @NotBlank(message = "First name is required")
        String firstName,

        @Schema(description = "Last name", example = "Ivanov")
        @NotBlank(message = "Last name is required")
        String lastName,

        @Schema(description = "Email address", example = "ivan@example.com")
        @Email(message = "Email format is invalid")
        String email,

        @Schema(description = "Date of birth (must be in the past)", example = "1990-05-15")
        @Past(message = "Birth date must be in the past")
        LocalDate birthDate,

        @Schema(description = "Optional comments about the customer")
        String comments
) {
}
