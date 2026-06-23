package com.github.jenkaby.bikerental.componenttest.transformer.shared;

import com.github.jenkaby.bikerental.identity.domain.model.Role;
import lombok.experimental.UtilityClass;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

@UtilityClass
public class RoleHelper {

    public static Set<Role> parseRoles(String raw) {
        if (raw == null || raw.isBlank()) {
            return Set.of();
        }
        return Arrays.stream(raw.split(","))
                .map(String::trim)
                .map(Role::valueOf)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
