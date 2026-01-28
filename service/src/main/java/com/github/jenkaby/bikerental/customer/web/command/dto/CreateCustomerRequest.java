package com.github.jenkaby.bikerental.customer.web.command.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;

public record CreateCustomerRequest(
        @NotBlank(message = "Phone is required")
        @Pattern(regexp = "^\\+?[0-9\\-\\s()]+$", message = "Phone format is invalid")
        String phone,

        @NotBlank(message = "First name is required")
        String firstName,

        @NotBlank(message = "Last name is required")
        String lastName,

        String email,

        @Past(message = "Birth date must be in the past")
        LocalDate birthDate
) {
}
