package com.github.jenkaby.bikerental.users.domain.service;

public interface SessionRevoker {

    void revokeAllSessions(String principalName);
}
