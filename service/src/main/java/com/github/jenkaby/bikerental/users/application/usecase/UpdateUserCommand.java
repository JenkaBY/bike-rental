package com.github.jenkaby.bikerental.users.application.usecase;

import com.github.jenkaby.bikerental.users.domain.model.Role;
import com.github.jenkaby.bikerental.users.domain.model.UserStatus;

import java.util.Set;

public record UpdateUserCommand(
        String displayName,
        Set<Role> roles,
        UserStatus status
) {
}
