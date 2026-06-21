package com.github.jenkaby.bikerental.tariff.web.error;

import com.github.jenkaby.bikerental.shared.web.advice.CorrelationIdProvider;
import com.github.jenkaby.bikerental.shared.web.advice.ProblemDetailField;
import com.github.jenkaby.bikerental.tariff.InvalidSpecialTariffTypeException;
import com.github.jenkaby.bikerental.tariff.SuitableTariffNotFoundException;
import com.github.jenkaby.bikerental.tariff.domain.exception.InvalidSpecialPriceException;
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
@RestControllerAdvice(basePackages = "com.github.jenkaby.bikerental.tariff")
@Order(Ordered.LOWEST_PRECEDENCE - 1)
@RequiredArgsConstructor
public class TariffRestControllerAdvice {

    private final CorrelationIdProvider correlationIdProvider;

    @ExceptionHandler(SuitableTariffNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleSuitableTariffNotFound(SuitableTariffNotFoundException ex) {
        var correlationId = correlationIdProvider.resolve();
        log.warn("[correlationId={}] Suitable tariff not found: {}", correlationId, ex.getMessage());
        var problem = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        problem.setTitle("Resource not found");
        problem.setDetail("Suitable tariff not found");
        problem.setProperty(ProblemDetailField.CORRELATION_ID, correlationId);
        problem.setProperty(ProblemDetailField.ERROR_CODE, ex.getErrorCode());
        problem.setProperty(ProblemDetailField.PARAMS, ex.getDetails());
        return ResponseEntity.of(problem).build();
    }

    @ExceptionHandler(InvalidTariffPricingException.class)
    public ResponseEntity<ProblemDetail> handleInvalidTariffV2Pricing(InvalidTariffPricingException ex) {
        var correlationId = correlationIdProvider.resolve();
        log.warn("[correlationId={}] Invalid tariff V2 pricing: {}", correlationId, ex.getMessage());
        var problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setTitle("Validation error");
        problem.setDetail(ex.getMessage());
        problem.setProperty(ProblemDetailField.CORRELATION_ID, correlationId);
        problem.setProperty(ProblemDetailField.ERROR_CODE, ex.getErrorCode());
        return ResponseEntity.badRequest().body(problem);
    }

    @ExceptionHandler(InvalidSpecialTariffTypeException.class)
    public ResponseEntity<ProblemDetail> handleInvalidSpecialTariffType(InvalidSpecialTariffTypeException ex) {
        var correlationId = correlationIdProvider.resolve();
        log.warn("[correlationId={}] Invalid special tariff type: {}", correlationId, ex.getMessage());
        var problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setTitle("Invalid special tariff");
        problem.setDetail(ex.getMessage());
        problem.setProperty(ProblemDetailField.CORRELATION_ID, correlationId);
        problem.setProperty(ProblemDetailField.ERROR_CODE, ex.getErrorCode());
        problem.setProperty(ProblemDetailField.PARAMS, ex.getDetails());
        return ResponseEntity.badRequest().body(problem);
    }

    @ExceptionHandler(InvalidSpecialPriceException.class)
    public ResponseEntity<ProblemDetail> handleInvalidSpecialPrice(InvalidSpecialPriceException ex) {
        var correlationId = correlationIdProvider.resolve();
        log.warn("[correlationId={}] Invalid special price: {}", correlationId, ex.getMessage());
        var problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setTitle("Invalid special price");
        problem.setDetail(ex.getMessage());
        problem.setProperty(ProblemDetailField.CORRELATION_ID, correlationId);
        problem.setProperty(ProblemDetailField.ERROR_CODE, ex.getErrorCode());
        return ResponseEntity.badRequest().body(problem);
    }
}
