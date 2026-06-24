package com.github.jenkaby.bikerental.users.web.dto;

import com.github.jenkaby.bikerental.users.domain.model.Role;
import com.github.jenkaby.bikerental.users.domain.model.UserStatus;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String username,
        String email,
        String displayName,
        UserStatus status,
        boolean mustChangePassword,
        Set<Role> roles,
        Instant lastLoginAt
) {
}
