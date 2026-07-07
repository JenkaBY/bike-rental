package com.github.jenkaby.bikerental.agreement.web.query;

import com.github.jenkaby.bikerental.agreement.application.usecase.FindAgreementTemplateSummariesUseCase;
import com.github.jenkaby.bikerental.agreement.application.usecase.FindAgreementTemplateVariablesUseCase;
import com.github.jenkaby.bikerental.agreement.application.usecase.GetActiveAgreementTemplateUseCase;
import com.github.jenkaby.bikerental.agreement.application.usecase.GetAgreementTemplateUseCase;
import com.github.jenkaby.bikerental.agreement.domain.model.AgreementTemplate;
import com.github.jenkaby.bikerental.agreement.web.mapper.AgreementTemplateWebMapper;
import com.github.jenkaby.bikerental.agreement.web.query.dto.AgreementTemplateResponse;
import com.github.jenkaby.bikerental.support.web.ApiTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ApiTest(controllers = AgreementTemplateQueryController.class)
class AgreementTemplateQueryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FindAgreementTemplateSummariesUseCase findAgreementTemplateSummariesUseCase;

    @MockitoBean
    private GetActiveAgreementTemplateUseCase getActiveAgreementTemplateUseCase;

    @MockitoBean
    private GetAgreementTemplateUseCase getAgreementTemplateUseCase;

    @MockitoBean
    private FindAgreementTemplateVariablesUseCase findAgreementTemplateVariablesUseCase;

    @MockitoBean
    private AgreementTemplateWebMapper mapper;

    @Nested
    class GetAgreements {

        @Test
        void shouldReturn200WithSummaryList() throws Exception {
            given(findAgreementTemplateSummariesUseCase.execute()).willReturn(List.of());
            given(mapper.toSummaryResponses(any())).willReturn(List.of());

            mockMvc.perform(get("/api/agreements"))
                    .andExpect(status().isOk());

            verify(findAgreementTemplateSummariesUseCase).execute();
        }
    }

    @Nested
    class GetActiveAgreement {

        @Test
        void shouldReturn200AndResolveActiveRouteNotIdRoute() throws Exception {
            given(getActiveAgreementTemplateUseCase.execute()).willReturn(mock(AgreementTemplate.class));
            given(mapper.toResponse(any(AgreementTemplate.class))).willReturn(mock(AgreementTemplateResponse.class));

            mockMvc.perform(get("/api/agreements/active"))
                    .andExpect(status().isOk());

            verify(getActiveAgreementTemplateUseCase).execute();
            verify(getAgreementTemplateUseCase, never()).execute(any());
        }
    }

    @Nested
    class GetTemplateVariables {

        @Test
        void shouldReturn200WithVariableCatalog() throws Exception {
            given(findAgreementTemplateVariablesUseCase.execute()).willReturn(List.of());
            given(mapper.toVariableResponses(any())).willReturn(List.of());

            mockMvc.perform(get("/api/agreements/variables"))
                    .andExpect(status().isOk());

            verify(findAgreementTemplateVariablesUseCase).execute();
            verify(getAgreementTemplateUseCase, never()).execute(any());
        }
    }

    @Nested
    class GetAgreementById {

        @Test
        void shouldReturn200WhenIdIsValid() throws Exception {
            given(getAgreementTemplateUseCase.execute(any(Long.class))).willReturn(mock(AgreementTemplate.class));
            given(mapper.toResponse(any(AgreementTemplate.class))).willReturn(mock(AgreementTemplateResponse.class));

            mockMvc.perform(get("/api/agreements/{id}", 7L))
                    .andExpect(status().isOk());

            verify(getAgreementTemplateUseCase).execute(7L);
        }

        @ParameterizedTest
        @ValueSource(longs = {0L, -1L})
        void shouldReturn400WhenIdIsNotPositive(long id) throws Exception {
            mockMvc.perform(get("/api/agreements/{id}", id))
                    .andExpect(status().isBadRequest());

            verify(getAgreementTemplateUseCase, never()).execute(any());
        }

        @Test
        void shouldReturn400WhenIdIsNotNumeric() throws Exception {
            mockMvc.perform(get("/api/agreements/{id}", "not-a-number"))
                    .andExpect(status().isBadRequest());

            verify(getAgreementTemplateUseCase, never()).execute(any());
        }
    }
}
