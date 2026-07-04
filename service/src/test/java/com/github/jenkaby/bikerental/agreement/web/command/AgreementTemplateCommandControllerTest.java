package com.github.jenkaby.bikerental.agreement.web.command;

import com.github.jenkaby.bikerental.agreement.application.usecase.ActivateAgreementTemplateUseCase;
import com.github.jenkaby.bikerental.agreement.application.usecase.CreateAgreementTemplateUseCase;
import com.github.jenkaby.bikerental.agreement.application.usecase.DeleteAgreementTemplateUseCase;
import com.github.jenkaby.bikerental.agreement.application.usecase.PreviewAgreementPdfUseCase;
import com.github.jenkaby.bikerental.agreement.application.usecase.UpdateAgreementTemplateUseCase;
import com.github.jenkaby.bikerental.agreement.domain.model.AgreementTemplate;
import com.github.jenkaby.bikerental.agreement.web.command.dto.AgreementPdfPreviewRequest;
import com.github.jenkaby.bikerental.agreement.web.command.dto.AgreementTemplateRequest;
import com.github.jenkaby.bikerental.agreement.web.mapper.AgreementTemplateWebMapper;
import com.github.jenkaby.bikerental.agreement.web.query.dto.AgreementTemplateResponse;
import com.github.jenkaby.bikerental.support.web.ApiTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ApiTest(controllers = AgreementTemplateCommandController.class)
class AgreementTemplateCommandControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CreateAgreementTemplateUseCase createAgreementTemplateUseCase;

    @MockitoBean
    private UpdateAgreementTemplateUseCase updateAgreementTemplateUseCase;

    @MockitoBean
    private ActivateAgreementTemplateUseCase activateAgreementTemplateUseCase;

    @MockitoBean
    private DeleteAgreementTemplateUseCase deleteAgreementTemplateUseCase;

    @MockitoBean
    private PreviewAgreementPdfUseCase previewAgreementPdfUseCase;

    @MockitoBean
    private AgreementTemplateWebMapper mapper;

    private static final String OVERSIZED_TITLE = "a".repeat(256);

    @Nested
    class PostAgreements {

        @Test
        void shouldReturn201WhenRequestIsValid() throws Exception {
            var request = new AgreementTemplateRequest("Rental Agreement", "You agree to the terms.");
            given(createAgreementTemplateUseCase.execute(any(CreateAgreementTemplateUseCase.CreateAgreementTemplateCommand.class)))
                    .willReturn(mock(AgreementTemplate.class));
            given(mapper.toResponse(any(AgreementTemplate.class)))
                    .willReturn(mock(AgreementTemplateResponse.class));

            mockMvc.perform(post("/api/agreements")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());

            verify(createAgreementTemplateUseCase).execute(any(CreateAgreementTemplateUseCase.CreateAgreementTemplateCommand.class));
        }

        @ParameterizedTest
        @ValueSource(strings = {"   ", "\t"})
        @NullAndEmptySource
        void shouldReturn400WhenTitleIsBlank(String title) throws Exception {
            var request = new AgreementTemplateRequest(title, "valid content");

            mockMvc.perform(post("/api/agreements")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors[0].field").value(containsString("title")));

            verify(createAgreementTemplateUseCase, never()).execute(any());
        }

        @Test
        void shouldReturn400WhenTitleExceeds255Chars() throws Exception {
            var request = new AgreementTemplateRequest(OVERSIZED_TITLE, "valid content");

            mockMvc.perform(post("/api/agreements")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors[0].field").value(containsString("title")))
                    .andExpect(jsonPath("$.errors[0].code").value(containsString("size")));

            verify(createAgreementTemplateUseCase, never()).execute(any());
        }

        @ParameterizedTest
        @ValueSource(strings = {"   ", "\t"})
        @NullAndEmptySource
        void shouldReturn400WhenContentIsBlank(String content) throws Exception {
            var request = new AgreementTemplateRequest("valid title", content);

            mockMvc.perform(post("/api/agreements")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors[0].field").value(containsString("content")));

            verify(createAgreementTemplateUseCase, never()).execute(any());
        }
    }

    @Nested
    class PatchAgreements {

        @Test
        void shouldReturn200WhenRequestIsValid() throws Exception {
            var request = new AgreementTemplateRequest("Updated title", "Updated content");
            given(updateAgreementTemplateUseCase.execute(any(UpdateAgreementTemplateUseCase.UpdateAgreementTemplateCommand.class)))
                    .willReturn(mock(AgreementTemplate.class));
            given(mapper.toResponse(any(AgreementTemplate.class)))
                    .willReturn(mock(AgreementTemplateResponse.class));

            mockMvc.perform(patch("/api/agreements/{id}", 5L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            verify(updateAgreementTemplateUseCase).execute(any(UpdateAgreementTemplateUseCase.UpdateAgreementTemplateCommand.class));
        }

        @ParameterizedTest
        @ValueSource(strings = {"   ", "\t"})
        @NullAndEmptySource
        void shouldReturn400WhenTitleIsBlank(String title) throws Exception {
            var request = new AgreementTemplateRequest(title, "valid content");

            mockMvc.perform(patch("/api/agreements/{id}", 5L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors[0].field").value(containsString("title")));

            verify(updateAgreementTemplateUseCase, never()).execute(any());
        }

        @Test
        void shouldReturn400WhenContentIsBlank() throws Exception {
            var request = new AgreementTemplateRequest("valid title", "");

            mockMvc.perform(patch("/api/agreements/{id}", 5L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors[0].field").value(containsString("content")));

            verify(updateAgreementTemplateUseCase, never()).execute(any());
        }
    }

    @Nested
    class ActivateAgreement {

        @Test
        void shouldReturn200WhenActivated() throws Exception {
            given(activateAgreementTemplateUseCase.execute(any(Long.class)))
                    .willReturn(mock(AgreementTemplate.class));
            given(mapper.toResponse(any(AgreementTemplate.class)))
                    .willReturn(mock(AgreementTemplateResponse.class));

            mockMvc.perform(patch("/api/agreements/{id}/activate", 5L))
                    .andExpect(status().isOk());

            verify(activateAgreementTemplateUseCase).execute(5L);
        }
    }

    @Nested
    class DeleteAgreement {

        @Test
        void shouldReturn204WhenDeleted() throws Exception {
            mockMvc.perform(delete("/api/agreements/{id}", 5L))
                    .andExpect(status().isNoContent());

            verify(deleteAgreementTemplateUseCase).execute(5L);
        }
    }

    @Nested
    class PreviewPdf {

        @Test
        void shouldReturn200AndPdfWhenRequestIsValid() throws Exception {
            var request = new AgreementPdfPreviewRequest("Rental Agreement", "Вы соглашаетесь с условиями.");
            byte[] expected = "%PDF-1.7 fake".getBytes();
            given(previewAgreementPdfUseCase.execute(any(PreviewAgreementPdfUseCase.PreviewAgreementPdfCommand.class)))
                    .willReturn(expected);

            mockMvc.perform(post("/api/agreements/preview")
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_PDF)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_PDF));

            verify(previewAgreementPdfUseCase).execute(any(PreviewAgreementPdfUseCase.PreviewAgreementPdfCommand.class));
        }

        @ParameterizedTest
        @ValueSource(strings = {"   ", "\t"})
        @NullAndEmptySource
        void shouldReturn400WhenTitleIsBlank(String title) throws Exception {
            var request = new AgreementPdfPreviewRequest(title, "valid content");

            mockMvc.perform(post("/api/agreements/preview")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors[0].field").value(containsString("title")));

            verify(previewAgreementPdfUseCase, never()).execute(any());
        }

        @ParameterizedTest
        @ValueSource(strings = {"   ", "\t"})
        @NullAndEmptySource
        void shouldReturn400WhenContentIsBlank(String content) throws Exception {
            var request = new AgreementPdfPreviewRequest("valid title", content);

            mockMvc.perform(post("/api/agreements/preview")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors[0].field").value(containsString("content")));

            verify(previewAgreementPdfUseCase, never()).execute(any());
        }
    }
}
