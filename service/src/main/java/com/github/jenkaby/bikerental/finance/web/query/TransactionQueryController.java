package com.github.jenkaby.bikerental.finance.web.query;

import com.github.jenkaby.bikerental.finance.application.usecase.FindTransactionsUseCase;
import com.github.jenkaby.bikerental.finance.application.usecase.GetTransactionDetailsUseCase;
import com.github.jenkaby.bikerental.finance.web.query.dto.TransactionDetailsResponse;
import com.github.jenkaby.bikerental.finance.web.query.dto.TransactionFilterParams;
import com.github.jenkaby.bikerental.finance.web.query.dto.TransactionSummaryResponse;
import com.github.jenkaby.bikerental.finance.web.query.mapper.TransactionQueryMapper;
import com.github.jenkaby.bikerental.shared.config.OpenApiConfig;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Page;
import com.github.jenkaby.bikerental.shared.mapper.PageMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Validated
@RestController
@RequestMapping(path = "/api/finance/transactions", produces = {MediaType.APPLICATION_JSON_VALUE})
@Tag(name = OpenApiConfig.Tags.FINANCE)
@Slf4j
class TransactionQueryController {

    private final FindTransactionsUseCase findTransactionsUseCase;
    private final GetTransactionDetailsUseCase getTransactionDetailsUseCase;
    private final TransactionQueryMapper mapper;
    private final PageMapper pageMapper;

    TransactionQueryController(FindTransactionsUseCase findTransactionsUseCase,
                               GetTransactionDetailsUseCase getTransactionDetailsUseCase,
                               TransactionQueryMapper mapper,
                               PageMapper pageMapper) {
        this.findTransactionsUseCase = findTransactionsUseCase;
        this.getTransactionDetailsUseCase = getTransactionDetailsUseCase;
        this.mapper = mapper;
        this.pageMapper = pageMapper;
    }

    @GetMapping
    @Operation(summary = "List transactions across customers with the full double-entry breakdown",
            description = "Returns a paged list of business transactions filtered by any combination of customer ids, "
                    + "recorded-at date range, source, and affected ledger types. Sortable by recordedAt, amount or type; "
                    + "defaults to recordedAt descending.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Transactions page retrieved"),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<Page<TransactionSummaryResponse>> findTransactions(
            @Valid @ModelAttribute TransactionFilterParams filterParams,
            @PageableDefault(size = 20, sort = "recordedAt", direction = Sort.Direction.DESC) Pageable pageable) {

        log.info("[GET] Transactions page={} size={} sort={} filter={}",
                pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort(), filterParams);
        var filter = mapper.toFilter(filterParams);
        var pageRequest = pageMapper.toPageRequest(pageable);
        var result = findTransactionsUseCase.execute(filter, pageRequest);
        return ResponseEntity.ok(result.map(mapper::toResponse));
    }

    @GetMapping("/{transactionId}")
    @Operation(summary = "Get a single transaction with its full double-entry breakdown",
            description = "Returns the transaction identified by the path id, including every ledger leg with its "
                    + "signed movement and running balance, plus the customer-side deltas and resulting bucket balances.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Transaction retrieved"),
            @ApiResponse(responseCode = "400", description = "Malformed transaction id",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Transaction not found",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<TransactionDetailsResponse> getTransactionDetails(@PathVariable("transactionId") UUID transactionId) {
        log.info("[GET] Transaction details id={}", transactionId);
        var details = getTransactionDetailsUseCase.execute(transactionId);
        return ResponseEntity.ok(mapper.toDetailsResponse(details));
    }
}
