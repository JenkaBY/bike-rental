package com.github.jenkaby.bikerental.finance.web.query;

import com.github.jenkaby.bikerental.finance.application.usecase.GetCustomerAccountBalancesUseCase;
import com.github.jenkaby.bikerental.finance.application.usecase.GetTransactionHistoryUseCase;
import com.github.jenkaby.bikerental.finance.domain.model.TransactionHistoryFilter;
import com.github.jenkaby.bikerental.finance.web.query.dto.CustomerAccountBalancesResponse;
import com.github.jenkaby.bikerental.finance.web.query.dto.TransactionHistoryFilterParams;
import com.github.jenkaby.bikerental.finance.web.query.dto.TransactionResponse;
import com.github.jenkaby.bikerental.finance.web.query.mapper.AccountQueryMapper;
import com.github.jenkaby.bikerental.finance.web.query.mapper.TransactionHistoryQueryMapper;
import com.github.jenkaby.bikerental.shared.config.OpenApiConfig;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Page;
import com.github.jenkaby.bikerental.shared.domain.model.vo.PageRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Validated
@RestController
@RequestMapping("/api/finance/customers")
@Tag(name = OpenApiConfig.Tags.FINANCE)
@Slf4j
public class AccountQueryController {

    private final GetCustomerAccountBalancesUseCase getCustomerAccountBalancesUseCase;
    private final AccountQueryMapper mapper;
    private final GetTransactionHistoryUseCase getTransactionHistoryUseCase;
    private final TransactionHistoryQueryMapper transactionHistoryQueryMapper;

    public AccountQueryController(GetCustomerAccountBalancesUseCase getCustomerAccountBalancesUseCase,
                                  AccountQueryMapper mapper,
                                  GetTransactionHistoryUseCase getTransactionHistoryUseCase,
                                  TransactionHistoryQueryMapper transactionHistoryQueryMapper) {
        this.getCustomerAccountBalancesUseCase = getCustomerAccountBalancesUseCase;
        this.mapper = mapper;
        this.getTransactionHistoryUseCase = getTransactionHistoryUseCase;
        this.transactionHistoryQueryMapper = transactionHistoryQueryMapper;
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

    @GetMapping("/{customerId}/transactions")
    @Operation(summary = "Retrieve paginated transaction history for a customer")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Transaction history retrieved",
                    content = @Content(schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "404", description = "Customer finance account not found",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<Page<TransactionResponse>> getTransactionHistory(
            @Parameter(description = "Customer UUID") @PathVariable("customerId") UUID customerId,
            @ModelAttribute TransactionHistoryFilterParams filterParams,
            @PageableDefault(size = 20) Pageable pageable) {

        log.info("[GET] Transaction history for customerId={} page={} size={} filter={}",
                customerId, pageable.getPageNumber(), pageable.getPageSize(), filterParams);
        var filter = new TransactionHistoryFilter(
                filterParams.fromDate(), filterParams.toDate(),
                filterParams.sourceId(), filterParams.sourceType());
        var pageRequest = new PageRequest(pageable.getPageSize(), pageable.getPageNumber(), null);
        var result = getTransactionHistoryUseCase.execute(customerId, filter, pageRequest);
        return ResponseEntity.ok(result.map(transactionHistoryQueryMapper::toResponse));
    }
}
