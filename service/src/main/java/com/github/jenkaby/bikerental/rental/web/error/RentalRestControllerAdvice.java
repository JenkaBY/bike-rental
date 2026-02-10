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

@Slf4j
@RestControllerAdvice(basePackages = "com.github.jenkaby.bikerental.rental")
@Order(Ordered.LOWEST_PRECEDENCE - 1)
public class RentalRestControllerAdvice {

    @ExceptionHandler(InvalidRentalStatusException.class)
    public ResponseEntity<ProblemDetail> handleInvalidRentalStatus(InvalidRentalStatusException ex) {
        log.warn("Invalid rental status: {}", ex.getMessage());
        var problem = ProblemDetail.forStatus(HttpStatus.UNPROCESSABLE_CONTENT);
        problem.setTitle("Invalid rental status");
        problem.setDetail(ex.getMessage());
        return ResponseEntity.of(problem).build();
    }

    @ExceptionHandler(RentalNotReadyForActivationException.class)
    public ResponseEntity<ProblemDetail> handleRentalNotReadyForActivation(RentalNotReadyForActivationException ex) {
        log.warn("Rental not ready for activation: {}", ex.getMessage());
        var problem = ProblemDetail.forStatus(HttpStatus.UNPROCESSABLE_CONTENT);
        problem.setTitle("Rental not ready for activation");
        problem.setDetail(ex.getMessage());
        return ResponseEntity.of(problem).build();
    }

    @ExceptionHandler(SuitableTariffNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleSuitableTariffNotFound(SuitableTariffNotFoundException ex) {
        log.warn("Suitable tariff not found: {}", ex.getMessage());
        var problem = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        problem.setTitle("Suitable tariff not found");
        problem.setDetail(ex.getMessage());
        return ResponseEntity.of(problem).build();
    }

    @ExceptionHandler(InvalidRentalUpdateException.class)
    public ResponseEntity<ProblemDetail> handleInvalidRentalUpdate(InvalidRentalUpdateException ex) {
        log.warn("Invalid rental update: {}", ex.getMessage());
        var problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setTitle("Invalid rental update");
        problem.setDetail(ex.getMessage());
        return ResponseEntity.of(problem).build();
    }

    @ExceptionHandler(PrepaymentRequiredException.class)
    public ResponseEntity<ProblemDetail> handlePrepaymentRequired(PrepaymentRequiredException ex) {
        log.warn("Prepayment required for rental {}: {}", ex.getRentalId(), ex.getMessage());
        var problem = ProblemDetail.forStatus(HttpStatus.UNPROCESSABLE_CONTENT);
        problem.setTitle("Prepayment required");
        problem.setDetail(ex.getMessage());
        return ResponseEntity.of(problem).build();
    }

    @ExceptionHandler(InsufficientPrepaymentException.class)
    public ResponseEntity<ProblemDetail> handleInsufficientPrepayment(InsufficientPrepaymentException ex) {
        log.warn("Insufficient prepayment for rental {}: {}", ex.getRentalId(), ex.getMessage());
        var problem = ProblemDetail.forStatus(HttpStatus.UNPROCESSABLE_CONTENT);
        problem.setTitle("Insufficient prepayment");
        problem.setDetail(ex.getMessage());
        return ResponseEntity.of(problem).build();
    }
}
