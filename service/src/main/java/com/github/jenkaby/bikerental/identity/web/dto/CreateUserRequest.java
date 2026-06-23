package com.github.jenkaby.bikerental.identity.web.dto;

import com.github.jenkaby.bikerental.identity.domain.model.Role;
import com.github.jenkaby.bikerental.shared.web.support.Password;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record CreateUserRequest(

        @NotBlank
        @Size(max = 100)
        @Schema(description = "Unique login name", example = "j.doe")
        String username,

        @NotBlank
        @Email
        @Size(max = 255)
        @Schema(description = "Unique email address", example = "j.doe@example.com")
        String email,

        @Size(max = 200)
        @Schema(description = "Human-friendly display name", example = "John Doe")
        String displayName,

        @NotEmpty
        @Schema(description = "Roles assigned to the account; at least one is required")
        Set<Role> roles,

        @Password
        @Schema(description = "Optional initial temporary password; generated when omitted")
        String password
) {
}
