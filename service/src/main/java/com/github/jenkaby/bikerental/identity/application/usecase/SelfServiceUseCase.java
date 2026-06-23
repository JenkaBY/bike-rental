package com.github.jenkaby.bikerental.identity.application.usecase;

import com.github.jenkaby.bikerental.identity.domain.model.User;

import java.util.UUID;

public interface SelfServiceUseCase {

    User getById(UUID userId);

    void changeOwnPassword(UUID userId, String currentPassword, String newPassword);
}
