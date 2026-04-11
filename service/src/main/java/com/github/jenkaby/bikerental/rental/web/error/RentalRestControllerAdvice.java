package com.github.jenkaby.bikerental.rental.web.error;

import com.github.jenkaby.bikerental.finance.domain.exception.InsufficientBalanceException;
import com.github.jenkaby.bikerental.rental.domain.exception.*;
import com.github.jenkaby.bikerental.shared.web.advice.ErrorCodes;
import com.github.jenkaby.bikerental.shared.web.advice.ProblemDetailField;
import com.github.jenkaby.bikerental.tariff.SuitableTariffNotFoundException;
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

@Slf4j
@RestControllerAdvice(basePackages = "com.github.jenkaby.bikerental.rental")
@Order(Ordered.LOWEST_PRECEDENCE - 1)
public class RentalRestControllerAdvice {

    @ExceptionHandler(InvalidRentalStatusException.class)
    public ResponseEntity<ProblemDetail> handleInvalidRentalStatus(InvalidRentalStatusException ex) {
        var correlationId = resolveCorrelationId();
        log.warn("[correlationId={}] Invalid rental status: {}", correlationId, ex.getMessage());
        var problem = ProblemDetail.forStatus(HttpStatus.UNPROCESSABLE_CONTENT);
        problem.setTitle("Invalid rental status");
        problem.setDetail(ex.getMessage());
        problem.setProperty(ProblemDetailField.CORRELATION_ID, correlationId);
        problem.setProperty(ProblemDetailField.ERROR_CODE, ex.getErrorCode());
        problem.setProperty(ProblemDetailField.PARAMS, ex.getDetails());
        return ResponseEntity.of(problem).build();
    }

    @ExceptionHandler(RentalNotReadyForActivationException.class)
    public ResponseEntity<ProblemDetail> handleRentalNotReadyForActivation(RentalNotReadyForActivationException ex) {
        var correlationId = resolveCorrelationId();
        log.warn("[correlationId={}] Rental not ready for activation: {}", correlationId, ex.getMessage());
        var problem = ProblemDetail.forStatus(HttpStatus.UNPROCESSABLE_CONTENT);
        problem.setTitle("Rental not ready for activation");
        problem.setProperty(ProblemDetailField.CORRELATION_ID, correlationId);
        problem.setProperty(ProblemDetailField.ERROR_CODE, ex.getErrorCode());
        problem.setProperty(ProblemDetailField.PARAMS, ex.getDetails());
        return ResponseEntity.of(problem).build();
    }

    @ExceptionHandler(SuitableTariffNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleSuitableTariffNotFound(SuitableTariffNotFoundException ex) {
        var correlationId = resolveCorrelationId();
        log.warn("[correlationId={}] Suitable tariff not found: {}", correlationId, ex.getMessage());
        var problem = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        problem.setTitle("Suitable tariff not found");
        problem.setDetail(ex.getMessage());
        problem.setProperty(ProblemDetailField.CORRELATION_ID, correlationId);
        problem.setProperty(ProblemDetailField.ERROR_CODE, ex.getErrorCode());
        problem.setProperty(ProblemDetailField.PARAMS, ex.getDetails());
        return ResponseEntity.of(problem).build();
    }

    @ExceptionHandler(InvalidRentalUpdateException.class)
    public ResponseEntity<ProblemDetail> handleInvalidRentalUpdate(InvalidRentalUpdateException ex) {
        var correlationId = resolveCorrelationId();
        log.warn("[correlationId={}] Invalid rental update: {}", correlationId, ex.getMessage());
        var problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setTitle("Invalid rental update");
        problem.setDetail(ex.getMessage());
        problem.setProperty(ProblemDetailField.CORRELATION_ID, correlationId);
        problem.setProperty(ProblemDetailField.ERROR_CODE, ex.getErrorCode());
        return ResponseEntity.of(problem).build();
    }

    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<ProblemDetail> handleInsufficientBalance(InsufficientBalanceException ex) {
        var correlationId = resolveCorrelationId();
        log.warn("[correlationId={}] Insufficient balance for rental creation: {}", correlationId, ex.getMessage());
        var problem = ProblemDetail.forStatus(HttpStatus.UNPROCESSABLE_CONTENT);
        problem.setTitle("Insufficient funds");
        problem.setDetail(ex.getMessage());
        problem.setProperty(ProblemDetailField.CORRELATION_ID, correlationId);
        problem.setProperty(ProblemDetailField.ERROR_CODE, ErrorCodes.INSUFFICIENT_FUNDS);
        return ResponseEntity.of(problem).build();
    }

    @ExceptionHandler(HoldRequiredException.class)
    public ResponseEntity<ProblemDetail> handleHoldRequired(HoldRequiredException ex) {
        var correlationId = resolveCorrelationId();
        log.warn("[correlationId={}] Hold required for rental {}: {}", correlationId, ex.getDetails().rentalId(), ex.getMessage());
        var problem = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        problem.setTitle("Hold required");
        problem.setDetail(ex.getMessage());
        problem.setProperty(ProblemDetailField.CORRELATION_ID, correlationId);
        problem.setProperty(ProblemDetailField.ERROR_CODE, ErrorCodes.HOLD_REQUIRED);
        return ResponseEntity.of(problem).build();
    }

    @ExceptionHandler(PrepaymentRequiredException.class)
    public ResponseEntity<ProblemDetail> handlePrepaymentRequired(PrepaymentRequiredException ex) {
        var correlationId = resolveCorrelationId();
        log.warn("[correlationId={}] Prepayment required for rental {}: {}", correlationId, ex.getDetails().rentalId(), ex.getMessage());
        var problem = ProblemDetail.forStatus(HttpStatus.UNPROCESSABLE_CONTENT);
        problem.setTitle("Prepayment required");
        problem.setDetail(ex.getMessage());
        problem.setProperty(ProblemDetailField.CORRELATION_ID, correlationId);
        problem.setProperty(ProblemDetailField.ERROR_CODE, ex.getErrorCode());
        return ResponseEntity.of(problem).build();
    }

    @ExceptionHandler(InsufficientPrepaymentException.class)
    public ResponseEntity<ProblemDetail> handleInsufficientPrepayment(InsufficientPrepaymentException ex) {
        var correlationId = resolveCorrelationId();
        log.warn("[correlationId={}] Insufficient prepayment for rental {}: {}", correlationId, ex.getDetails().rentalId(), ex.getMessage());
        var problem = ProblemDetail.forStatus(HttpStatus.UNPROCESSABLE_CONTENT);
        problem.setTitle("Insufficient prepayment");
        problem.setDetail(ex.getMessage());
        problem.setProperty(ProblemDetailField.CORRELATION_ID, correlationId);
        problem.setProperty(ProblemDetailField.ERROR_CODE, ex.getErrorCode());
        return ResponseEntity.of(problem).build();
    }

    @ExceptionHandler(InvalidRentalPlannedDurationException.class)
    public ResponseEntity<ProblemDetail> handleInsufficientPrepayment(InvalidRentalPlannedDurationException ex) {
        var correlationId = resolveCorrelationId();
        log.warn("[correlationId={}] Planned duration is missing for rental {}: {}", correlationId, ex.getDetails().rentalId(), ex.getMessage());
        var problem = ProblemDetail.forStatus(HttpStatus.UNPROCESSABLE_CONTENT);
        problem.setTitle("Invalid rental planned duration");
        problem.setDetail(ex.getMessage());
        problem.setProperty(ProblemDetailField.CORRELATION_ID, correlationId);
        problem.setProperty(ProblemDetailField.ERROR_CODE, ex.getErrorCode());
        return ResponseEntity.of(problem).build();
    }

    private String resolveCorrelationId() {
        String correlationId = MDC.get("correlationId");
        return correlationId != null ? correlationId : UUID.randomUUID().toString();
    }
}
