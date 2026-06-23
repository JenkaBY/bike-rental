package com.github.jenkaby.bikerental.identity.web.dto;

import com.github.jenkaby.bikerental.shared.web.support.Password;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record ChangePasswordRequest(

        @NotBlank
        @Schema(description = "The current password")
        String currentPassword,

        @NotBlank
        @Password
        @Schema(description = "The new password (8-20 characters, at least one letter and one digit)")
        String newPassword
) {
}
