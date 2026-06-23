package com.github.jenkaby.bikerental.identity.infrastructure.security;

import com.github.jenkaby.bikerental.identity.domain.model.Role;
import com.github.jenkaby.bikerental.identity.domain.model.User;
import com.github.jenkaby.bikerental.identity.domain.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
class GoogleOidcUserService implements OAuth2UserService<OidcUserRequest, OidcUser> {

    private static final String EMAIL_ATTRIBUTE = "email";

    private final OidcUserService delegate = new OidcUserService();
    private final UserRepository userRepository;

    GoogleOidcUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser oidcUser = delegate.loadUser(userRequest);
        String email = oidcUser.getEmail();
        if (email == null || !Boolean.TRUE.equals(oidcUser.getEmailVerified())) {
            throw new OAuth2AuthenticationException(new OAuth2Error("invalid_account"),
                    "Google account email is missing or not verified");
        }
        User user = userRepository.findByEmail(email)
                .filter(User::isActive)
                .orElseThrow(() -> new OAuth2AuthenticationException(new OAuth2Error("account_not_provisioned"),
                        "No active account is provisioned for " + email));
        Set<SimpleGrantedAuthority> authorities = user.getRoles().stream()
                .map(Role::name)
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toSet());
        return new DefaultOidcUser(authorities, oidcUser.getIdToken(), oidcUser.getUserInfo(), EMAIL_ATTRIBUTE);
    }
}
