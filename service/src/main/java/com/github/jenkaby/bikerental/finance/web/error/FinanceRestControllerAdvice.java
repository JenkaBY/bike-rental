package com.github.jenkaby.bikerental.finance.web.error;

import com.github.jenkaby.bikerental.finance.domain.exception.InsufficientBalanceException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.UUID;

import static com.github.jenkaby.bikerental.shared.web.advice.ProblemDetailField.CORRELATION_ID;
import static com.github.jenkaby.bikerental.shared.web.advice.ProblemDetailField.ERROR_CODE;

@Slf4j
@RestControllerAdvice(basePackages = "com.github.jenkaby.bikerental.finance")
@Order(Ordered.LOWEST_PRECEDENCE - 1)
public class FinanceRestControllerAdvice {

    private String resolveCorrelationId() {
        String correlationId = MDC.get("correlationId");
        return correlationId != null ? correlationId : UUID.randomUUID().toString();
    }

    @ExceptionHandler(InsufficientBalanceException.class)
    ResponseEntity<ProblemDetail> handleInsufficientBalance(InsufficientBalanceException ex) {
        var correlationId = resolveCorrelationId();
        log.warn("[correlationId={}] Insufficient balance: {}", correlationId, ex.getMessage());
        var body = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
        body.setProperty(CORRELATION_ID, correlationId);
        body.setProperty(ERROR_CODE, ex.getErrorCode());
        return new ResponseEntity<>(body, HttpStatus.UNPROCESSABLE_ENTITY);
    }
}
