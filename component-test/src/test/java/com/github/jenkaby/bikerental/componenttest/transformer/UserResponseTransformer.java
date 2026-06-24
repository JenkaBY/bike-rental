package com.github.jenkaby.bikerental.componenttest.transformer;

import com.github.jenkaby.bikerental.componenttest.transformer.shared.Aliases;
import com.github.jenkaby.bikerental.users.domain.model.Role;
import com.github.jenkaby.bikerental.users.domain.model.UserStatus;
import com.github.jenkaby.bikerental.users.web.dto.UserResponse;
import io.cucumber.java.DataTableType;

import java.util.*;
import java.util.stream.Collectors;

public class UserResponseTransformer {

    @DataTableType
    public UserResponse userResponse(Map<String, String> entry) {
        var idValue = DataTableHelper.getStringOrNull(entry, "id");
        var id = idValue != null ? UUID.fromString(Aliases.getValueOrDefault(idValue)) : null;
        return new UserResponse(
                id,
                entry.get("username"),
                entry.get("email"),
                DataTableHelper.getStringOrNull(entry, "displayName"),
                UserStatus.valueOf(entry.getOrDefault("status", "ACTIVE")),
                Boolean.parseBoolean(entry.getOrDefault("mustChangePassword", "false")),
                parseRoles(entry.get("roles")),
                null
        );
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
