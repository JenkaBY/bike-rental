package com.github.jenkaby.bikerental.customer.web.command;

import com.github.jenkaby.bikerental.customer.application.usecase.CreateCustomerUseCase;
import com.github.jenkaby.bikerental.customer.domain.model.Customer;
import com.github.jenkaby.bikerental.customer.web.command.dto.CreateCustomerRequest;
import com.github.jenkaby.bikerental.customer.web.command.mapper.CustomerCommandMapper;
import com.github.jenkaby.bikerental.customer.web.query.dto.CustomerResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/customers")
@Slf4j
class CustomerCommandController {

    private final CreateCustomerUseCase createCustomerUseCase;
    private final CustomerCommandMapper mapper;

    CustomerCommandController(CreateCustomerUseCase createCustomerUseCase, CustomerCommandMapper mapper) {
        this.createCustomerUseCase = createCustomerUseCase;
        this.mapper = mapper;
    }

    @PostMapping
    public ResponseEntity<CustomerResponse> createCustomer(@Valid @RequestBody CreateCustomerRequest request) {
        var command = mapper.toCreateCommand(request);
        Customer customer = createCustomerUseCase.execute(command);
        var response = mapper.toResponse(customer);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

}
