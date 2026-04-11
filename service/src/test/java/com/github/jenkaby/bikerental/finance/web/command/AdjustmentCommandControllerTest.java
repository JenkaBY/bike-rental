package com.github.jenkaby.bikerental.finance.web.command;

import com.github.jenkaby.bikerental.finance.application.usecase.ApplyAdjustmentUseCase;
import com.github.jenkaby.bikerental.finance.application.usecase.ApplyAdjustmentUseCase.AdjustmentResult;
import com.github.jenkaby.bikerental.finance.web.command.dto.TransactionResponse;
import com.github.jenkaby.bikerental.finance.web.command.mapper.AdjustmentCommandMapper;
import com.github.jenkaby.bikerental.shared.domain.IdempotencyKey;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;
import com.github.jenkaby.bikerental.shared.exception.InsufficientBalanceException;
import com.github.jenkaby.bikerental.support.web.ApiTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
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

@ApiTest(controllers = AdjustmentCommandController.class)
class AdjustmentCommandControllerTest {

    private static final String ENDPOINT = "/api/finance/adjustments";
    private static final UUID CUSTOMER_ID = UUID.fromString("018e2cc3-0001-7000-8000-000000000001");
    private static final UUID TRANSACTION_ID = UUID.fromString("018e2cc3-0002-7000-8000-000000000002");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ApplyAdjustmentUseCase applyAdjustmentUseCase;

    @MockitoBean
    private AdjustmentCommandMapper mapper;

    private Map<String, Object> validRequest() {
        var req = new HashMap<String, Object>();
        req.put("customerId", CUSTOMER_ID.toString());
        req.put("amount", "12345678901234567.00");
        req.put("reason", "Compensation for system error");
        req.put("operatorId", "admin-1");
        req.put("idempotencyKey", UUID.randomUUID().toString());
        return req;
    }

    @Nested
    @DisplayName("POST /api/finance/adjustments")
    class ApplyAdjustment {

        @Test
        @DisplayName("should return 201 with transactionId and newWalletBalance for a valid top-up")
        void shouldReturn201ForValidRequest() throws Exception {
            Instant now = Instant.now();
            given(mapper.toCommand(any())).willReturn(
                    new ApplyAdjustmentUseCase.ApplyAdjustmentCommand(
                            CUSTOMER_ID, Money.of(new BigDecimal("10.00")),
                            "Compensation for system error", "admin-1",
                            IdempotencyKey.of(UUID.randomUUID())));
            given(applyAdjustmentUseCase.execute(any())).willReturn(new AdjustmentResult(TRANSACTION_ID, now));
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
        class BadRequests {

            @Test
            @DisplayName("customerId is null")
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
            @DisplayName("amount is null")
            void whenAmountIsNull() throws Exception {
                var request = validRequest();
                request.remove("amount");
                mockMvc.perform(post(ENDPOINT)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.errors[0].field").value("amount"));
            }

            @ParameterizedTest
            @DisplayName("amount is {0}")
            @CsvSource({"0.001", "-0.001", "123456789012345678.00", "-123456789012345678.00"})
            void whenAmountIsOutOfMoneyDigitsRange(String amount) throws Exception {
                var request = validRequest();
                request.put("amount", amount);
                mockMvc.perform(post(ENDPOINT)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.errors[0].field").value("amount"));
            }

            @ParameterizedTest(name = "reason=''{0}''")
            @MethodSource("blankReasons")
            @DisplayName("reason must not be blank")
            void whenReasonIsBlank(String reason) throws Exception {
                var request = validRequest();
                request.put("reason", reason);
                mockMvc.perform(post(ENDPOINT)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.errors[0].field").value("reason"));
            }

            static Stream<String> blankReasons() {
                return Stream.of("", "   ");
            }

            @ParameterizedTest(name = "operatorId=''{0}''")
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
        @DisplayName("should return 422 when wallet balance is insufficient")
        class InsufficientBalance {

            @Test
            @DisplayName("service rejects deduction that exceeds wallet balance")
            void whenDeductionExceedsBalance() throws Exception {
                var request = validRequest();
                request.put("amount", "-20.00");

                given(mapper.toCommand(any())).willReturn(
                        new ApplyAdjustmentUseCase.ApplyAdjustmentCommand(
                                CUSTOMER_ID, Money.of(new BigDecimal("-20.00")),
                                "Overcharge correction", "admin-1",
                                IdempotencyKey.of(UUID.randomUUID())));
                willThrow(new InsufficientBalanceException(
                        Money.of(new BigDecimal("10.00")),
                        Money.of(new BigDecimal("20.00"))))
                        .given(applyAdjustmentUseCase).execute(any());

                mockMvc.perform(post(ENDPOINT)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isUnprocessableContent())
                        .andExpect(jsonPath("$.errorCode").value("finance.insufficient_balance"));
            }
        }
    }
}
