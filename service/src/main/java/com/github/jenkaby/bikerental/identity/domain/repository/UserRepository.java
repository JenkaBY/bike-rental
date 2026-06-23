package com.github.jenkaby.bikerental.identity.domain.repository;

import com.github.jenkaby.bikerental.identity.domain.model.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository {

    User save(User user);

    Optional<User> findById(UUID id);

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean isEmpty();

    List<User> findAll();
}
