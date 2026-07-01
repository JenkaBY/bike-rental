package com.github.jenkaby.bikerental.identity.infrastructure.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.server.authorization.oidc.authentication.OidcLogoutAuthenticationToken;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

final class StatelessOidcLogoutResponseHandler implements AuthenticationSuccessHandler {

    private final SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();
    private final RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        logoutHandler.logout(request, response, SecurityContextHolder.getContext().getAuthentication());
        redirectStrategy.sendRedirect(request, response, redirectTarget((OidcLogoutAuthenticationToken) authentication));
    }

    private String redirectTarget(OidcLogoutAuthenticationToken authentication) {
        if (!StringUtils.hasText(authentication.getPostLogoutRedirectUri())) {
            return "/";
        }
        if (!StringUtils.hasText(authentication.getState())) {
            return authentication.getPostLogoutRedirectUri();
        }
        return UriComponentsBuilder.fromUriString(authentication.getPostLogoutRedirectUri())
                .queryParam(OAuth2ParameterNames.STATE, authentication.getState())
                .toUriString();
    }
}
