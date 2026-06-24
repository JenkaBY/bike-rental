package com.github.jenkaby.bikerental.users.application.usecase;

import com.github.jenkaby.bikerental.users.domain.model.User;

public record UserWithTemporaryPassword(User user, String temporaryPassword) {
}
