package com.github.jenkaby.bikerental.identity.application.usecase;

import com.github.jenkaby.bikerental.identity.domain.model.User;

public record UserWithTemporaryPassword(User user, String temporaryPassword) {
}
