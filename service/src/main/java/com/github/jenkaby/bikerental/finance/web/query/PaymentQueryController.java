package com.github.jenkaby.bikerental.finance.web.query;

import com.github.jenkaby.bikerental.finance.application.usecase.GetPaymentByIdUseCase;
import com.github.jenkaby.bikerental.finance.application.usecase.GetPaymentsByRentalIdUseCase;
import com.github.jenkaby.bikerental.finance.web.query.dto.PaymentResponse;
import com.github.jenkaby.bikerental.finance.web.query.mapper.PaymentQueryMapper;
import com.github.jenkaby.bikerental.shared.config.OpenApiConfig;
import com.github.jenkaby.bikerental.shared.web.support.Id;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ProblemDetail;
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
@Tag(name = OpenApiConfig.Tags.FINANCE)
@Deprecated
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
    @Operation(summary = "Get payment by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Payment found",
                    content = @Content(schema = @Schema(implementation = PaymentResponse.class))),
            @ApiResponse(responseCode = "404", description = "Payment not found",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<PaymentResponse> getById(
            @Parameter(description = "Payment UUID") @PathVariable("id") UUID id) {
        var payment = getByIdUseCase.execute(id);
        return ResponseEntity.ok(mapper.toResponse(payment));
    }

    @GetMapping("/by-rental/{rentalId}")
    @Operation(summary = "Get payments by rental ID", description = "Returns all payments associated with a rental")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Payments returned",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = PaymentResponse.class)))),
            @ApiResponse(responseCode = "400", description = "Invalid rental ID",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<List<PaymentResponse>> getByRental(
            @Parameter(description = "Rental ID", example = "42") @PathVariable(name = "rentalId") @Id Long rentalId) {
        var payments = getByRentalUseCase.execute(rentalId);
        var response = payments.stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }
}
