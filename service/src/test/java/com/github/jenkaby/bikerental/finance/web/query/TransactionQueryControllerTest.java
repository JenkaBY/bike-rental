package com.github.jenkaby.bikerental.finance.web.query;

import com.github.jenkaby.bikerental.finance.PaymentMethod;
import com.github.jenkaby.bikerental.finance.application.usecase.FindTransactionsUseCase;
import com.github.jenkaby.bikerental.finance.application.usecase.GetTransactionDetailsUseCase;
import com.github.jenkaby.bikerental.finance.domain.model.EntryDirection;
import com.github.jenkaby.bikerental.finance.domain.model.LedgerType;
import com.github.jenkaby.bikerental.finance.domain.model.SubLedgerRef;
import com.github.jenkaby.bikerental.finance.domain.model.Transaction;
import com.github.jenkaby.bikerental.finance.domain.model.TransactionDetails;
import com.github.jenkaby.bikerental.finance.domain.model.TransactionFilter;
import com.github.jenkaby.bikerental.finance.domain.model.TransactionRecord;
import com.github.jenkaby.bikerental.finance.domain.model.TransactionType;
import com.github.jenkaby.bikerental.finance.web.query.dto.TransactionBalancesResponse;
import com.github.jenkaby.bikerental.finance.web.query.dto.TransactionDeltasResponse;
import com.github.jenkaby.bikerental.finance.web.query.dto.TransactionDetailsResponse;
import com.github.jenkaby.bikerental.finance.web.query.dto.TransactionDetailsResponse.TransactionDetailEntryResponse;
import com.github.jenkaby.bikerental.finance.web.query.dto.TransactionSummaryResponse;
import com.github.jenkaby.bikerental.finance.web.query.dto.TransactionSummaryResponse.TransactionEntryResponse;
import com.github.jenkaby.bikerental.finance.web.query.mapper.TransactionQueryMapper;
import com.github.jenkaby.bikerental.shared.domain.IdempotencyKey;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Page;
import com.github.jenkaby.bikerental.shared.domain.model.vo.PageRequest;
import com.github.jenkaby.bikerental.shared.exception.ResourceNotFoundException;
import com.github.jenkaby.bikerental.shared.mapper.PageMapper;
import com.github.jenkaby.bikerental.support.web.ApiTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ApiTest(controllers = TransactionQueryController.class)
class TransactionQueryControllerTest {

    private static final String TRANSACTIONS_ENDPOINT = "/api/finance/transactions";
    private static final UUID CUSTOMER_ID = UUID.fromString("018e2cc3-0001-7000-8000-000000000001");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FindTransactionsUseCase findTransactionsUseCase;
    @MockitoBean
    private GetTransactionDetailsUseCase getTransactionDetailsUseCase;
    @MockitoBean
    private TransactionQueryMapper mapper;
    @MockitoBean
    private PageMapper pageMapper;

    @Nested
    class FindTransactions {

        @Test
        void shouldReturn200WithPagedTransactionsCarryingAllLegs() throws Exception {
            var transactionId = UUID.fromString("018e2cc3-0002-7000-8000-000000000002");
            var transaction = Transaction.builder()
                    .id(transactionId)
                    .type(TransactionType.DEPOSIT)
                    .paymentMethod(PaymentMethod.CASH)
                    .amount(Money.of("50.00"))
                    .customerId(CUSTOMER_ID)
                    .operatorId("operator-1")
                    .recordedAt(Instant.parse("2026-03-15T10:30:00Z"))
                    .idempotencyKey(IdempotencyKey.of(UUID.fromString("018e2cc3-0003-7000-8000-000000000003")))
                    .records(List.of(
                            TransactionRecord.builder()
                                    .id(UUID.fromString("018e2cc3-0004-7000-8000-000000000004"))
                                    .subLedgerRef(new SubLedgerRef(UUID.fromString("018e2cc3-0005-7000-8000-000000000005")))
                                    .ledgerType(LedgerType.CASH)
                                    .direction(EntryDirection.DEBIT)
                                    .amount(Money.of("50.00"))
                                    .build(),
                            TransactionRecord.builder()
                                    .id(UUID.fromString("018e2cc3-0006-7000-8000-000000000006"))
                                    .subLedgerRef(new SubLedgerRef(UUID.fromString("018e2cc3-0007-7000-8000-000000000007")))
                                    .ledgerType(LedgerType.CUSTOMER_WALLET)
                                    .direction(EntryDirection.CREDIT)
                                    .amount(Money.of("50.00"))
                                    .build()))
                    .build();
            var pageRequest = new PageRequest(20, 0);
            var page = new Page<>(List.of(transaction), 1L, pageRequest);
            var response = new TransactionSummaryResponse(
                    transactionId,
                    CUSTOMER_ID,
                    new BigDecimal("50.00"),
                    "DEPOSIT",
                    Instant.parse("2026-03-15T10:30:00Z"),
                    "CASH",
                    null,
                    null,
                    null,
                    "operator-1",
                    List.of(
                            new TransactionEntryResponse("CASH", "DEBIT", new BigDecimal("50.00")),
                            new TransactionEntryResponse("CUSTOMER_WALLET", "CREDIT", new BigDecimal("50.00"))));

            given(mapper.toFilter(any())).willReturn(TransactionFilter.empty());
            given(pageMapper.toPageRequest(any())).willReturn(pageRequest);
            given(findTransactionsUseCase.execute(any(TransactionFilter.class), any(PageRequest.class))).willReturn(page);
            given(mapper.toResponse(transaction)).willReturn(response);

            mockMvc.perform(get(TRANSACTIONS_ENDPOINT)
                            .param("page", "0")
                            .param("size", "20"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalItems").value(1))
                    .andExpect(jsonPath("$.items[0].id").value(transactionId.toString()))
                    .andExpect(jsonPath("$.items[0].type").value("DEPOSIT"))
                    .andExpect(jsonPath("$.items[0].operatorId").value("operator-1"))
                    .andExpect(jsonPath("$.items[0].entries.length()").value(2))
                    .andExpect(jsonPath("$.items[0].entries[0].ledgerType").value("CASH"))
                    .andExpect(jsonPath("$.items[0].entries[0].direction").value("DEBIT"))
                    .andExpect(jsonPath("$.items[0].entries[1].ledgerType").value("CUSTOMER_WALLET"));
        }

        @Test
        void shouldReturn200WithEmptyPageWhenNothingMatches() throws Exception {
            var pageRequest = new PageRequest(20, 0);
            given(mapper.toFilter(any())).willReturn(TransactionFilter.empty());
            given(pageMapper.toPageRequest(any())).willReturn(pageRequest);
            given(findTransactionsUseCase.execute(any(TransactionFilter.class), any(PageRequest.class)))
                    .willReturn(Page.empty(pageRequest));

            mockMvc.perform(get(TRANSACTIONS_ENDPOINT))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalItems").value(0))
                    .andExpect(jsonPath("$.items.length()").value(0));
        }

        @Nested
        class BadRequest {

            @Test
            void whenCustomerIdIsNotValidUuid() throws Exception {
                mockMvc.perform(get(TRANSACTIONS_ENDPOINT).param("customerIds", "not-a-uuid"))
                        .andExpect(status().isBadRequest());
            }

            @Test
            void whenSourceTypeIsUnrecognised() throws Exception {
                mockMvc.perform(get(TRANSACTIONS_ENDPOINT).param("sourceType", "UNKNOWN_VALUE"))
                        .andExpect(status().isBadRequest());
            }

            @Test
            void whenLedgerTypeIsUnrecognised() throws Exception {
                mockMvc.perform(get(TRANSACTIONS_ENDPOINT).param("ledgerTypes", "UNKNOWN_LEDGER"))
                        .andExpect(status().isBadRequest());
            }
        }
    }

    @Nested
    class GetTransactionDetails {

        private static final String DETAILS_ENDPOINT = TRANSACTIONS_ENDPOINT + "/{transactionId}";
        private static final UUID TRANSACTION_ID = UUID.fromString("018e2cc3-0002-7000-8000-000000000002");

        @Test
        void shouldReturn200WithFullBreakdown() throws Exception {
            var transaction = Transaction.builder()
                    .id(TRANSACTION_ID)
                    .type(TransactionType.DEPOSIT)
                    .paymentMethod(PaymentMethod.CASH)
                    .amount(Money.of("50.00"))
                    .customerId(CUSTOMER_ID)
                    .operatorId("operator-1")
                    .recordedAt(Instant.parse("2026-03-15T10:30:00Z"))
                    .idempotencyKey(IdempotencyKey.of(UUID.fromString("018e2cc3-0003-7000-8000-000000000003")))
                    .records(List.of())
                    .build();
            var details = new TransactionDetails(transaction, Money.of("50.00"), Money.of("0.00"));
            var response = new TransactionDetailsResponse(
                    TRANSACTION_ID,
                    CUSTOMER_ID,
                    new BigDecimal("50.00"),
                    "DEPOSIT",
                    Instant.parse("2026-03-15T10:30:00Z"),
                    "CASH",
                    null,
                    null,
                    null,
                    "operator-1",
                    new TransactionDeltasResponse(
                            new BigDecimal("50.00"), new BigDecimal("0.00"), new BigDecimal("50.00")),
                    new TransactionBalancesResponse(new BigDecimal("50.00"), new BigDecimal("0.00")),
                    List.of(
                            new TransactionDetailEntryResponse("CUSTOMER_WALLET", "CREDIT", new BigDecimal("50.00"),
                                    new BigDecimal("50.00"), new BigDecimal("50.00"), false),
                            new TransactionDetailEntryResponse("CASH", "DEBIT", new BigDecimal("50.00"),
                                    new BigDecimal("50.00"), null, true)));

            given(getTransactionDetailsUseCase.execute(TRANSACTION_ID)).willReturn(details);
            given(mapper.toDetailsResponse(details)).willReturn(response);

            mockMvc.perform(get(DETAILS_ENDPOINT, TRANSACTION_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(TRANSACTION_ID.toString()))
                    .andExpect(jsonPath("$.type").value("DEPOSIT"))
                    .andExpect(jsonPath("$.deltas.wallet").value(50.00))
                    .andExpect(jsonPath("$.deltas.hold").value(0.00))
                    .andExpect(jsonPath("$.deltas.external").value(50.00))
                    .andExpect(jsonPath("$.balances.wallet").value(50.00))
                    .andExpect(jsonPath("$.balances.hold").value(0.00))
                    .andExpect(jsonPath("$.entries.length()").value(2))
                    .andExpect(jsonPath("$.entries[1].ledgerType").value("CASH"))
                    .andExpect(jsonPath("$.entries[1].signedDelta").value(50.00))
                    .andExpect(jsonPath("$.entries[1].systemLedger").value(true))
                    .andExpect(jsonPath("$.entries[0].systemLedger").value(false));
        }

        @Test
        void shouldReturn404WhenTransactionDoesNotExist() throws Exception {
            given(getTransactionDetailsUseCase.execute(eq(TRANSACTION_ID)))
                    .willThrow(new ResourceNotFoundException(Transaction.class, TRANSACTION_ID));

            mockMvc.perform(get(DETAILS_ENDPOINT, TRANSACTION_ID))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorCode").value(ResourceNotFoundException.ERROR_CODE))
                    .andExpect(jsonPath("$.params.resourceName").value("Transaction"));
        }

        @Nested
        class BadRequest {

            @Test
            void whenTransactionIdIsNotValidUuid() throws Exception {
                mockMvc.perform(get(DETAILS_ENDPOINT, "not-a-uuid"))
                        .andExpect(status().isBadRequest());
            }
        }
    }
}
