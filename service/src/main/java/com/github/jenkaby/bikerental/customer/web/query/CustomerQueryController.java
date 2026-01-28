package com.github.jenkaby.bikerental.customer.web.query;

import com.github.jenkaby.bikerental.customer.application.usecase.CustomerQueryUseCase;
import com.github.jenkaby.bikerental.customer.web.query.dto.CustomerSearchResponse;
import com.github.jenkaby.bikerental.customer.web.query.mapper.CustomerQueryMapper;
import jakarta.validation.constraints.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
@Validated
@Slf4j
class CustomerQueryController {

    private final CustomerQueryUseCase customerQueryUseCase;
    private final CustomerQueryMapper mapper;

    CustomerQueryController(CustomerQueryUseCase customerQueryUseCase, CustomerQueryMapper mapper) {
        this.customerQueryUseCase = customerQueryUseCase;
        this.mapper = mapper;
    }

    @GetMapping
    public ResponseEntity<List<CustomerSearchResponse>> searchByPhone(
            @RequestParam("phone")
            @Pattern(regexp = "^\\d{4,11}$", message = "Phone search must be 4 to 11 digits")
            String phone) {
        var results = customerQueryUseCase.searchByPhone(phone);
        return ResponseEntity.ok(mapper.toSearchResponses(results));
    }
}
