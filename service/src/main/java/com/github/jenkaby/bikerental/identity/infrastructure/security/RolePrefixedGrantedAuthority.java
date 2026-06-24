package com.github.jenkaby.bikerental.identity.infrastructure.security;

import org.springframework.security.core.GrantedAuthority;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public final class RolePrefixedGrantedAuthority implements GrantedAuthority {

    public static final String ROLE_PREFIX = "ROLE_";

    private final String authority;

    public RolePrefixedGrantedAuthority(String role) {
        this.authority = ROLE_PREFIX + Objects.requireNonNull(role, "role must not be null");
    }

    @JsonCreator
    static RolePrefixedGrantedAuthority fromAuthority(@JsonProperty("authority") String fullAuthority) {
        String role = fullAuthority.startsWith(ROLE_PREFIX)
                ? fullAuthority.substring(ROLE_PREFIX.length())
                : fullAuthority;
        return new RolePrefixedGrantedAuthority(role);
    }

    @Override
    public String getAuthority() {
        return authority;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof GrantedAuthority granted && authority.equals(granted.getAuthority());
    }

    @Override
    public int hashCode() {
        return authority.hashCode();
    }

    @Override
    public String toString() {
        return authority;
    }
}
