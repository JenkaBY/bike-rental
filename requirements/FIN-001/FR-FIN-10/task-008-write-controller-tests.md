# Task 008: Write WebMVC Controller Tests for Transaction History Endpoint

> **Applied Skill:** `spring-mvc-controller-test` — @ApiTest, @MockitoBean, @Nested, BadRequest parameterized tests

## 1. Objective

Create `AccountQueryControllerTransactionHistoryTest` covering the new `GET /{customerId}/transactions` endpoint:
a happy-path 200, a 404 when the customer account is not found, and BadRequest cases for an invalid `customerId` UUID
and an unrecognised `sourceType` enum value.

## 2. File to Modify / Create

* **File Path:**
  `service/src/test/java/com/github/jenkaby/bikerental/finance/web/query/AccountQueryControllerTransactionHistoryTest.java`
* **Action:** Create New File

## 3. Code Implementation

**Snippet:**

```java
package com.github.jenkaby.bikerental.finance.web.query;

import com.github.jenkaby.bikerental.finance.application.usecase.GetTransactionHistoryUseCase;
import com.github.jenkaby.bikerental.finance.application.usecase.GetTransactionHistoryUseCase.TransactionEntryDto;
import com.github.jenkaby.bikerental.finance.domain.model.EntryDirection;
import com.github.jenkaby.bikerental.finance.domain.model.LedgerType;
import com.github.jenkaby.bikerental.finance.domain.model.TransactionHistoryFilter;
import com.github.jenkaby.bikerental.finance.domain.model.TransactionType;
import com.github.jenkaby.bikerental.finance.web.query.dto.TransactionResponse;
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
class AccountQueryControllerTransactionHistoryTest {

    private static final String ENDPOINT = "/api/finance/customers/{customerId}/transactions";
    private static final UUID CUSTOMER_ID = UUID.fromString("018e2cc3-0001-7000-8000-000000000001");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GetTransactionHistoryUseCase getTransactionHistoryUseCase;

    @MockitoBean
    private TransactionHistoryQueryMapper transactionHistoryQueryMapper;

    // Required by AccountQueryController constructor — mock but not exercised in these tests
    @MockitoBean
    private com.github.jenkaby.bikerental.finance.application.usecase.GetCustomerAccountBalancesUseCase getCustomerAccountBalancesUseCase;

    @MockitoBean
    private com.github.jenkaby.bikerental.finance.web.query.mapper.AccountQueryMapper accountQueryMapper;

    @Nested
    class GetTransactionHistory {

        @Test
        void shouldReturn200WithPagedEntries() throws Exception {
            var entry = new TransactionEntryDto(
                    LedgerType.CUSTOMER_WALLET,
                    CUSTOMER_ID,
                    new BigDecimal("50.00"),
                    EntryDirection.CREDIT,
                    TransactionType.DEPOSIT,
                    Instant.parse("2026-03-15T10:30:00Z"),
                    null,
                    null,
                    null,
                    null
            );
            var pageRequest = new PageRequest(20, 0, null);
            var page = new Page<>(List.of(entry), 1L, pageRequest);
            var entryResponse = new TransactionEntryResponse(
                    "CUSTOMER_WALLET",
                    CUSTOMER_ID.toString(),
                    new BigDecimal("50.00"),
                    "CREDIT",
                    "DEPOSIT",
                    Instant.parse("2026-03-15T10:30:00Z"),
                    null,
                    null,
                    null,
                    null
            );

            given(getTransactionHistoryUseCase.execute(eq(CUSTOMER_ID), any(TransactionHistoryFilter.class), any(PageRequest.class)))
                    .willReturn(page);
            given(transactionHistoryQueryMapper.toEntry(entry)).willReturn(entryResponse);

            mockMvc.perform(get(ENDPOINT, CUSTOMER_ID)
                            .param("page", "0")
                            .param("size", "20"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalItems").value(1))
                    .andExpect(jsonPath("$.items[0].subLedger").value("CUSTOMER_WALLET"))
                    .andExpect(jsonPath("$.items[0].customerId").value(CUSTOMER_ID.toString()))
                    .andExpect(jsonPath("$.items[0].direction").value("CREDIT"))
                    .andExpect(jsonPath("$.items[0].type").value("DEPOSIT"));
        }

        @Test
        void shouldReturn404WhenCustomerAccountNotFound() throws Exception {
            willThrow(new ResourceNotFoundException("CustomerAccount", CUSTOMER_ID.toString()))
                    .given(getTransactionHistoryUseCase).execute(eq(CUSTOMER_ID), any(), any());

            mockMvc.perform(get(ENDPOINT, CUSTOMER_ID))
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
                mockMvc.perform(get(ENDPOINT, CUSTOMER_ID)
                                .param("sourceType", "UNKNOWN_VALUE"))
                        .andExpect(status().isBadRequest());
            }
        }
    }
}
```

## 4. Validation Steps

```bash
./gradlew :service:test "-Dspring.profiles.active=test" --tests AccountQueryControllerTransactionHistoryTest
```

All 4 tests must be green.
