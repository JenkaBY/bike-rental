package com.github.jenkaby.bikerental.identity.application.usecase;

import com.github.jenkaby.bikerental.identity.domain.model.Role;
import com.github.jenkaby.bikerental.identity.domain.model.UserStatus;

import java.util.Set;

public record UpdateUserCommand(
        String displayName,
        Set<Role> roles,
        UserStatus status
) {
}
