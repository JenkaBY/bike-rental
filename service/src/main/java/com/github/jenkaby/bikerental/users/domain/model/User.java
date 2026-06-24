package com.github.jenkaby.bikerental.users.domain.model;

import com.github.jenkaby.bikerental.shared.domain.model.vo.EmailAddress;
import lombok.*;

import java.time.Instant;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class User {

    @Setter
    private UUID id;
    private final String username;
    private final EmailAddress email;
    private String passwordHash;
    private String displayName;
    private UserStatus status;
    private boolean mustChangePassword;
    private Set<Role> roles;
    private Instant lastLoginAt;

    public void assignRoles(Set<Role> newRoles) {
        this.roles = new HashSet<>(Objects.requireNonNull(newRoles, "roles must not be null"));
    }

    public void setTemporaryPassword(String encodedPassword) {
        this.passwordHash = Objects.requireNonNull(encodedPassword, "encodedPassword must not be null");
        this.mustChangePassword = true;
    }

    public void changePassword(String encodedPassword) {
        this.passwordHash = Objects.requireNonNull(encodedPassword, "encodedPassword must not be null");
        this.mustChangePassword = false;
    }

    public void rename(String newDisplayName) {
        this.displayName = newDisplayName;
    }

    public void disable() {
        this.status = UserStatus.DISABLED;
    }

    public void enable() {
        this.status = UserStatus.ACTIVE;
    }

    public void recordLogin(Instant when) {
        this.lastLoginAt = when;
    }

    public boolean isActive() {
        return this.status == UserStatus.ACTIVE;
    }

    public boolean hasPassword() {
        return this.passwordHash != null && !this.passwordHash.isBlank();
    }
}
