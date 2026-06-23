package com.github.jenkaby.bikerental.identity.infrastructure.security;

import com.github.jenkaby.bikerental.identity.domain.service.SessionRevoker;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.stereotype.Component;

@Component
class JdbcSessionRevoker implements SessionRevoker {

    private static final String DELETE_BY_PRINCIPAL = "DELETE FROM oauth2_authorization WHERE principal_name = ?";

    private final JdbcOperations jdbcOperations;

    JdbcSessionRevoker(JdbcOperations jdbcOperations) {
        this.jdbcOperations = jdbcOperations;
    }

    @Override
    public void revokeAllSessions(String principalName) {
        jdbcOperations.update(DELETE_BY_PRINCIPAL, principalName);
    }
}
