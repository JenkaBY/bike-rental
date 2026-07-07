package com.github.jenkaby.bikerental.agreement.web.query;

import com.github.jenkaby.bikerental.agreement.application.usecase.FindRentalAgreementUseCase;
import com.github.jenkaby.bikerental.agreement.domain.model.RentalAgreementView;
import com.github.jenkaby.bikerental.agreement.web.mapper.AgreementTemplateWebMapper;
import com.github.jenkaby.bikerental.agreement.web.query.dto.RentalAgreementResponse;
import com.github.jenkaby.bikerental.support.web.ApiTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ApiTest(controllers = RentalAgreementQueryController.class)
class RentalAgreementQueryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FindRentalAgreementUseCase findRentalAgreementUseCase;

    @MockitoBean
    private AgreementTemplateWebMapper mapper;

    @Nested
    class GetRentalAgreement {

        @Test
        void shouldReturn200WithRenderedAgreement() throws Exception {
            var view = new RentalAgreementView(5L, 3, "Rental Agreement", "Dear Alex Johnson");
            var response = new RentalAgreementResponse(5L, 3, "Rental Agreement", "Dear Alex Johnson");
            given(findRentalAgreementUseCase.execute(1L)).willReturn(view);
            given(mapper.toRentalAgreementResponse(view)).willReturn(response);

            mockMvc.perform(get("/api/rentals/{rentalId}/agreement", 1L))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.templateId").value(5))
                    .andExpect(jsonPath("$.versionNumber").value(3))
                    .andExpect(jsonPath("$.title").value("Rental Agreement"))
                    .andExpect(jsonPath("$.content").value("Dear Alex Johnson"));

            verify(findRentalAgreementUseCase).execute(1L);
        }

        @ParameterizedTest
        @ValueSource(longs = {0L, -1L})
        void shouldReturn400WhenRentalIdIsNotPositive(long rentalId) throws Exception {
            mockMvc.perform(get("/api/rentals/{rentalId}/agreement", rentalId))
                    .andExpect(status().isBadRequest());

            verify(findRentalAgreementUseCase, never()).execute(any());
        }

        @Test
        void shouldReturn400WhenRentalIdIsNotNumeric() throws Exception {
            mockMvc.perform(get("/api/rentals/{rentalId}/agreement", "not-a-number"))
                    .andExpect(status().isBadRequest());

            verify(findRentalAgreementUseCase, never()).execute(any());
        }
    }
}
