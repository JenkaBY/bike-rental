package com.github.jenkaby.bikerental.users.application.usecase;

import com.github.jenkaby.bikerental.users.domain.model.User;

import java.util.UUID;

public interface SelfServiceUseCase {

    User getById(UUID userId);

    void changeOwnPassword(UUID userId, String currentPassword, String newPassword);
}
