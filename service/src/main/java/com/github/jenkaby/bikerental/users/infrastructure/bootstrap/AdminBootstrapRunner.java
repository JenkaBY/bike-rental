package com.github.jenkaby.bikerental.users.infrastructure.bootstrap;

import com.github.jenkaby.bikerental.users.application.config.BootstrapAdminProperties;
import com.github.jenkaby.bikerental.users.domain.model.Role;
import com.github.jenkaby.bikerental.users.domain.model.User;
import com.github.jenkaby.bikerental.users.domain.model.UserStatus;
import com.github.jenkaby.bikerental.users.domain.repository.UserRepository;
import com.github.jenkaby.bikerental.shared.domain.model.vo.EmailAddress;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Slf4j
@Component
@ConditionalOnProperty(name = "app.security.bootstrap.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
class AdminBootstrapRunner implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final BootstrapAdminProperties properties;

    @Override
    public void run(ApplicationArguments args) {
        if (properties.password() == null || properties.password().isBlank()) {
            throw new IllegalStateException("Bootstrap admin is enabled but no password is configured. "
                    + "Set BOOTSTRAP_ADMIN_PASSWORD, or disable bootstrapping via app.security.bootstrap.enabled=false");
        }
        if (!userRepository.isEmpty()) {
            return;
        }
        var admin = User.builder()
                .username(properties.username())
                .email(new EmailAddress(properties.email()))
                .displayName(properties.displayName())
                .passwordHash(passwordEncoder.encode(properties.password()))
                .status(UserStatus.ACTIVE)
                .mustChangePassword(true)
                .roles(Set.of(Role.ADMIN, Role.OPERATOR))
                .build();
        userRepository.save(admin);
        log.info("Bootstrapped initial admin user '{}'", properties.username());
    }
}
