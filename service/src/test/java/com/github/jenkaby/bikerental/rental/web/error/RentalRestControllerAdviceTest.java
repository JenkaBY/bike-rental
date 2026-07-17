package com.github.jenkaby.bikerental.rental.web.error;

import com.github.jenkaby.bikerental.rental.domain.exception.*;
import com.github.jenkaby.bikerental.rental.domain.model.RentalStatus;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;
import com.github.jenkaby.bikerental.shared.exception.InsufficientBalanceException;
import com.github.jenkaby.bikerental.support.web.ApiTest;
import com.github.jenkaby.bikerental.tariff.InvalidSpecialTariffTypeException;
import com.github.jenkaby.bikerental.tariff.SuitableTariffNotFoundException;
import com.github.jenkaby.bikerental.tariff.domain.model.PricingType;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ApiTest(controllers = RentalRestControllerAdviceTest.StubController.class)
@Import(RentalRestControllerAdvice.class)
class RentalRestControllerAdviceTest {

    @Autowired
    private MockMvc mockMvc;

    @Nested
    class InvalidRentalStatus {

        @Test
        void returnsProblemDetailWithCurrentAndExpectedStatus() throws Exception {
            mockMvc.perform(get("/api/stub/rental/invalid-status"))
                    .andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$.errorCode").value("rental.status.invalid"))
                    .andExpect(jsonPath("$.correlationId").exists())
                    .andExpect(jsonPath("$.params.currentStatus").value("COMPLETED"))
                    .andExpect(jsonPath("$.params.expectedStatus").value("ACTIVE"));
        }
    }

    @Nested
    class RentalNotReadyForActivation {

        @Test
        void returnsProblemDetailWithMissingFields() throws Exception {
            mockMvc.perform(get("/api/stub/rental/not-ready"))
                    .andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$.errorCode").value("rental.activation.not_ready"))
                    .andExpect(jsonPath("$.correlationId").exists())
                    .andExpect(jsonPath("$.params.fields").isArray())
                    .andExpect(jsonPath("$.params.fields[0]").value("customerId"))
                    .andExpect(jsonPath("$.params.fields[1]").value("equipmentId"));
        }
    }

    @Nested
    class SuitableTariffNotFound {

        @Test
        void returnsProblemDetailWithTariffSearchParams() throws Exception {
            mockMvc.perform(get("/api/stub/rental/tariff-not-found"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorCode").value("tariff.suitable.not_found"))
                    .andExpect(jsonPath("$.correlationId").exists())
                    .andExpect(jsonPath("$.params.equipmentType").value("BICYCLE"))
                    .andExpect(jsonPath("$.params.rentalDate").value("2025-01-01"));
        }
    }

    @Nested
    class InsufficientBalance {

        @Test
        void returnsRentalSpecificErrorCodeNotFinanceCode() throws Exception {
            mockMvc.perform(get("/api/stub/rental/insufficient-balance"))
                    .andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$.errorCode").value("rental.insufficient_funds"))
                    .andExpect(jsonPath("$.correlationId").exists())
                    .andExpect(jsonPath("$.params.available").value(5.00))
                    .andExpect(jsonPath("$.params.requested").value(12.50));
        }
    }

    @Nested
    class HoldRequired {

        @Test
        void returnsProblemDetailWithConflictStatus() throws Exception {
            mockMvc.perform(get("/api/stub/rental/hold-required"))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.errorCode").value("rental.hold.required"))
                    .andExpect(jsonPath("$.correlationId").exists());
        }
    }

    @Nested
    class InvalidPlannedDuration {

        @Test
        void returnsProblemDetailWithUnprocessableStatus() throws Exception {
            mockMvc.perform(get("/api/stub/rental/invalid-duration"))
                    .andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$.errorCode").value("rental.planned-duration.invalid"))
                    .andExpect(jsonPath("$.correlationId").exists());
        }
    }

    @Nested
    class InvalidSpecialTariffType {

        @Test
        void returnsProblemDetailWithTariffTypeParams() throws Exception {
            mockMvc.perform(get("/api/stub/rental/invalid-special-type"))
                    .andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$.errorCode").value("tariff.special.type_invalid"))
                    .andExpect(jsonPath("$.correlationId").exists())
                    .andExpect(jsonPath("$.params.tariffId").value(10))
                    .andExpect(jsonPath("$.params.actualPricingType").value("FLAT_HOURLY"));
        }
    }

    @Nested
    class EquipmentOccupied {

        @Test
        void returnsProblemDetailWithUnavailableIds() throws Exception {
            mockMvc.perform(get("/api/stub/rental/equipment-occupied"))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.errorCode").value("rental.equipment.not_available"))
                    .andExpect(jsonPath("$.correlationId").exists())
                    .andExpect(jsonPath("$.params.unavailableIds").isArray())
                    .andExpect(jsonPath("$.params.unavailableIds[0]").value(7));
        }
    }

    @Nested
    class InvalidDateRange {

        @Test
        void returnsProblemDetailWithConstraintViolationCode() throws Exception {
            mockMvc.perform(get("/api/stub/rental/invalid-date-range"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("shared.request.constraint_violation"))
                    .andExpect(jsonPath("$.correlationId").exists());
        }
    }

    @RestController
    @RequestMapping("/api/stub/rental")
    static class StubController {

        @GetMapping("/invalid-status")
        void invalidStatus() {
            throw new InvalidRentalStatusException(RentalStatus.COMPLETED, RentalStatus.ACTIVE);
        }

        @GetMapping("/not-ready")
        void notReady() {
            throw new RentalNotReadyForActivationException(List.of("customerId", "equipmentId"));
        }

        @GetMapping("/tariff-not-found")
        void tariffNotFound() {
            throw new SuitableTariffNotFoundException("BICYCLE", LocalDate.of(2025, 1, 1), null);
        }

        @GetMapping("/insufficient-balance")
        void insufficientBalance() {
            throw new InsufficientBalanceException(Money.of("5.00"), Money.of("12.50"));
        }

        @GetMapping("/hold-required")
        void holdRequired() {
            throw new HoldRequiredException(42L);
        }

        @GetMapping("/invalid-duration")
        void invalidDuration() {
            throw new InvalidRentalPlannedDurationException(99L);
        }

        @GetMapping("/invalid-special-type")
        void invalidSpecialType() {
            throw new InvalidSpecialTariffTypeException(10L, PricingType.FLAT_HOURLY);
        }

        @GetMapping("/equipment-occupied")
        void equipmentOccupied() {
            throw new EquipmentOccupiedException(Set.of(7L));
        }

        @GetMapping("/invalid-date-range")
        void invalidDateRange() {
            throw new InvalidDateRangeException(LocalDate.of(2025, 3, 1), LocalDate.of(2025, 1, 1));
        }
    }
}
