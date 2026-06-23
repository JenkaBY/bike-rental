package com.github.jenkaby.bikerental.identity.infrastructure.security;

import com.github.jenkaby.bikerental.shared.web.advice.CorrelationIdProvider;
import com.github.jenkaby.bikerental.shared.web.advice.ErrorCodes;
import com.github.jenkaby.bikerental.shared.web.advice.ProblemDetailField;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

@Component
@RequiredArgsConstructor
class ProblemDetailAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final CorrelationIdProvider correlationIdProvider;
    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, "Authentication required");
        problem.setProperty(ProblemDetailField.CORRELATION_ID, correlationIdProvider.resolve());
        problem.setProperty(ProblemDetailField.ERROR_CODE, ErrorCodes.AUTHENTICATION_REQUIRED);
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), problem);
    }
}
