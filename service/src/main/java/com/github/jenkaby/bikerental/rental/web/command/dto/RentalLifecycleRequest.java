package com.github.jenkaby.bikerental.rental.web.command.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record RentalLifecycleRequest(
        @NotNull LifecycleStatus status,
        @NotEmpty String operatorId
) {
}