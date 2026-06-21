package com.github.jenkaby.bikerental.tariff.web.error;

import com.github.jenkaby.bikerental.support.web.ApiTest;
import com.github.jenkaby.bikerental.tariff.InvalidSpecialTariffTypeException;
import com.github.jenkaby.bikerental.tariff.SuitableTariffNotFoundException;
import com.github.jenkaby.bikerental.tariff.domain.exception.InvalidSpecialPriceException;
import com.github.jenkaby.bikerental.tariff.domain.model.PricingType;
import com.github.jenkaby.bikerental.tariff.web.error.TariffRestControllerAdvice;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ApiTest(controllers = TariffRestControllerAdviceTest.StubController.class)
@Import(TariffRestControllerAdvice.class)
class TariffRestControllerAdviceTest {

    @Autowired
    private MockMvc mockMvc;

    @Nested
    class SuitableTariffNotFound {

        @Test
        void returnsProblemDetailWithParamsAndNotFoundStatus() throws Exception {
            mockMvc.perform(get("/api/stub/tariff/suitable-not-found"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorCode").value("tariff.suitable.not_found"))
                    .andExpect(jsonPath("$.correlationId").exists())
                    .andExpect(jsonPath("$.params.equipmentType").value("BICYCLE"))
                    .andExpect(jsonPath("$.params.rentalDate").value("2025-01-01"))
                    .andExpect(jsonPath("$.params.duration").doesNotExist());
        }
    }

    @Nested
    class InvalidTariffPricing {

        @Test
        void returnsProblemDetailWithErrorCode() throws Exception {
            mockMvc.perform(get("/api/stub/tariff/invalid-pricing"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("tariff.pricing.invalid"))
                    .andExpect(jsonPath("$.correlationId").exists());
        }
    }

    @Nested
    class InvalidSpecialTariffType {

        @Test
        void returnsProblemDetailWithTariffTypeParams() throws Exception {
            mockMvc.perform(get("/api/stub/tariff/invalid-special-type"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("tariff.special.type_invalid"))
                    .andExpect(jsonPath("$.correlationId").exists())
                    .andExpect(jsonPath("$.params.tariffId").value(10))
                    .andExpect(jsonPath("$.params.actualPricingType").value("FLAT_HOURLY"));
        }
    }

    @Nested
    class InvalidSpecialPrice {

        @Test
        void returnsProblemDetailWithErrorCode() throws Exception {
            mockMvc.perform(get("/api/stub/tariff/invalid-special-price"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("tariff.special.price_invalid"))
                    .andExpect(jsonPath("$.correlationId").exists());
        }
    }

    @RestController
    @RequestMapping("/api/stub/tariff")
    static class StubController {

        @GetMapping("/suitable-not-found")
        void suitableTariffNotFound() {
            throw new SuitableTariffNotFoundException("BICYCLE", LocalDate.of(2025, 1, 1), null);
        }

        @GetMapping("/invalid-pricing")
        void invalidPricing() {
            throw new InvalidTariffPricingException("Pricing is invalid", InvalidTariffPricingException.ERROR_CODE);
        }

        @GetMapping("/invalid-special-type")
        void invalidSpecialType() {
            throw new InvalidSpecialTariffTypeException(10L, PricingType.FLAT_HOURLY);
        }

        @GetMapping("/invalid-special-price")
        void invalidSpecialPrice() {
            throw new InvalidSpecialPriceException();
        }
    }
}
