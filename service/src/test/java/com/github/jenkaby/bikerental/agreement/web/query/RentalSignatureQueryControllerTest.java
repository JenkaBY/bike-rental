package com.github.jenkaby.bikerental.agreement.web.query;

import com.github.jenkaby.bikerental.agreement.application.usecase.FindRentalSignaturesUseCase;
import com.github.jenkaby.bikerental.agreement.application.usecase.GetSignaturePdfUseCase;
import com.github.jenkaby.bikerental.agreement.domain.model.AgreementSignature;
import com.github.jenkaby.bikerental.agreement.domain.model.AgreementSignatureSummary;
import com.github.jenkaby.bikerental.agreement.web.mapper.SignatureWebMapper;
import com.github.jenkaby.bikerental.agreement.web.query.dto.SignatureSummaryResponse;
import com.github.jenkaby.bikerental.shared.exception.ResourceNotFoundException;
import com.github.jenkaby.bikerental.support.web.ApiTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ApiTest(controllers = RentalSignatureQueryController.class)
class RentalSignatureQueryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FindRentalSignaturesUseCase findRentalSignaturesUseCase;

    @MockitoBean
    private GetSignaturePdfUseCase getSignaturePdfUseCase;

    @MockitoBean
    private SignatureWebMapper mapper;

    @Nested
    class ListSignatures {

        @Test
        void shouldReturn200AndEmptyListWhenUnsigned() throws Exception {
            given(findRentalSignaturesUseCase.execute(1L)).willReturn(List.of());
            given(mapper.toResponses(any())).willReturn(List.of());

            mockMvc.perform(get("/api/rentals/{rentalId}/signatures", 1L)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(0));
        }

        @Test
        void shouldReturn200AndOneItemWhenSigned() throws Exception {
            var summary = new AgreementSignatureSummary(10L, 5L, 3, Instant.parse("2026-07-04T12:34:56Z"));
            var response = new SignatureSummaryResponse(10L, 5L, 3, Instant.parse("2026-07-04T12:34:56Z"));
            given(findRentalSignaturesUseCase.execute(1L)).willReturn(List.of(summary));
            given(mapper.toResponses(List.of(summary))).willReturn(List.of(response));

            mockMvc.perform(get("/api/rentals/{rentalId}/signatures", 1L)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].signatureId").value(10))
                    .andExpect(jsonPath("$[0].templateId").value(5))
                    .andExpect(jsonPath("$[0].templateVersionNumber").value(3));
        }
    }

    @Nested
    class DownloadPdf {

        @Test
        void shouldReturn200PdfWithAttachmentHeaderWhenSigned() throws Exception {
            given(getSignaturePdfUseCase.execute(1L)).willReturn("%PDF-1.7 fake".getBytes());

            mockMvc.perform(get("/api/rentals/{rentalId}/signatures", 1L)
                            .accept(MediaType.APPLICATION_PDF))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                    .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"rental-1-agreement.pdf\""));
        }

        @Test
        void shouldReturn404PdfWhenUnsigned() throws Exception {
            given(getSignaturePdfUseCase.execute(eq(1L)))
                    .willThrow(new ResourceNotFoundException(AgreementSignature.class, "1"));

            mockMvc.perform(get("/api/rentals/{rentalId}/signatures", 1L)
                            .accept(MediaType.APPLICATION_PDF))
                    .andExpect(status().isNotFound());
        }
    }
}
