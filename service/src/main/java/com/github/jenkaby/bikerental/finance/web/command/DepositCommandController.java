package com.github.jenkaby.bikerental.finance.web.command;

import com.github.jenkaby.bikerental.finance.application.usecase.RecordDepositUseCase;
import com.github.jenkaby.bikerental.finance.web.command.dto.RecordDepositRequest;
import com.github.jenkaby.bikerental.finance.web.command.dto.RecordDepositResponse;
import com.github.jenkaby.bikerental.finance.web.command.mapper.DepositCommandMapper;
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
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/finance/deposits")
@Tag(name = OpenApiConfig.Tags.FINANCE)
@Slf4j
public class DepositCommandController {

    private final RecordDepositUseCase recordDepositUseCase;
    private final DepositCommandMapper mapper;

    public DepositCommandController(RecordDepositUseCase recordDepositUseCase,
                                    DepositCommandMapper mapper) {
        this.recordDepositUseCase = recordDepositUseCase;
        this.mapper = mapper;
    }

    @Operation(summary = "Record a fund deposit for a customer")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Deposit recorded",
                    content = @Content(schema = @Schema(implementation = RecordDepositResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Customer finance account not found",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping
    public ResponseEntity<RecordDepositResponse> recordDeposit(@Valid @RequestBody RecordDepositRequest request) {
                log.info("[POST] Record deposit request idempotencyKey={} customerId={} amount={}",
                                request.idempotencyKey(), request.customerId(), request.amount());

                var command = mapper.toCommand(request);
                var result = recordDepositUseCase.execute(command);

                log.info("[POST] Deposit recorded transactionId={} idempotencyKey={}", result.transactionId(), request.idempotencyKey());

                return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(result));
    }
}
