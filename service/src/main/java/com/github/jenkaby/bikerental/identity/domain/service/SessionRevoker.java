package com.github.jenkaby.bikerental.identity.domain.service;

public interface SessionRevoker {

    void revokeAllSessions(String principalName);
}
