package com.github.jenkaby.bikerental.users.application.service;

import com.github.jenkaby.bikerental.users.application.config.PasswordPolicyProperties;
import com.github.jenkaby.bikerental.users.application.usecase.CreateUserCommand;
import com.github.jenkaby.bikerental.users.application.usecase.UpdateUserCommand;
import com.github.jenkaby.bikerental.users.application.usecase.UserAccountUseCase;
import com.github.jenkaby.bikerental.users.application.usecase.UserWithTemporaryPassword;
import com.github.jenkaby.bikerental.users.domain.exception.DuplicateEmailException;
import com.github.jenkaby.bikerental.users.domain.exception.DuplicateUsernameException;
import com.github.jenkaby.bikerental.users.domain.exception.PasswordPolicyViolationException;
import com.github.jenkaby.bikerental.users.domain.model.User;
import com.github.jenkaby.bikerental.users.domain.model.UserStatus;
import com.github.jenkaby.bikerental.users.domain.repository.UserRepository;
import com.github.jenkaby.bikerental.users.SessionRevoker;
import com.github.jenkaby.bikerental.shared.domain.model.vo.EmailAddress;
import com.github.jenkaby.bikerental.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
class UserAccountService implements UserAccountUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SessionRevoker sessionRevoker;
    private final PasswordGenerator passwordGenerator;
    private final PasswordPolicyProperties passwordPolicyProperties;

    @Override
    public UserWithTemporaryPassword createUser(CreateUserCommand command) {
        if (command.roles() == null || command.roles().isEmpty()) {
            throw new IllegalArgumentException("At least one role must be provided");
        }
        if (userRepository.existsByUsername(command.username())) {
            throw new DuplicateUsernameException(command.username());
        }
        if (userRepository.existsByEmail(command.email())) {
            throw new DuplicateEmailException(command.email());
        }
        var temporaryPassword = hasText(command.password()) ? command.password() : passwordGenerator.generate();
        validatePassword(temporaryPassword);
        var user = User.builder()
                .username(command.username())
                .email(new EmailAddress(command.email()))
                .displayName(command.displayName())
                .passwordHash(passwordEncoder.encode(temporaryPassword))
                .status(UserStatus.ACTIVE)
                .mustChangePassword(true)
                .roles(new HashSet<>(command.roles()))
                .build();
        var saved = userRepository.save(user);
        return new UserWithTemporaryPassword(saved, temporaryPassword);
    }

    @Override
    public User updateUser(UUID id, UpdateUserCommand command) {
        var user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(User.class, id));
        if (hasText(command.displayName())) {
            user.rename(command.displayName());
        }
        if (command.roles() != null && !command.roles().isEmpty()) {
            user.assignRoles(command.roles());
        }
        if (command.status() != null) {
            applyStatus(user, command.status());
        }
        return userRepository.save(user);
    }

    @Override
    public User deactivateUser(UUID id) {
        var user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(User.class, id));
        user.disable();
        var saved = userRepository.save(user);
        sessionRevoker.revokeAllSessions(user.getUsername());
        return saved;
    }

    @Override
    public UserWithTemporaryPassword resetPassword(UUID id) {
        var user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(User.class, id));
        var temporaryPassword = passwordGenerator.generate();
        user.setTemporaryPassword(passwordEncoder.encode(temporaryPassword));
        var saved = userRepository.save(user);
        sessionRevoker.revokeAllSessions(user.getUsername());
        return new UserWithTemporaryPassword(saved, temporaryPassword);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> listUsers() {
        return userRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public User getUser(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(User.class, id));
    }

    private void applyStatus(User user, UserStatus status) {
        if (status == UserStatus.DISABLED) {
            user.disable();
        } else {
            user.enable();
        }
    }

    private void validatePassword(String password) {
        if (password == null || password.length() < passwordPolicyProperties.minLength()) {
            throw new PasswordPolicyViolationException(
                    "Password must be at least %d characters long".formatted(passwordPolicyProperties.minLength()));
        }
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
