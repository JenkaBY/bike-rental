package com.github.jenkaby.bikerental.customer.web.command;

import com.github.jenkaby.bikerental.customer.application.usecase.CreateCustomerUseCase;
import com.github.jenkaby.bikerental.customer.application.usecase.UpdateCustomerUseCase;
import com.github.jenkaby.bikerental.customer.domain.model.Customer;
import com.github.jenkaby.bikerental.customer.web.command.dto.CustomerRequest;
import com.github.jenkaby.bikerental.customer.web.command.mapper.CustomerCommandMapper;
import com.github.jenkaby.bikerental.customer.web.query.dto.CustomerResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/customers")
@Slf4j
class CustomerCommandController {

    private final CreateCustomerUseCase createCustomerUseCase;
    private final UpdateCustomerUseCase updateCustomerUseCase;
    private final CustomerCommandMapper mapper;

    CustomerCommandController(
            CreateCustomerUseCase createCustomerUseCase,
            UpdateCustomerUseCase updateCustomerUseCase,
            CustomerCommandMapper mapper) {
        this.createCustomerUseCase = createCustomerUseCase;
        this.updateCustomerUseCase = updateCustomerUseCase;
        this.mapper = mapper;
    }

    @PostMapping
    public ResponseEntity<CustomerResponse> createCustomer(@Valid @RequestBody CustomerRequest request) {
        log.info("[POST] Creating customer with phone: {}", request.phone());
        var command = mapper.toCreateCommand(request);
        Customer customer = createCustomerUseCase.execute(command);
        var response = mapper.toResponse(customer);
        log.info("[POST] Customer created successfully with id: {}", customer.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CustomerResponse> updateCustomer(@PathVariable("id") UUID id, @Valid @RequestBody CustomerRequest request) {
        log.info("[PUT] Updating customer with id: {}", id);
        var command = mapper.toUpdateCommand(id, request);
        Customer customer = updateCustomerUseCase.execute(command);
        var response = mapper.toResponse(customer);
        log.info("[PUT] Customer updated successfully with id: {}", customer.getId());
        return ResponseEntity.ok(response);
    }
}
