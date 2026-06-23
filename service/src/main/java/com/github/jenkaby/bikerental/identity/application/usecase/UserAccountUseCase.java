package com.github.jenkaby.bikerental.identity.application.usecase;

import com.github.jenkaby.bikerental.identity.domain.model.User;

import java.util.List;
import java.util.UUID;

public interface UserAccountUseCase {

    UserWithTemporaryPassword createUser(CreateUserCommand command);

    User updateUser(UUID id, UpdateUserCommand command);

    User deactivateUser(UUID id);

    UserWithTemporaryPassword resetPassword(UUID id);

    List<User> listUsers();

    User getUser(UUID id);
}
