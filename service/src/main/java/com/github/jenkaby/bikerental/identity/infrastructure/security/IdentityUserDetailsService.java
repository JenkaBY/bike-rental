package com.github.jenkaby.bikerental.identity.infrastructure.security;

import com.github.jenkaby.bikerental.identity.domain.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
class IdentityUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    IdentityUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(IdentityUserPrincipal::from)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }
}
