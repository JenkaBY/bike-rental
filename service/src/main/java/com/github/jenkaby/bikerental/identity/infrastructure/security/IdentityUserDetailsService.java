package com.github.jenkaby.bikerental.identity.infrastructure.security;

import com.github.jenkaby.bikerental.users.UserAuthFacade;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
class IdentityUserDetailsService implements UserDetailsService {

    private final UserAuthFacade userAuthFacade;

    IdentityUserDetailsService(UserAuthFacade userAuthFacade) {
        this.userAuthFacade = userAuthFacade;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        return userAuthFacade.findByUsername(username)
                .map(IdentityUserPrincipal::from)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }
}
