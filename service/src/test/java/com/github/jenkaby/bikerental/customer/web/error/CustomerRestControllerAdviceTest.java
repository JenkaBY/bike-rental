package com.github.jenkaby.bikerental.customer.web.error;

import com.github.jenkaby.bikerental.customer.domain.exception.DuplicatePhoneException;
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

@ApiTest(controllers = CustomerRestControllerAdviceTest.StubController.class)
@Import(CustomerRestControllerAdvice.class)
class CustomerRestControllerAdviceTest {

    @Autowired
    private MockMvc mockMvc;

    @Nested
    class DuplicatePhone {

        @Test
        void returnsProblemDetailWithErrorCodeAndParams() throws Exception {
            mockMvc.perform(get("/api/stub/customer/duplicate-phone"))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.errorCode").value("customer.phone.duplicate"))
                    .andExpect(jsonPath("$.correlationId").exists())
                    .andExpect(jsonPath("$.params.resourceName").value("Customer"))
                    .andExpect(jsonPath("$.params.identifier").value("+49123456"));
        }
    }

    @RestController
    @RequestMapping("/api/stub/customer")
    static class StubController {

        @GetMapping("/duplicate-phone")
        void duplicatePhone() {
            throw new DuplicatePhoneException("Customer", "+49123456");
        }
    }
}
