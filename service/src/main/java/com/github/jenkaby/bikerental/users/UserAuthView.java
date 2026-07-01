package com.github.jenkaby.bikerental.users;

import java.util.Set;
import java.util.UUID;

public record UserAuthView(
        UUID id,
        String username,
        String passwordHash,
        Set<String> roles,
        boolean active,
        boolean mustChangePassword
) {
}
