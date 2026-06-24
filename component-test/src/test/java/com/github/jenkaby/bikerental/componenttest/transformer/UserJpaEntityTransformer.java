package com.github.jenkaby.bikerental.componenttest.transformer;

import com.github.jenkaby.bikerental.componenttest.transformer.shared.Aliases;
import com.github.jenkaby.bikerental.users.domain.model.Role;
import com.github.jenkaby.bikerental.users.domain.model.UserStatus;
import com.github.jenkaby.bikerental.users.infrastructure.persistence.entity.UserJpaEntity;
import io.cucumber.java.DataTableType;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

public class UserJpaEntityTransformer {

    private static final PasswordEncoder PASSWORD_ENCODER = PasswordEncoderFactories.createDelegatingPasswordEncoder();

    @DataTableType
    public UserJpaEntity createUserAccount(Map<String, String> entry) {
        var user = new UserJpaEntity();
        var id = DataTableHelper.getStringOrNull(entry, "id");
        user.setId(id != null ? UUID.fromString(Aliases.getValueOrDefault(id)) : UUID.randomUUID());
        user.setUsername(entry.get("username"));
        user.setEmail(entry.get("email"));
        var password = DataTableHelper.getStringOrNull(entry, "password");
        user.setPasswordHash(password != null ? PASSWORD_ENCODER.encode(password) : null);
        user.setDisplayName(DataTableHelper.getStringOrNull(entry, "displayName"));
        user.setStatus(UserStatus.valueOf(entry.getOrDefault("status", "ACTIVE")));
        user.setMustChangePassword(Boolean.parseBoolean(entry.getOrDefault("mustChangePassword", "false")));
        user.setRoles(parseRoles(entry.get("roles")));
        user.setCreatedAt(Instant.now());
        return user;
    }

    private Set<Role> parseRoles(String raw) {
        if (raw == null || raw.isBlank()) {
            return new HashSet<>();
        }
        return Arrays.stream(raw.split(","))
                .map(String::trim)
                .map(Role::valueOf)
                .collect(Collectors.toCollection(HashSet::new));
    }
}
