package com.github.jenkaby.bikerental.equipment.web.error;

import com.github.jenkaby.bikerental.equipment.domain.exception.InvalidStatusTransitionException;
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

@ApiTest(controllers = EquipmentRestControllerAdviceTest.StubController.class)
@Import(EquipmentRestControllerAdvice.class)
class EquipmentRestControllerAdviceTest {

    @Autowired
    private MockMvc mockMvc;

    @Nested
    class InvalidStatusTransition {

        @Test
        void returnsProblemDetailWithErrorCodeAndParams() throws Exception {
            mockMvc.perform(get("/api/stub/equipment/invalid-transition"))
                    .andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$.errorCode").value("equipment.status.invalid_transition"))
                    .andExpect(jsonPath("$.correlationId").exists())
                    .andExpect(jsonPath("$.params.id").value(42))
                    .andExpect(jsonPath("$.params.fromStatus").value("ACTIVE"))
                    .andExpect(jsonPath("$.params.toStatus").value("DRAFT"));
        }
    }

    @RestController
    @RequestMapping("/api/stub/equipment")
    static class StubController {

        @GetMapping("/invalid-transition")
        void invalidTransition() {
            throw new InvalidStatusTransitionException(42L, "ACTIVE", "DRAFT");
        }
    }
}
