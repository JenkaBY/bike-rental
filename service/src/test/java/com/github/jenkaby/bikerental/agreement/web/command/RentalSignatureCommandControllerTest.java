package com.github.jenkaby.bikerental.agreement.web.command;

import com.github.jenkaby.bikerental.agreement.application.usecase.SignAgreementUseCase;
import com.github.jenkaby.bikerental.agreement.web.command.dto.SignAgreementRequest;
import com.github.jenkaby.bikerental.support.web.ApiTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ApiTest(controllers = RentalSignatureCommandController.class)
class RentalSignatureCommandControllerTest {

    private static final String VALID_PNG = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SignAgreementUseCase signAgreementUseCase;

    private static SignAgreementRequest validRequest() {
        return new SignAgreementRequest(VALID_PNG, 1L, 5L, "op-1");
    }

    @Nested
    class PostSignature {

        @Test
        void shouldReturn201WhenRequestIsValid() throws Exception {
            given(signAgreementUseCase.execute(any(SignAgreementUseCase.SignAgreementCommand.class)))
                    .willReturn(new SignAgreementUseCase.SignAgreementResult(10L, Instant.parse("2026-07-04T12:34:56Z")));

            mockMvc.perform(post("/api/rentals/{rentalId}/signatures", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest())))
                    .andExpect(status().isCreated());

            verify(signAgreementUseCase).execute(any(SignAgreementUseCase.SignAgreementCommand.class));
        }

        @ParameterizedTest
        @ValueSource(strings = {"   ", "\t"})
        @NullAndEmptySource
        void shouldReturn400WhenSignaturePngIsBlank(String signaturePng) throws Exception {
            var request = new SignAgreementRequest(signaturePng, 1L, 5L, "op-1");

            mockMvc.perform(post("/api/rentals/{rentalId}/signatures", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verify(signAgreementUseCase, never()).execute(any());
        }

        @ParameterizedTest
        @ValueSource(strings = {"   ", "\t"})
        @NullAndEmptySource
        void shouldReturn400WhenOperatorIdIsBlank(String operatorId) throws Exception {
            var request = new SignAgreementRequest(VALID_PNG, 1L, 5L, operatorId);

            mockMvc.perform(post("/api/rentals/{rentalId}/signatures", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verify(signAgreementUseCase, never()).execute(any());
        }

        @Test
        void shouldReturn400WhenRentalVersionIsNull() throws Exception {
            var request = new SignAgreementRequest(VALID_PNG, null, 5L, "op-1");

            mockMvc.perform(post("/api/rentals/{rentalId}/signatures", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verify(signAgreementUseCase, never()).execute(any());
        }

        @Test
        void shouldReturn400WhenRentalVersionIsNegative() throws Exception {
            var request = new SignAgreementRequest(VALID_PNG, -1L, 5L, "op-1");

            mockMvc.perform(post("/api/rentals/{rentalId}/signatures", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verify(signAgreementUseCase, never()).execute(any());
        }

        @Test
        void shouldReturn400WhenTemplateIdIsNull() throws Exception {
            var request = new SignAgreementRequest(VALID_PNG, 1L, null, "op-1");

            mockMvc.perform(post("/api/rentals/{rentalId}/signatures", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verify(signAgreementUseCase, never()).execute(any());
        }

        @Test
        void shouldReturn400WhenTemplateIdIsNotPositive() throws Exception {
            var request = new SignAgreementRequest(VALID_PNG, 1L, 0L, "op-1");

            mockMvc.perform(post("/api/rentals/{rentalId}/signatures", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verify(signAgreementUseCase, never()).execute(any());
        }
    }

    @Nested
    class IpResolution {

        @Test
        void shouldUseFirstForwardedForTokenAsIpAddress() throws Exception {
            given(signAgreementUseCase.execute(any(SignAgreementUseCase.SignAgreementCommand.class)))
                    .willReturn(new SignAgreementUseCase.SignAgreementResult(10L, Instant.parse("2026-07-04T12:34:56Z")));

            mockMvc.perform(post("/api/rentals/{rentalId}/signatures", 1L)
                            .header("X-Forwarded-For", "203.0.113.7, 70.41.3.18, 150.172.238.178")
                            .header("User-Agent", "JUnit-Agent")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest())))
                    .andExpect(status().isCreated());

            var captor = ArgumentCaptor.forClass(SignAgreementUseCase.SignAgreementCommand.class);
            verify(signAgreementUseCase).execute(captor.capture());
            var actual = captor.getValue();
            assertThat(actual.ipAddress()).as("Resolved ip address").isEqualTo("203.0.113.7");
            assertThat(actual.userAgent()).as("Resolved user agent").isEqualTo("JUnit-Agent");
        }

        @Test
        void shouldFallBackToRemoteAddressWhenForwardedForAbsent() throws Exception {
            given(signAgreementUseCase.execute(any(SignAgreementUseCase.SignAgreementCommand.class)))
                    .willReturn(new SignAgreementUseCase.SignAgreementResult(10L, Instant.parse("2026-07-04T12:34:56Z")));

            mockMvc.perform(post("/api/rentals/{rentalId}/signatures", 1L)
                            .with(request -> {
                                request.setRemoteAddr("198.51.100.5");
                                return request;
                            })
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest())))
                    .andExpect(status().isCreated());

            var captor = ArgumentCaptor.forClass(SignAgreementUseCase.SignAgreementCommand.class);
            verify(signAgreementUseCase).execute(captor.capture());
            assertThat(captor.getValue().ipAddress()).as("Fallback ip address").isEqualTo("198.51.100.5");
        }
    }
}
