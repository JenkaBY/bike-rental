package com.github.jenkaby.bikerental.finance.web.command;

import com.github.jenkaby.bikerental.finance.PaymentMethod;
import com.github.jenkaby.bikerental.finance.application.usecase.RecordDepositUseCase;
import com.github.jenkaby.bikerental.finance.application.usecase.RecordDepositUseCase.DepositResult;
import com.github.jenkaby.bikerental.finance.web.command.dto.TransactionResponse;
import com.github.jenkaby.bikerental.finance.web.command.mapper.DepositCommandMapper;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;
import com.github.jenkaby.bikerental.support.web.ApiTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ApiTest(controllers = DepositCommandController.class)
class DepositCommandControllerTest {

    private static final String ENDPOINT = "/api/finance/deposits";
    private static final UUID CUSTOMER_ID = UUID.fromString("018e2cc3-0001-7000-8000-000000000001");
    private static final UUID TRANSACTION_ID = UUID.fromString("018e2cc3-0002-7000-8000-000000000002");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RecordDepositUseCase recordDepositUseCase;

    @MockitoBean
    private DepositCommandMapper mapper;

    @Nested
    @DisplayName("POST /api/finance/deposits")
    class RecordDeposit {

        @Test
        @DisplayName("should return 201 with transactionId and recordedAt for a valid request")
        void shouldReturn201ForValidRequest() throws Exception {
            var request = Map.of(
                    "idempotencyKey", UUID.randomUUID().toString(),
                    "customerId", CUSTOMER_ID.toString(),
                    "amount", "50.00",
                    "paymentMethod", "CASH",
                    "operatorId", "operator-1"
            );
            Instant now = Instant.now();
            given(mapper.toCommand(any())).willReturn(
                    new RecordDepositUseCase.RecordDepositCommand(CUSTOMER_ID, Money.of(new BigDecimal("50.00")),
                            PaymentMethod.CASH, "operator-1", com.github.jenkaby.bikerental.shared.domain.IdempotencyKey.of(UUID.randomUUID())));
            given(recordDepositUseCase.execute(any())).willReturn(new DepositResult(TRANSACTION_ID, now));
            given(mapper.toResponse(any())).willReturn(new TransactionResponse(TRANSACTION_ID, now));

            mockMvc.perform(post(ENDPOINT)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.transactionId").value(TRANSACTION_ID.toString()))
                    .andExpect(jsonPath("$.recordedAt").exists());
        }

        @Nested
        @DisplayName("should return 400 when request is invalid")
        class BadRequests {

            @ParameterizedTest(name = "amount={0} is invalid")
            @MethodSource("invalidAmounts")
            @DisplayName("amount must be greater than zero")
            void whenAmountIsInvalid(Object amount) throws Exception {
                var request = Map.of(
                        "idempotencyKey", UUID.randomUUID().toString(),
                        "customerId", CUSTOMER_ID.toString(),
                        "amount", amount,
                        "paymentMethod", "CASH",
                        "operatorId", "operator-1"
                );
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
            void whenIdempotencyKeyIsMissing() throws Exception {
                var request = Map.of(
                        "customerId", CUSTOMER_ID.toString(),
                        "amount", "50",
                        "paymentMethod", "CASH",
                        "operatorId", "operator-1"
                );
                mockMvc.perform(post(ENDPOINT)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.errors[0].field").value("idempotencyKey"));
            }

            @Test
            @DisplayName("customerId must not be null")
            void whenCustomerIdIsNull() throws Exception {
                var request = Map.of(
                        "idempotencyKey", UUID.randomUUID().toString(),
                        "amount", "50.00",
                        "paymentMethod", "CASH",
                        "operatorId", "operator-1"
                );
                mockMvc.perform(post(ENDPOINT)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.errors[0].field").value("customerId"));
            }

            @Test
            @DisplayName("paymentMethod must not be null")
            void whenPaymentMethodIsNull() throws Exception {
                var request = Map.of(
                        "idempotencyKey", UUID.randomUUID().toString(),
                        "customerId", CUSTOMER_ID.toString(),
                        "amount", "50.00",
                        "operatorId", "operator-1"
                );
                mockMvc.perform(post(ENDPOINT)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.errors[0].field").value("paymentMethod"));
            }

            @ParameterizedTest(name = "operatorId=''{0}''")
            @NullSource
            @MethodSource("blankOperatorIds")
            @DisplayName("operatorId must not be blank")
            void whenOperatorIdIsBlank(String operatorId) throws Exception {
                var body = new HashMap<String, Object>();
                body.put("customerId", CUSTOMER_ID.toString());
                body.put("amount", "50.00");
                body.put("paymentMethod", "CASH");
                body.put("operatorId", operatorId);
                body.put("idempotencyKey", UUID.randomUUID().toString());
                mockMvc.perform(post(ENDPOINT)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(body)))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.errors[0].field").value("operatorId"));
            }

            static Stream<String> blankOperatorIds() {
                return Stream.of("", "   ");
            }
        }
    }
}
