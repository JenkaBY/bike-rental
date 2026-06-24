package com.github.jenkaby.bikerental.users.application.service;

import com.github.jenkaby.bikerental.users.application.config.PasswordPolicyProperties;
import com.github.jenkaby.bikerental.users.application.usecase.SelfServiceUseCase;
import com.github.jenkaby.bikerental.users.domain.exception.InvalidCurrentPasswordException;
import com.github.jenkaby.bikerental.users.domain.exception.PasswordPolicyViolationException;
import com.github.jenkaby.bikerental.users.domain.model.User;
import com.github.jenkaby.bikerental.users.domain.repository.UserRepository;
import com.github.jenkaby.bikerental.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
class SelfServiceService implements SelfServiceUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordPolicyProperties passwordPolicyProperties;

    @Override
    @Transactional(readOnly = true)
    public User getById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(User.class, userId));
    }

    @Override
    public void changeOwnPassword(UUID userId, String currentPassword, String newPassword) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(User.class, userId));
        if (!user.hasPassword() || !passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new InvalidCurrentPasswordException();
        }
        if (newPassword == null || newPassword.length() < passwordPolicyProperties.minLength()) {
            throw new PasswordPolicyViolationException(
                    "Password must be at least %d characters long".formatted(passwordPolicyProperties.minLength()));
        }
        user.changePassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}
