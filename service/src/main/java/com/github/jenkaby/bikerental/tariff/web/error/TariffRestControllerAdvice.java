package com.github.jenkaby.bikerental.tariff.web.error;

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
@RestControllerAdvice(basePackages = "com.github.jenkaby.bikerental.tariff")
@Order(Ordered.LOWEST_PRECEDENCE - 1)
public class TariffRestControllerAdvice {

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
}
