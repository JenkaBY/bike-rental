package com.github.jenkaby.bikerental.identity.infrastructure.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.http.converter.OAuth2ErrorHttpMessageConverter;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;

final class OAuth2ErrorAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final OAuth2ErrorHttpMessageConverter errorHttpResponseConverter = new OAuth2ErrorHttpMessageConverter();

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        var error = authException instanceof OAuth2AuthenticationException oauth2Exception
                ? oauth2Exception.getError()
                : new OAuth2Error(OAuth2ErrorCodes.INVALID_REQUEST, "Authentication required", null);
        var httpResponse = new ServletServerHttpResponse(response);
        httpResponse.setStatusCode(HttpStatus.UNAUTHORIZED);
        errorHttpResponseConverter.write(error, null, httpResponse);
    }
}
