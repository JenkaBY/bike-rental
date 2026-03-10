package com.github.jenkaby.bikerental.equipment.web.error;

import com.github.jenkaby.bikerental.equipment.domain.exception.InvalidStatusTransitionException;
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
@RestControllerAdvice(basePackages = "com.github.jenkaby.bikerental.equipment")
@Order(Ordered.LOWEST_PRECEDENCE - 1)
public class EquipmentRestControllerAdvice {

    @ExceptionHandler(InvalidStatusTransitionException.class)
    public ResponseEntity<ProblemDetail> handleInvalidStatusTransition(InvalidStatusTransitionException ex) {
        var errorId = UUID.randomUUID();
        log.warn("[errorId={}] Attempt to change status for Equipment {}: {}", errorId, ex.getId(), ex.getMessage());

        var problem = ProblemDetail.forStatus(HttpStatus.UNPROCESSABLE_CONTENT);

        problem.setTitle("Invalid status transition");
        problem.setDetail(ex.getMessage());
        problem.setProperty("errorId", errorId);
        return ResponseEntity.of(problem)
                .build();
    }
}
