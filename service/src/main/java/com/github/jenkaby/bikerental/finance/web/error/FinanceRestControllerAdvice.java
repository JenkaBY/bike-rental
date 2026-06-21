package com.github.jenkaby.bikerental.finance.web.error;

import com.github.jenkaby.bikerental.shared.exception.InsufficientBalanceException;
import com.github.jenkaby.bikerental.shared.web.advice.CorrelationIdProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static com.github.jenkaby.bikerental.shared.web.advice.ProblemDetailField.CORRELATION_ID;
import static com.github.jenkaby.bikerental.shared.web.advice.ProblemDetailField.ERROR_CODE;
import static com.github.jenkaby.bikerental.shared.web.advice.ProblemDetailField.PARAMS;

@Slf4j
@RestControllerAdvice(basePackages = "com.github.jenkaby.bikerental.finance")
@Order(Ordered.LOWEST_PRECEDENCE - 1)
@RequiredArgsConstructor
public class FinanceRestControllerAdvice {

    private final CorrelationIdProvider correlationIdProvider;

    @ExceptionHandler(InsufficientBalanceException.class)
    ResponseEntity<ProblemDetail> handleInsufficientBalance(InsufficientBalanceException ex) {
        var correlationId = correlationIdProvider.resolve();
        log.warn("[correlationId={}] Insufficient balance: {}", correlationId, ex.getMessage());
        var body = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_CONTENT, ex.getMessage());
        body.setProperty(CORRELATION_ID, correlationId);
        body.setProperty(ERROR_CODE, ex.getErrorCode());
        body.setProperty(PARAMS, ex.getDetails());
        return new ResponseEntity<>(body, HttpStatus.UNPROCESSABLE_CONTENT);
    }
}
