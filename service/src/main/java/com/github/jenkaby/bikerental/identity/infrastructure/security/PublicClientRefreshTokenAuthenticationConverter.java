package com.github.jenkaby.bikerental.identity.infrastructure.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

final class PublicClientRefreshTokenAuthenticationConverter implements AuthenticationConverter {

    @Override
    public Authentication convert(HttpServletRequest request) {
        if (!isPublicClientRefreshTokenRequest(request)) {
            return null;
        }

        var clientIdValues = request.getParameterValues(OAuth2ParameterNames.CLIENT_ID);
        if (clientIdValues == null || clientIdValues.length != 1 || !StringUtils.hasText(clientIdValues[0])) {
            throw new OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_REQUEST);
        }

        Map<String, Object> additionalParameters = new HashMap<>();
        request.getParameterMap().forEach((name, values) -> {
            if (!OAuth2ParameterNames.CLIENT_ID.equals(name) && values.length > 0) {
                additionalParameters.put(name, values.length == 1 ? values[0] : values.clone());
            }
        });

        return new OAuth2ClientAuthenticationToken(clientIdValues[0], ClientAuthenticationMethod.NONE, null,
                additionalParameters);
    }

    private static boolean isPublicClientRefreshTokenRequest(HttpServletRequest request) {
        return AuthorizationGrantType.REFRESH_TOKEN.getValue().equals(request.getParameter(OAuth2ParameterNames.GRANT_TYPE))
                && request.getHeader(HttpHeaders.AUTHORIZATION) == null
                && !StringUtils.hasText(request.getParameter(OAuth2ParameterNames.CLIENT_SECRET))
                && !StringUtils.hasText(request.getParameter(OAuth2ParameterNames.CLIENT_ASSERTION));
    }
}
