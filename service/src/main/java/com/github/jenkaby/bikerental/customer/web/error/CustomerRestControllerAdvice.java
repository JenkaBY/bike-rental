package com.github.jenkaby.bikerental.customer.web.error;

import com.github.jenkaby.bikerental.customer.domain.exception.DuplicatePhoneException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice(basePackages = "com.github.jenkaby.bikerental.customer")
@Order(Ordered.LOWEST_PRECEDENCE - 1)
public class CustomerRestControllerAdvice {

    @ExceptionHandler(DuplicatePhoneException.class)
    public ResponseEntity<String> handleDuplicatePhone(DuplicatePhoneException ex) {
        log.warn("Attempt to create customer with duplicate phone: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }
}
