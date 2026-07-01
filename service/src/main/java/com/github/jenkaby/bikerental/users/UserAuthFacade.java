package com.github.jenkaby.bikerental.users;

import java.util.Optional;

public interface UserAuthFacade {

    Optional<UserAuthView> findByUsername(String username);

    Optional<UserAuthView> findByEmail(String email);
}
