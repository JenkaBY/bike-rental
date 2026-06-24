package com.github.jenkaby.bikerental.users.infrastructure.service;

import com.github.jenkaby.bikerental.users.UserAuthFacade;
import com.github.jenkaby.bikerental.users.UserAuthView;
import com.github.jenkaby.bikerental.users.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
class UserAuthFacadeImpl implements UserAuthFacade {

    private final UserRepository userRepository;

    @Override
    public Optional<UserAuthView> findByUsername(String username) {
        return userRepository.findByUsername(username).map(this::toView);
    }

    @Override
    public Optional<UserAuthView> findByEmail(String email) {
        return userRepository.findByEmail(email).map(this::toView);
    }

    private UserAuthView toView(com.github.jenkaby.bikerental.users.domain.model.User user) {
        var roles = user.getRoles().stream()
                .map(Enum::name)
                .collect(java.util.stream.Collectors.toSet());
        return new UserAuthView(
                user.getId(),
                user.getUsername(),
                user.getPasswordHash(),
                roles,
                user.isActive(),
                user.isMustChangePassword()
        );
    }
}
