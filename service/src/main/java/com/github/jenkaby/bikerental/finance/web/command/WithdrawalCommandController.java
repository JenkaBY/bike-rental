package com.github.jenkaby.bikerental.finance.web.command;

import com.github.jenkaby.bikerental.finance.application.usecase.RecordWithdrawalUseCase;
import com.github.jenkaby.bikerental.finance.web.command.dto.RecordWithdrawalRequest;
import com.github.jenkaby.bikerental.finance.web.command.dto.TransactionResponse;
import com.github.jenkaby.bikerental.finance.web.command.mapper.WithdrawalCommandMapper;
import com.github.jenkaby.bikerental.shared.config.OpenApiConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping(path = "/api/finance/withdrawals", produces = {MediaType.APPLICATION_JSON_VALUE})
@Tag(name = OpenApiConfig.Tags.FINANCE)
@Slf4j
public class WithdrawalCommandController {

    private final RecordWithdrawalUseCase recordWithdrawalUseCase;
    private final WithdrawalCommandMapper mapper;

    public WithdrawalCommandController(RecordWithdrawalUseCase recordWithdrawalUseCase,
                                       WithdrawalCommandMapper mapper) {
        this.recordWithdrawalUseCase = recordWithdrawalUseCase;
        this.mapper = mapper;
    }

    @Operation(summary = "Record a fund withdrawal for a customer")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Withdrawal recorded",
                    content = @Content(schema = @Schema(implementation = TransactionResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Customer finance account not found",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "422", description = "Insufficient available balance",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping
    public ResponseEntity<TransactionResponse> recordWithdrawal(@Valid @RequestBody RecordWithdrawalRequest request) {
        log.info("[POST] Record withdrawal request idempotencyKey={} customerId={} amount={}",
                request.idempotencyKey(), request.customerId(), request.amount());

        var command = mapper.toCommand(request);
        var result = recordWithdrawalUseCase.execute(command);

        log.info("[POST] Withdrawal recorded transactionId={} idempotencyKey={}", result.transactionId(), request.idempotencyKey());

        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(result));
    }
}
