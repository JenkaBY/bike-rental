package com.github.jenkaby.bikerental.users;

public interface SessionRevoker {

    void revokeAllSessions(String principalName);
}
