package com.github.jenkaby.bikerental.finance.web.query;

import com.github.jenkaby.bikerental.finance.application.usecase.GetCustomerAccountBalancesUseCase;
import com.github.jenkaby.bikerental.finance.web.query.dto.CustomerAccountBalancesResponse;
import com.github.jenkaby.bikerental.finance.web.query.mapper.AccountQueryMapper;
import com.github.jenkaby.bikerental.shared.config.OpenApiConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Validated
@RestController
@RequestMapping("/api/finance/customers")
@Tag(name = OpenApiConfig.Tags.FINANCE)
@Slf4j
public class AccountQueryController {

    private final GetCustomerAccountBalancesUseCase getCustomerAccountBalancesUseCase;
    private final AccountQueryMapper mapper;

    public AccountQueryController(GetCustomerAccountBalancesUseCase getCustomerAccountBalancesUseCase,
                                  AccountQueryMapper mapper) {
        this.getCustomerAccountBalancesUseCase = getCustomerAccountBalancesUseCase;
        this.mapper = mapper;
    }

    @GetMapping("/{customerId}/balances")
    @Operation(summary = "Retrieve customer account balances")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Account balances retrieved",
                    content = @Content(schema = @Schema(implementation = CustomerAccountBalancesResponse.class))),
            @ApiResponse(responseCode = "404", description = "Customer finance account not found",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "400", description = "Invalid customer UUID format",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<CustomerAccountBalancesResponse> getBalances(
            @Parameter(description = "Customer UUID") @PathVariable("customerId") UUID customerId) {
        log.info("[GET] Retrieve balances for customerId={}", customerId);
        var result = getCustomerAccountBalancesUseCase.execute(customerId);
        return ResponseEntity.ok(mapper.toResponse(result));
    }
}
