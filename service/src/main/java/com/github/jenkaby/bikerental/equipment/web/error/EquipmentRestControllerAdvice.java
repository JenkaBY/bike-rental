package com.github.jenkaby.bikerental.equipment.web.error;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice(basePackages = "com.github.jenkaby.bikerental.equipment")
@Order(Ordered.LOWEST_PRECEDENCE - 1)
public class EquipmentRestControllerAdvice {
    // Add exception handlers as needed
}
