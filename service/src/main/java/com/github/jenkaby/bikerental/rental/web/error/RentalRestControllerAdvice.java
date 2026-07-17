package com.github.jenkaby.bikerental.rental.web.error;

import com.github.jenkaby.bikerental.rental.domain.exception.*;
import com.github.jenkaby.bikerental.shared.exception.InsufficientBalanceException;
import com.github.jenkaby.bikerental.shared.web.advice.CorrelationIdProvider;
import com.github.jenkaby.bikerental.shared.web.advice.ErrorCodes;
import com.github.jenkaby.bikerental.shared.web.advice.ProblemDetailField;
import com.github.jenkaby.bikerental.tariff.InvalidSpecialTariffTypeException;
import com.github.jenkaby.bikerental.tariff.QuoteAlreadyConsumedException;
import com.github.jenkaby.bikerental.tariff.QuoteExpiredException;
import com.github.jenkaby.bikerental.tariff.QuoteNotFoundException;
import com.github.jenkaby.bikerental.tariff.SuitableTariffNotFoundException;
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
@RestControllerAdvice(basePackages = "com.github.jenkaby.bikerental.rental")
@Order(Ordered.LOWEST_PRECEDENCE - 1)
@RequiredArgsConstructor
public class RentalRestControllerAdvice {

    private final CorrelationIdProvider correlationIdProvider;

    @ExceptionHandler(InvalidRentalStatusException.class)
    public ResponseEntity<ProblemDetail> handleInvalidRentalStatus(InvalidRentalStatusException ex) {
        var correlationId = correlationIdProvider.resolve();
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
        var correlationId = correlationIdProvider.resolve();
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
        var correlationId = correlationIdProvider.resolve();
        log.warn("[correlationId={}] Suitable tariff not found: {}", correlationId, ex.getMessage());
        var problem = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        problem.setTitle("Suitable tariff not found");
        problem.setDetail(ex.getMessage());
        problem.setProperty(ProblemDetailField.CORRELATION_ID, correlationId);
        problem.setProperty(ProblemDetailField.ERROR_CODE, ex.getErrorCode());
        problem.setProperty(ProblemDetailField.PARAMS, ex.getDetails());
        return ResponseEntity.of(problem).build();
    }

    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<ProblemDetail> handleInsufficientBalance(InsufficientBalanceException ex) {
        var correlationId = correlationIdProvider.resolve();
        log.warn("[correlationId={}] Insufficient balance for rental creation: {}", correlationId, ex.getMessage());
        var problem = ProblemDetail.forStatus(HttpStatus.UNPROCESSABLE_CONTENT);
        problem.setTitle("Insufficient funds");
        problem.setDetail(ex.getMessage());
        problem.setProperty(ProblemDetailField.CORRELATION_ID, correlationId);
        problem.setProperty(ProblemDetailField.ERROR_CODE, ErrorCodes.INSUFFICIENT_FUNDS);
        problem.setProperty(ProblemDetailField.PARAMS, ex.getDetails());
        return ResponseEntity.of(problem).build();
    }

    @ExceptionHandler(HoldRequiredException.class)
    public ResponseEntity<ProblemDetail> handleHoldRequired(HoldRequiredException ex) {
        var correlationId = correlationIdProvider.resolve();
        log.warn("[correlationId={}] Hold required for rental {}: {}", correlationId, ex.getDetails().rentalId(), ex.getMessage());
        var problem = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        problem.setTitle("Hold required");
        problem.setDetail(ex.getMessage());
        problem.setProperty(ProblemDetailField.CORRELATION_ID, correlationId);
        problem.setProperty(ProblemDetailField.ERROR_CODE, ErrorCodes.HOLD_REQUIRED);
        return ResponseEntity.of(problem).build();
    }

    @ExceptionHandler(InvalidRentalPlannedDurationException.class)
    public ResponseEntity<ProblemDetail> handleInsufficientPrepayment(InvalidRentalPlannedDurationException ex) {
        var correlationId = correlationIdProvider.resolve();
        log.warn("[correlationId={}] Planned duration is missing for rental {}: {}", correlationId, ex.getDetails().rentalId(), ex.getMessage());
        var problem = ProblemDetail.forStatus(HttpStatus.UNPROCESSABLE_CONTENT);
        problem.setTitle("Invalid rental planned duration");
        problem.setDetail(ex.getMessage());
        problem.setProperty(ProblemDetailField.CORRELATION_ID, correlationId);
        problem.setProperty(ProblemDetailField.ERROR_CODE, ex.getErrorCode());
        return ResponseEntity.of(problem).build();
    }

    @ExceptionHandler(InvalidSpecialTariffTypeException.class)
    public ResponseEntity<ProblemDetail> handleInvalidSpecialTariffType(InvalidSpecialTariffTypeException ex) {
        var correlationId = correlationIdProvider.resolve();
        log.warn("[correlationId={}] Invalid special tariff type: {}", correlationId, ex.getMessage());
        var problem = ProblemDetail.forStatus(HttpStatus.UNPROCESSABLE_CONTENT);
        problem.setTitle("Invalid special tariff type");
        problem.setDetail(ex.getMessage());
        problem.setProperty(ProblemDetailField.CORRELATION_ID, correlationId);
        problem.setProperty(ProblemDetailField.ERROR_CODE, ex.getErrorCode());
        problem.setProperty(ProblemDetailField.PARAMS, ex.getDetails());
        return ResponseEntity.of(problem).build();
    }

    @ExceptionHandler(EquipmentOccupiedException.class)
    public ResponseEntity<ProblemDetail> handleEquipmentOccupied(EquipmentOccupiedException ex) {
        var correlationId = correlationIdProvider.resolve();
        log.warn("[correlationId={}] Equipment occupied: {}", correlationId, ex.getMessage());
        var problem = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        problem.setTitle("Equipment not available");
        problem.setDetail(ex.getMessage());
        problem.setProperty(ProblemDetailField.CORRELATION_ID, correlationId);
        problem.setProperty(ProblemDetailField.ERROR_CODE, ErrorCodes.EQUIPMENT_NOT_AVAILABLE);
        problem.setProperty(ProblemDetailField.PARAMS, ex.getDetails());
        return ResponseEntity.of(problem).build();
    }

    @ExceptionHandler(RentalWindowElapsedException.class)
    public ResponseEntity<ProblemDetail> handleRentalWindowElapsed(RentalWindowElapsedException ex) {
        var correlationId = correlationIdProvider.resolve();
        log.warn("[correlationId={}] Rental window elapsed: {}", correlationId, ex.getMessage());
        var problem = ProblemDetail.forStatus(HttpStatus.UNPROCESSABLE_CONTENT);
        problem.setTitle("Rental window elapsed");
        problem.setDetail(ex.getMessage());
        problem.setProperty(ProblemDetailField.CORRELATION_ID, correlationId);
        problem.setProperty(ProblemDetailField.ERROR_CODE, ex.getErrorCode());
        problem.setProperty(ProblemDetailField.PARAMS, ex.getDetails());
        return ResponseEntity.of(problem).build();
    }

    @ExceptionHandler(QuoteNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleQuoteNotFound(QuoteNotFoundException ex) {
        var correlationId = correlationIdProvider.resolve();
        log.warn("[correlationId={}] Cost quote not found: {}", correlationId, ex.getMessage());
        var problem = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        problem.setTitle("Cost quote not found");
        problem.setDetail(ex.getMessage());
        problem.setProperty(ProblemDetailField.CORRELATION_ID, correlationId);
        problem.setProperty(ProblemDetailField.ERROR_CODE, ex.getErrorCode());
        problem.setProperty(ProblemDetailField.PARAMS, ex.getDetails());
        return ResponseEntity.of(problem).build();
    }

    @ExceptionHandler(QuoteExpiredException.class)
    public ResponseEntity<ProblemDetail> handleQuoteExpired(QuoteExpiredException ex) {
        var correlationId = correlationIdProvider.resolve();
        log.warn("[correlationId={}] Cost quote expired: {}", correlationId, ex.getMessage());
        var problem = ProblemDetail.forStatus(HttpStatus.GONE);
        problem.setTitle("Cost quote expired");
        problem.setDetail(ex.getMessage());
        problem.setProperty(ProblemDetailField.CORRELATION_ID, correlationId);
        problem.setProperty(ProblemDetailField.ERROR_CODE, ex.getErrorCode());
        problem.setProperty(ProblemDetailField.PARAMS, ex.getDetails());
        return ResponseEntity.of(problem).build();
    }

    @ExceptionHandler(QuoteAlreadyConsumedException.class)
    public ResponseEntity<ProblemDetail> handleQuoteAlreadyConsumed(QuoteAlreadyConsumedException ex) {
        var correlationId = correlationIdProvider.resolve();
        log.warn("[correlationId={}] Cost quote already consumed: {}", correlationId, ex.getMessage());
        var problem = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        problem.setTitle("Cost quote already consumed");
        problem.setDetail(ex.getMessage());
        problem.setProperty(ProblemDetailField.CORRELATION_ID, correlationId);
        problem.setProperty(ProblemDetailField.ERROR_CODE, ex.getErrorCode());
        problem.setProperty(ProblemDetailField.PARAMS, ex.getDetails());
        return ResponseEntity.of(problem).build();
    }

    @ExceptionHandler(QuoteRentalMismatchException.class)
    public ResponseEntity<ProblemDetail> handleQuoteRentalMismatch(QuoteRentalMismatchException ex) {
        var correlationId = correlationIdProvider.resolve();
        log.warn("[correlationId={}] Cost quote inconsistent with rental: {}", correlationId, ex.getMessage());
        var problem = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        problem.setTitle("Cost quote inconsistent with rental");
        problem.setDetail(ex.getMessage());
        problem.setProperty(ProblemDetailField.CORRELATION_ID, correlationId);
        problem.setProperty(ProblemDetailField.ERROR_CODE, ex.getErrorCode());
        problem.setProperty(ProblemDetailField.PARAMS, ex.getDetails());
        return ResponseEntity.of(problem).build();
    }

    @ExceptionHandler(RentalCompletionFlowViolationException.class)
    public ResponseEntity<ProblemDetail> handleRentalCompletionFlowViolation(RentalCompletionFlowViolationException ex) {
        var correlationId = correlationIdProvider.resolve();
        log.warn("[correlationId={}] Rental completion flow violated: {}", correlationId, ex.getMessage());
        var problem = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        problem.setTitle("Rental completion flow violated");
        problem.setDetail(ex.getMessage());
        problem.setProperty(ProblemDetailField.CORRELATION_ID, correlationId);
        problem.setProperty(ProblemDetailField.ERROR_CODE, ex.getErrorCode());
        problem.setProperty(ProblemDetailField.PARAMS, ex.getDetails());
        return ResponseEntity.of(problem).build();
    }

    @ExceptionHandler(InvalidDateRangeException.class)
    public ResponseEntity<ProblemDetail> handleInvalidDateRange(InvalidDateRangeException ex) {
        var correlationId = correlationIdProvider.resolve();
        log.warn("[correlationId={}] Invalid date range: {}", correlationId, ex.getMessage());
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problem.setProperty(ProblemDetailField.CORRELATION_ID, correlationId);
        problem.setProperty(ProblemDetailField.ERROR_CODE, ErrorCodes.CONSTRAINT_VIOLATION);
        return ResponseEntity.of(problem).build();
    }
}
