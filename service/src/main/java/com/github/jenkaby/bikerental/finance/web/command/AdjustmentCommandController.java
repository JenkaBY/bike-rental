package com.github.jenkaby.bikerental.finance.web.command;

import com.github.jenkaby.bikerental.finance.application.usecase.ApplyAdjustmentUseCase;
import com.github.jenkaby.bikerental.finance.web.command.dto.AdjustmentRequest;
import com.github.jenkaby.bikerental.finance.web.command.dto.AdjustmentResponse;
import com.github.jenkaby.bikerental.finance.web.command.mapper.AdjustmentCommandMapper;
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
@RequestMapping("/api/finance/adjustments")
@Tag(name = OpenApiConfig.Tags.FINANCE)
@Slf4j
public class AdjustmentCommandController {

    private final ApplyAdjustmentUseCase applyAdjustmentUseCase;
    private final AdjustmentCommandMapper mapper;

    public AdjustmentCommandController(ApplyAdjustmentUseCase applyAdjustmentUseCase,
                                       AdjustmentCommandMapper mapper) {
        this.applyAdjustmentUseCase = applyAdjustmentUseCase;
        this.mapper = mapper;
    }

    @Operation(summary = "Apply a manual balance adjustment to a customer wallet")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Adjustment applied",
                    content = @Content(schema = @Schema(implementation = AdjustmentResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Customer finance account not found",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "422", description = "Insufficient wallet balance",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping
    public ResponseEntity<AdjustmentResponse> applyAdjustment(@Valid @RequestBody AdjustmentRequest request) {
        log.info("[POST] Apply adjustment request customerId={} amount={}", request.customerId(), request.amount());

        var command = mapper.toCommand(request);
        var result = applyAdjustmentUseCase.execute(command);

        log.info("[POST] Adjustment applied transactionId={} customerId={}", result.transactionId(), request.customerId());

        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(result));
    }
}
