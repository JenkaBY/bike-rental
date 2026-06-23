package com.github.jenkaby.bikerental.identity.infrastructure.security;

import org.springframework.security.core.GrantedAuthority;

import java.util.Objects;

public final class RolePrefixedGrantedAuthority implements GrantedAuthority {

    public static final String ROLE_PREFIX = "ROLE_";

    private final String authority;

    public RolePrefixedGrantedAuthority(String role) {
        this.authority = ROLE_PREFIX + Objects.requireNonNull(role, "role must not be null");
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
