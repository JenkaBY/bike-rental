package com.github.jenkaby.bikerental.identity.infrastructure.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.server.authorization.oidc.authentication.OidcLogoutAuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.util.StringUtils;

final class StatelessOidcLogoutAuthenticationConverter implements AuthenticationConverter {

    private static final AnonymousAuthenticationToken ANONYMOUS_PRINCIPAL = new AnonymousAuthenticationToken(
            "stateless-logout", "anonymous", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS"));

    @Override
    public Authentication convert(HttpServletRequest request) {
        var idTokenHint = request.getParameter("id_token_hint");
        if (!StringUtils.hasText(idTokenHint)) {
            return null;
        }
        return new OidcLogoutAuthenticationToken(
                idTokenHint,
                ANONYMOUS_PRINCIPAL,
                null,
                request.getParameter(OAuth2ParameterNames.CLIENT_ID),
                request.getParameter("post_logout_redirect_uri"),
                request.getParameter(OAuth2ParameterNames.STATE));
    }
}
