package com.github.jenkaby.bikerental.rental.web.error;

import com.github.jenkaby.bikerental.rental.domain.exception.*;
import com.github.jenkaby.bikerental.tariff.SuitableTariffNotFoundException;
import lombok.extern.slf4j.Slf4j;
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
        var errorId = UUID.randomUUID();
        log.warn("[errorId={}] Invalid rental status: {}", errorId, ex.getMessage());
        var problem = ProblemDetail.forStatus(HttpStatus.UNPROCESSABLE_CONTENT);
        problem.setTitle("Invalid rental status");
        problem.setDetail(ex.getMessage());
        problem.setProperty("errorId", errorId);
        return ResponseEntity.of(problem).build();
    }

    @ExceptionHandler(RentalNotReadyForActivationException.class)
    public ResponseEntity<ProblemDetail> handleRentalNotReadyForActivation(RentalNotReadyForActivationException ex) {
        var errorId = UUID.randomUUID();
        log.warn("[errorId={}] Rental not ready for activation: {}", errorId, ex.getMessage());
        var problem = ProblemDetail.forStatus(HttpStatus.UNPROCESSABLE_CONTENT);
        problem.setTitle("Rental not ready for activation");
        problem.setDetail(ex.getMessage());
        problem.setProperty("errorId", errorId);
        return ResponseEntity.of(problem).build();
    }

    @ExceptionHandler(SuitableTariffNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleSuitableTariffNotFound(SuitableTariffNotFoundException ex) {
        var errorId = UUID.randomUUID();
        log.warn("[errorId={}] Suitable tariff not found: {}", errorId, ex.getMessage());
        var problem = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        problem.setTitle("Suitable tariff not found");
        problem.setDetail(ex.getMessage());
        problem.setProperty("errorId", errorId);
        return ResponseEntity.of(problem).build();
    }

    @ExceptionHandler(InvalidRentalUpdateException.class)
    public ResponseEntity<ProblemDetail> handleInvalidRentalUpdate(InvalidRentalUpdateException ex) {
        var errorId = UUID.randomUUID();
        log.warn("[errorId={}] Invalid rental update: {}", errorId, ex.getMessage());
        var problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setTitle("Invalid rental update");
        problem.setDetail(ex.getMessage());
        problem.setProperty("errorId", errorId);
        return ResponseEntity.of(problem).build();
    }

    @ExceptionHandler(PrepaymentRequiredException.class)
    public ResponseEntity<ProblemDetail> handlePrepaymentRequired(PrepaymentRequiredException ex) {
        var errorId = UUID.randomUUID();
        log.warn("[errorId={}] Prepayment required for rental {}: {}", errorId, ex.getRentalId(), ex.getMessage());
        var problem = ProblemDetail.forStatus(HttpStatus.UNPROCESSABLE_CONTENT);
        problem.setTitle("Prepayment required");
        problem.setDetail(ex.getMessage());
        problem.setProperty("errorId", errorId);
        return ResponseEntity.of(problem).build();
    }

    @ExceptionHandler(InsufficientPrepaymentException.class)
    public ResponseEntity<ProblemDetail> handleInsufficientPrepayment(InsufficientPrepaymentException ex) {
        var errorId = UUID.randomUUID();
        log.warn("[errorId={}] Insufficient prepayment for rental {}: {}", errorId, ex.getRentalId(), ex.getMessage());
        var problem = ProblemDetail.forStatus(HttpStatus.UNPROCESSABLE_CONTENT);
        problem.setTitle("Insufficient prepayment");
        problem.setDetail(ex.getMessage());
        problem.setProperty("errorId", errorId);
        return ResponseEntity.of(problem).build();
    }
}
