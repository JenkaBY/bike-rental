package com.github.jenkaby.bikerental.finance.web.query;

import com.github.jenkaby.bikerental.finance.application.usecase.GetCustomerAccountBalancesUseCase;
import com.github.jenkaby.bikerental.finance.application.usecase.GetCustomerAccountBalancesUseCase.CustomerAccountBalances;
import com.github.jenkaby.bikerental.finance.application.usecase.GetTransactionHistoryUseCase;
import com.github.jenkaby.bikerental.finance.domain.model.TransactionHistoryFilter;
import com.github.jenkaby.bikerental.finance.domain.model.TransactionType;
import com.github.jenkaby.bikerental.finance.web.query.dto.CustomerAccountBalancesResponse;
import com.github.jenkaby.bikerental.finance.web.query.dto.CustomerTransactionResponse;
import com.github.jenkaby.bikerental.finance.web.query.mapper.AccountQueryMapper;
import com.github.jenkaby.bikerental.finance.web.query.mapper.TransactionHistoryQueryMapper;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Page;
import com.github.jenkaby.bikerental.shared.domain.model.vo.PageRequest;
import com.github.jenkaby.bikerental.shared.exception.ResourceNotFoundException;
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
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ApiTest(controllers = AccountQueryController.class)
class AccountQueryControllerTest {

    private static final String BALANCES_ENDPOINT = "/api/finance/customers/{customerId}/balances";
    private static final String TRANSACTIONS_ENDPOINT = "/api/finance/customers/{customerId}/transactions";
    private static final UUID CUSTOMER_ID = UUID.fromString("018e2cc3-0001-7000-8000-000000000001");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GetCustomerAccountBalancesUseCase getCustomerAccountBalancesUseCase;
    @MockitoBean
    private AccountQueryMapper mapper;
    @MockitoBean
    private GetTransactionHistoryUseCase getTransactionHistoryUseCase;
    @MockitoBean
    private TransactionHistoryQueryMapper transactionHistoryQueryMapper;

    @Nested
    class GetBalances {

        @Test
        void shouldReturn200WithBalancesForExistingCustomer() throws Exception {
            var balances = new CustomerAccountBalances(
                    new BigDecimal("120.00"),
                    new BigDecimal("30.00"),
                    Instant.parse("2026-04-07T10:30:00Z")
            );
            var response = new CustomerAccountBalancesResponse(
                    new BigDecimal("120.00"),
                    new BigDecimal("30.00"),
                    Instant.parse("2026-04-07T10:30:00Z")
            );
            given(getCustomerAccountBalancesUseCase.execute(CUSTOMER_ID)).willReturn(balances);
            given(mapper.toResponse(balances)).willReturn(response);

            mockMvc.perform(get(BALANCES_ENDPOINT, CUSTOMER_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.walletBalance").value("120.0"))
                    .andExpect(jsonPath("$.holdBalance").value("30.0"))
                    .andExpect(jsonPath("$.lastUpdatedAt").exists());
        }

        @Test
        void shouldReturn404WhenCustomerAccountNotFound() throws Exception {
            willThrow(new ResourceNotFoundException("CustomerAccount", CUSTOMER_ID.toString()))
                    .given(getCustomerAccountBalancesUseCase).execute(any());

            mockMvc.perform(get(BALANCES_ENDPOINT, CUSTOMER_ID))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404));
        }

        @Nested
        class BadRequest {

            @Test
            void whenCustomerIdIsNotValidUuid() throws Exception {
                mockMvc.perform(get(BALANCES_ENDPOINT, "not-a-uuid"))
                        .andExpect(status().isBadRequest());
            }
        }
    }

    @Nested
    class GetTransactionHistory {

        @Test
        void shouldReturn200WithPagedEntries() throws Exception {
            var entry = new GetTransactionHistoryUseCase.TransactionDto(
                    CUSTOMER_ID,
                    new BigDecimal("50.00"),
                    TransactionType.DEPOSIT,
                    Instant.parse("2026-03-15T10:30:00Z"),
                    null,
                    null,
                    null,
                    null
            );
            var pageRequest = new PageRequest(20, 0, null);
            var page = new Page<>(List.of(entry), 1L, pageRequest);
            var entryResponse = new CustomerTransactionResponse(
                    CUSTOMER_ID,
                    new BigDecimal("50.00"),
                    "DEPOSIT",
                    Instant.parse("2026-03-15T10:30:00Z"),
                    null,
                    null,
                    null,
                    null
            );

            given(getTransactionHistoryUseCase.execute(eq(CUSTOMER_ID), any(TransactionHistoryFilter.class), any(PageRequest.class)))
                    .willReturn(page);
            given(transactionHistoryQueryMapper.toResponse(entry)).willReturn(entryResponse);

            mockMvc.perform(get(TRANSACTIONS_ENDPOINT, CUSTOMER_ID)
                            .param("page", "0")
                            .param("size", "20"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalItems").value(1))
                    .andExpect(jsonPath("$.items[0].type").value("DEPOSIT"));
        }

        @Test
        void shouldReturn404WhenCustomerAccountNotFound() throws Exception {
            willThrow(new ResourceNotFoundException("CustomerAccount", CUSTOMER_ID.toString()))
                    .given(getTransactionHistoryUseCase).execute(eq(CUSTOMER_ID), any(), any());

            mockMvc.perform(get(TRANSACTIONS_ENDPOINT, CUSTOMER_ID))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404));
        }

        @Nested
        class BadRequest {

            @Test
            void whenCustomerIdIsNotValidUuid() throws Exception {
                mockMvc.perform(get("/api/finance/customers/{customerId}/transactions", "not-a-uuid"))
                        .andExpect(status().isBadRequest());
            }

            @Test
            void whenSourceTypeIsUnrecognised() throws Exception {
                mockMvc.perform(get(TRANSACTIONS_ENDPOINT, CUSTOMER_ID)
                                .param("sourceType", "UNKNOWN_VALUE"))
                        .andExpect(status().isBadRequest());
            }
        }
    }
}
