package com.github.jenkaby.bikerental.identity.infrastructure.security;

import com.github.jenkaby.bikerental.identity.domain.model.Role;
import com.github.jenkaby.bikerental.identity.domain.model.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.UUID;

@Getter
public class IdentityUserPrincipal implements UserDetails {

    private final UUID userId;
    private final String username;
    private final String password;
    private final boolean enabled;
    private final boolean mustChangePassword;
    private final Collection<? extends GrantedAuthority> authorities;

    private IdentityUserPrincipal(UUID userId, String username, String password, boolean enabled,
                                  boolean mustChangePassword, Collection<? extends GrantedAuthority> authorities) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.enabled = enabled;
        this.mustChangePassword = mustChangePassword;
        this.authorities = authorities;
    }

    public static IdentityUserPrincipal from(User user) {
        var authorities = user.getRoles().stream()
                .map(Role::name)
                .map(RolePrefixedGrantedAuthority::new)
                .toList();
        var password = user.hasPassword()
                ? user.getPasswordHash()
                : "{noop}" + UUID.randomUUID();
        return new IdentityUserPrincipal(
                user.getId(),
                user.getUsername(),
                password,
                user.isActive(),
                user.isMustChangePassword(),
                authorities);
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
}
