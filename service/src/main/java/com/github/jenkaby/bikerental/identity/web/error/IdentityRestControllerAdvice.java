package com.github.jenkaby.bikerental.identity.web.error;

import com.github.jenkaby.bikerental.identity.domain.exception.InvalidCurrentPasswordException;
import com.github.jenkaby.bikerental.identity.domain.exception.PasswordPolicyViolationException;
import com.github.jenkaby.bikerental.shared.web.advice.CorrelationIdProvider;
import com.github.jenkaby.bikerental.shared.web.advice.ProblemDetailField;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice(basePackages = "com.github.jenkaby.bikerental.identity")
@Order(Ordered.LOWEST_PRECEDENCE - 1)
@RequiredArgsConstructor
public class IdentityRestControllerAdvice {

    private final CorrelationIdProvider correlationIdProvider;

    @ExceptionHandler(PasswordPolicyViolationException.class)
    public ResponseEntity<ProblemDetail> handlePasswordPolicyViolation(PasswordPolicyViolationException ex) {
        var correlationId = correlationIdProvider.resolve();
        log.warn("[correlationId={}] Password policy violation: {}", correlationId, ex.getMessage());
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_CONTENT, ex.getMessage());
        problem.setTitle("Password policy violation");
        problem.setProperty(ProblemDetailField.CORRELATION_ID, correlationId);
        problem.setProperty(ProblemDetailField.ERROR_CODE, ex.getErrorCode());
        return ResponseEntity.of(problem).build();
    }

    @ExceptionHandler(InvalidCurrentPasswordException.class)
    public ResponseEntity<ProblemDetail> handleInvalidCurrentPassword(InvalidCurrentPasswordException ex) {
        var correlationId = correlationIdProvider.resolve();
        log.warn("[correlationId={}] Invalid current password supplied", correlationId);
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_CONTENT, ex.getMessage());
        problem.setTitle("Invalid current password");
        problem.setProperty(ProblemDetailField.CORRELATION_ID, correlationId);
        problem.setProperty(ProblemDetailField.ERROR_CODE, ex.getErrorCode());
        return ResponseEntity.of(problem).build();
    }
}
