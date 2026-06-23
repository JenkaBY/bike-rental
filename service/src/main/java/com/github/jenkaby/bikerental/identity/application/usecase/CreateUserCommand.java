package com.github.jenkaby.bikerental.identity.application.usecase;

import com.github.jenkaby.bikerental.identity.domain.model.Role;

import java.util.Set;

public record CreateUserCommand(
        String username,
        String email,
        String displayName,
        Set<Role> roles,
        String password
) {
}
