package com.github.jenkaby.bikerental.customer.web.error;

import com.github.jenkaby.bikerental.customer.domain.exception.DuplicatePhoneException;
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
@RestControllerAdvice(basePackages = "com.github.jenkaby.bikerental.customer")
@Order(Ordered.LOWEST_PRECEDENCE - 1)
@RequiredArgsConstructor
public class CustomerRestControllerAdvice {

    private final CorrelationIdProvider correlationIdProvider;

    @ExceptionHandler(DuplicatePhoneException.class)
    public ResponseEntity<ProblemDetail> handleDuplicatePhone(DuplicatePhoneException ex) {
        var correlationId = correlationIdProvider.resolve();
        log.warn("[correlationId={}] Attempt to create customer with duplicate phone: {}", correlationId, ex.getMessage());
        var problem = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        problem.setTitle("Duplicate phone number");
        problem.setDetail(ex.getMessage());
        problem.setProperty(ProblemDetailField.CORRELATION_ID, correlationId);
        problem.setProperty(ProblemDetailField.ERROR_CODE, ex.getErrorCode());
        problem.setProperty(ProblemDetailField.PARAMS, ex.getDetails());
        return ResponseEntity.of(problem)
                .build();
    }
}
