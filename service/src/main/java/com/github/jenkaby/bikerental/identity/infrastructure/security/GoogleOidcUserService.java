package com.github.jenkaby.bikerental.identity.infrastructure.security;

import com.github.jenkaby.bikerental.users.UserAuthFacade;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
class GoogleOidcUserService implements OAuth2UserService<OidcUserRequest, OidcUser> {

    private static final String EMAIL_ATTRIBUTE = "email";

    private final OidcUserService delegate = new OidcUserService();
    private final UserAuthFacade userAuthFacade;

    GoogleOidcUserService(UserAuthFacade userAuthFacade) {
        this.userAuthFacade = userAuthFacade;
    }

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        var oidcUser = delegate.loadUser(userRequest);
        var email = oidcUser.getEmail();
        if (email == null || !Boolean.TRUE.equals(oidcUser.getEmailVerified())) {
            throw new OAuth2AuthenticationException(new OAuth2Error("invalid_account"),
                    "Google account email is missing or not verified");
        }
        var view = userAuthFacade.findByEmail(email)
                .filter(u -> u.active())
                .orElseThrow(() -> new OAuth2AuthenticationException(new OAuth2Error("account_not_provisioned"),
                        "No active account is provisioned for " + email));
        var authorities = view.roles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toSet());
        return new DefaultOidcUser(authorities, oidcUser.getIdToken(), oidcUser.getUserInfo(), EMAIL_ATTRIBUTE);
    }
}
