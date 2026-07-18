package com.github.jenkaby.bikerental.finance.web.query;

import com.github.jenkaby.bikerental.finance.PaymentMethod;
import com.github.jenkaby.bikerental.finance.application.usecase.FindTransactionsUseCase;
import com.github.jenkaby.bikerental.finance.domain.model.EntryDirection;
import com.github.jenkaby.bikerental.finance.domain.model.LedgerType;
import com.github.jenkaby.bikerental.finance.domain.model.SubLedgerRef;
import com.github.jenkaby.bikerental.finance.domain.model.Transaction;
import com.github.jenkaby.bikerental.finance.domain.model.TransactionFilter;
import com.github.jenkaby.bikerental.finance.domain.model.TransactionRecord;
import com.github.jenkaby.bikerental.finance.domain.model.TransactionType;
import com.github.jenkaby.bikerental.finance.web.query.dto.TransactionSummaryResponse;
import com.github.jenkaby.bikerental.finance.web.query.dto.TransactionSummaryResponse.TransactionEntryResponse;
import com.github.jenkaby.bikerental.finance.web.query.mapper.TransactionQueryMapper;
import com.github.jenkaby.bikerental.shared.domain.IdempotencyKey;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Page;
import com.github.jenkaby.bikerental.shared.domain.model.vo.PageRequest;
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
}
