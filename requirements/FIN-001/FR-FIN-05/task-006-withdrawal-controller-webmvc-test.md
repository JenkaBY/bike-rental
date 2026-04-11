# Task 006: WebMVC Test for WithdrawalCommandController

> **Applied Skill:** `spring-mvc-controller-test` — `@ApiTest` meta-annotation, `@MockitoBean` for all
> constructor dependencies, `@Nested` structure per controller method, parameterized 400 tests for each invalid
> field, and one 422 test for `InsufficientBalanceException`; mirrors `DepositCommandControllerTest` and
> `AdjustmentCommandControllerTest`.

## 1. Objective

Cover `WithdrawalCommandController` with WebMVC-layer tests verifying:

- `201 Created` for a valid request (happy path).
- `400 Bad Request` for each invalid/missing field: `amount` ≤ 0, missing `idempotencyKey`, missing
  `customerId`, missing `payoutMethod`, blank `operatorId`.
- `422 Unprocessable Content` when the service throws `InsufficientBalanceException`.

## 2. File to Modify / Create

* **File Path:**
  `service/src/test/java/com/github/jenkaby/bikerental/finance/web/command/WithdrawalCommandControllerTest.java`
* **Action:** Create New File

## 3. Code Implementation

**Imports Required:**

```java
import com.github.jenkaby.bikerental.finance.PaymentMethod;
import com.github.jenkaby.bikerental.finance.application.usecase.RecordWithdrawalUseCase;
import com.github.jenkaby.bikerental.finance.application.usecase.RecordWithdrawalUseCase.WithdrawalResult;
import com.github.jenkaby.bikerental.shared.exception.InsufficientBalanceException;
import com.github.jenkaby.bikerental.finance.web.command.dto.TransactionResponse;
import com.github.jenkaby.bikerental.finance.web.command.mapper.WithdrawalCommandMapper;
import com.github.jenkaby.bikerental.shared.domain.IdempotencyKey;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;
import com.github.jenkaby.bikerental.support.web.ApiTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
```

**Code to Add/Replace:**

* **Location:** New file — full contents below.

```java
package com.github.jenkaby.bikerental.finance.web.command;

import com.github.jenkaby.bikerental.finance.PaymentMethod;
import com.github.jenkaby.bikerental.finance.application.usecase.RecordWithdrawalUseCase;
import com.github.jenkaby.bikerental.finance.application.usecase.RecordWithdrawalUseCase.WithdrawalResult;
import com.github.jenkaby.bikerental.shared.exception.InsufficientBalanceException;
import com.github.jenkaby.bikerental.finance.web.command.dto.TransactionResponse;
import com.github.jenkaby.bikerental.finance.web.command.mapper.WithdrawalCommandMapper;
import com.github.jenkaby.bikerental.shared.domain.IdempotencyKey;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;
import com.github.jenkaby.bikerental.support.web.ApiTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ApiTest(controllers = WithdrawalCommandController.class)
class WithdrawalCommandControllerTest {

    private static final String ENDPOINT = "/api/finance/withdrawals";
    private static final UUID CUSTOMER_ID = UUID.fromString("018e2cc3-0001-7000-8000-000000000001");
    private static final UUID TRANSACTION_ID = UUID.fromString("018e2cc3-0002-7000-8000-000000000002");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RecordWithdrawalUseCase recordWithdrawalUseCase;

    @MockitoBean
    private WithdrawalCommandMapper mapper;

    private static Map<String, Object> validRequest() {
        var request = new HashMap<String, Object>();
        request.put("idempotencyKey", UUID.randomUUID().toString());
        request.put("customerId", CUSTOMER_ID.toString());
        request.put("amount", "30.00");
        request.put("payoutMethod", "CASH");
        request.put("operatorId", "operator-1");
        return request;
    }

    @Nested
    @DisplayName("POST /api/finance/withdrawals")
    class RecordWithdrawal {

        @Test
        @DisplayName("should return 201 with transactionId and recordedAt for a valid request")
        void shouldReturn201ForValidRequest() throws Exception {
            Instant now = Instant.now();
            given(mapper.toCommand(any())).willReturn(
                    new RecordWithdrawalUseCase.RecordWithdrawalCommand(
                            CUSTOMER_ID, Money.of(new BigDecimal("30.00")),
                            PaymentMethod.CASH, "operator-1",
                            IdempotencyKey.of(UUID.randomUUID())));
            given(recordWithdrawalUseCase.execute(any())).willReturn(new WithdrawalResult(TRANSACTION_ID, now));
            given(mapper.toResponse(any())).willReturn(new TransactionResponse(TRANSACTION_ID, now));

            mockMvc.perform(post(ENDPOINT)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest())))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.transactionId").value(TRANSACTION_ID.toString()))
                    .andExpect(jsonPath("$.recordedAt").exists());
        }

        @Nested
        @DisplayName("should return 400 when request is invalid")
        class BadRequest {

            @ParameterizedTest(name = "amount={0} is invalid")
            @MethodSource("invalidAmounts")
            @DisplayName("amount must be greater than zero")
            void whenAmountIsInvalid(Object amount) throws Exception {
                var request = validRequest();
                request.put("amount", amount);

                mockMvc.perform(post(ENDPOINT)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.errors[0].field").value("amount"));
            }

            static Stream<Object> invalidAmounts() {
                return Stream.of(BigDecimal.ZERO, new BigDecimal("-1.00"), new BigDecimal("0.00"));
            }

            @Test
            @DisplayName("idempotencyKey must not be null")
            void whenIdempotencyKeyIsMissing() throws Exception {
                var request = validRequest();
                request.remove("idempotencyKey");

                mockMvc.perform(post(ENDPOINT)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.errors[0].field").value("idempotencyKey"));
            }

            @Test
            @DisplayName("customerId must not be null")
            void whenCustomerIdIsNull() throws Exception {
                var request = validRequest();
                request.remove("customerId");

                mockMvc.perform(post(ENDPOINT)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.errors[0].field").value("customerId"));
            }

            @Test
            @DisplayName("payoutMethod must not be null")
            void whenPayoutMethodIsNull() throws Exception {
                var request = validRequest();
                request.remove("payoutMethod");

                mockMvc.perform(post(ENDPOINT)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.errors[0].field").value("payoutMethod"));
            }

            @ParameterizedTest(name = "operatorId=\"{0}\" is blank")
            @MethodSource("blankOperatorIds")
            @DisplayName("operatorId must not be blank")
            void whenOperatorIdIsBlank(String operatorId) throws Exception {
                var request = validRequest();
                request.put("operatorId", operatorId);

                mockMvc.perform(post(ENDPOINT)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.errors[0].field").value("operatorId"));
            }

            static Stream<String> blankOperatorIds() {
                return Stream.of("", "   ");
            }
        }

        @Nested
        @DisplayName("should return 422 when available balance is insufficient")
        class InsufficientBalance {

            @Test
            @DisplayName("service rejects withdrawal that exceeds available balance")
            void whenAmountExceedsAvailableBalance() throws Exception {
                given(mapper.toCommand(any())).willReturn(
                        new RecordWithdrawalUseCase.RecordWithdrawalCommand(
                                CUSTOMER_ID, Money.of(new BigDecimal("100.00")),
                                PaymentMethod.CASH, "operator-1",
                                IdempotencyKey.of(UUID.randomUUID())));
                willThrow(new InsufficientBalanceException(
                        Money.of(new BigDecimal("10.00")),
                        Money.of(new BigDecimal("100.00"))))
                        .given(recordWithdrawalUseCase).execute(any());

                mockMvc.perform(post(ENDPOINT)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(validRequest())))
                        .andExpect(status().isUnprocessableContent())
                        .andExpect(jsonPath("$.errorCode").value("finance.insufficient_balance"));
            }
        }
    }
}
```

## 4. Validation Steps

```bash
./gradlew :service:test "-Dspring.profiles.active=test" --tests WithdrawalCommandControllerTest
```
