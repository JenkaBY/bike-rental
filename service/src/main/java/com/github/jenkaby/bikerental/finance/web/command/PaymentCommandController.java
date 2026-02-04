package com.github.jenkaby.bikerental.finance.web.command;

import com.github.jenkaby.bikerental.finance.application.usecase.RecordPaymentUseCase;
import com.github.jenkaby.bikerental.finance.web.command.dto.RecordPaymentRequest;
import com.github.jenkaby.bikerental.finance.web.command.dto.RecordPaymentResponse;
import com.github.jenkaby.bikerental.finance.web.command.mapper.PaymentCommandMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/payments")
public class PaymentCommandController {

    private final RecordPaymentUseCase recordPaymentUseCase;
    private final PaymentCommandMapper mapper;

    public PaymentCommandController(RecordPaymentUseCase recordPaymentUseCase, PaymentCommandMapper mapper) {
        this.recordPaymentUseCase = recordPaymentUseCase;
        this.mapper = mapper;
    }

    @PostMapping
    public ResponseEntity<RecordPaymentResponse> recordPayment(@Valid @RequestBody RecordPaymentRequest request) {
        var command = mapper.toCommand(request);
        var payment = recordPaymentUseCase.execute(command);
        var response = mapper.toRecordPaymentResponse(payment);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
