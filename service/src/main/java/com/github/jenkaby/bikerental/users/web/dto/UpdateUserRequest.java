package com.github.jenkaby.bikerental.users.web.dto;

import com.github.jenkaby.bikerental.users.domain.model.Role;
import com.github.jenkaby.bikerental.users.domain.model.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record UpdateUserRequest(

        @Size(max = 200)
        @Schema(description = "Human-friendly display name", example = "John Doe")
        String displayName,

        @Schema(description = "Replacement set of roles; ignored when empty")
        Set<Role> roles,

        @Schema(description = "Account status", example = "ACTIVE")
        UserStatus status
) {
}
