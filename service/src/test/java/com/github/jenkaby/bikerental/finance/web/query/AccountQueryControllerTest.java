package com.github.jenkaby.bikerental.finance.web.query;

import com.github.jenkaby.bikerental.finance.application.usecase.GetCustomerAccountBalancesUseCase;
import com.github.jenkaby.bikerental.finance.application.usecase.GetCustomerAccountBalancesUseCase.CustomerAccountBalances;
import com.github.jenkaby.bikerental.finance.web.query.dto.CustomerAccountBalancesResponse;
import com.github.jenkaby.bikerental.finance.web.query.mapper.AccountQueryMapper;
import com.github.jenkaby.bikerental.shared.exception.ResourceNotFoundException;
import com.github.jenkaby.bikerental.support.web.ApiTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ApiTest(controllers = AccountQueryController.class)
class AccountQueryControllerTest {

    private static final String ENDPOINT = "/api/finance/customers/{customerId}/balances";
    private static final UUID CUSTOMER_ID = UUID.fromString("018e2cc3-0001-7000-8000-000000000001");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GetCustomerAccountBalancesUseCase getCustomerAccountBalancesUseCase;

    @MockitoBean
    private AccountQueryMapper mapper;

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

            mockMvc.perform(get(ENDPOINT, CUSTOMER_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.walletBalance").value("120.0"))
                    .andExpect(jsonPath("$.holdBalance").value("30.0"))
                    .andExpect(jsonPath("$.lastUpdatedAt").exists());
        }

        @Test
        void shouldReturn404WhenCustomerAccountNotFound() throws Exception {
            willThrow(new ResourceNotFoundException("CustomerAccount", CUSTOMER_ID.toString()))
                    .given(getCustomerAccountBalancesUseCase).execute(any());

            mockMvc.perform(get(ENDPOINT, CUSTOMER_ID))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404));
        }

        @Nested
        class BadRequest {

            @Test
            void whenCustomerIdIsNotValidUuid() throws Exception {
                mockMvc.perform(get(ENDPOINT, "not-a-uuid"))
                        .andExpect(status().isBadRequest());
            }
        }
    }
}
