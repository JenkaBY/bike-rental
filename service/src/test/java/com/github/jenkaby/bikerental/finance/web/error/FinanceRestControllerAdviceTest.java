package com.github.jenkaby.bikerental.finance.web.error;

import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;
import com.github.jenkaby.bikerental.shared.exception.InsufficientBalanceException;
import com.github.jenkaby.bikerental.support.web.ApiTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ApiTest(controllers = FinanceRestControllerAdviceTest.StubController.class)
@Import(FinanceRestControllerAdvice.class)
class FinanceRestControllerAdviceTest {

    @Autowired
    private MockMvc mockMvc;

    @Nested
    class InsufficientBalance {

        @Test
        void returnsProblemDetailWithFinanceErrorCode() throws Exception {
            mockMvc.perform(get("/api/stub/finance/insufficient-balance"))
                    .andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$.errorCode").value("finance.insufficient_balance"))
                    .andExpect(jsonPath("$.correlationId").exists())
                    .andExpect(jsonPath("$.detail").value("Insufficient wallet balance. Available: 10.00, requested deduction: 25.00"))
                    .andExpect(jsonPath("$.params.available").value(10.00))
                    .andExpect(jsonPath("$.params.requested").value(25.00));
        }
    }

    @RestController
    @RequestMapping("/api/stub/finance")
    static class StubController {

        @GetMapping("/insufficient-balance")
        void insufficientBalance() {
            throw new InsufficientBalanceException(Money.of("10.00"), Money.of("25.00"));
        }
    }
}
