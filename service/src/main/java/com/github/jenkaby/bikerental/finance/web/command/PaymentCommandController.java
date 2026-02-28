package com.github.jenkaby.bikerental.finance.web.command;

import com.github.jenkaby.bikerental.finance.application.usecase.RecordPaymentUseCase;
import com.github.jenkaby.bikerental.finance.web.command.dto.RecordPaymentRequest;
import com.github.jenkaby.bikerental.finance.web.command.dto.RecordPaymentResponse;
import com.github.jenkaby.bikerental.finance.web.command.mapper.PaymentCommandMapper;
import com.github.jenkaby.bikerental.shared.config.OpenApiConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/payments")
@Tag(name = OpenApiConfig.Tags.FINANCE)
public class PaymentCommandController {

    private final RecordPaymentUseCase recordPaymentUseCase;
    private final PaymentCommandMapper mapper;

    public PaymentCommandController(RecordPaymentUseCase recordPaymentUseCase, PaymentCommandMapper mapper) {
        this.recordPaymentUseCase = recordPaymentUseCase;
        this.mapper = mapper;
    }

    @PostMapping
    @Operation(summary = "Record payment", description = "Records a payment against a rental")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Payment recorded",
                    content = @Content(schema = @Schema(implementation = RecordPaymentResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Rental not found",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<RecordPaymentResponse> recordPayment(@Valid @RequestBody RecordPaymentRequest request) {
        var command = mapper.toCommand(request);
        var payment = recordPaymentUseCase.execute(command);
        var response = mapper.toRecordPaymentResponse(payment);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
