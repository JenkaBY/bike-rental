package com.github.jenkaby.bikerental.finance.web.query;

import com.github.jenkaby.bikerental.finance.application.usecase.GetPaymentByIdUseCase;
import com.github.jenkaby.bikerental.finance.application.usecase.GetPaymentsByRentalIdUseCase;
import com.github.jenkaby.bikerental.finance.web.query.dto.PaymentResponse;
import com.github.jenkaby.bikerental.finance.web.query.mapper.PaymentQueryMapper;
import com.github.jenkaby.bikerental.shared.web.support.Id;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Validated
@RestController
@RequestMapping("/api/payments")
public class PaymentQueryController {

    private final GetPaymentByIdUseCase getByIdUseCase;
    private final GetPaymentsByRentalIdUseCase getByRentalUseCase;
    private final PaymentQueryMapper mapper;

    public PaymentQueryController(GetPaymentByIdUseCase getByIdUseCase, GetPaymentsByRentalIdUseCase getByRentalUseCase, PaymentQueryMapper mapper) {
        this.getByIdUseCase = getByIdUseCase;
        this.getByRentalUseCase = getByRentalUseCase;
        this.mapper = mapper;
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> getById(@PathVariable("id") UUID id) {
        var payment = getByIdUseCase.execute(id);
        return ResponseEntity.ok(mapper.toResponse(payment));
    }

    @GetMapping("/by-rental/{rentalId}")
    public ResponseEntity<List<PaymentResponse>> getByRental(@PathVariable(name = "rentalId") @Id Long rentalId) {
        var payments = getByRentalUseCase.execute(rentalId);
        var response = payments.stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }
}
