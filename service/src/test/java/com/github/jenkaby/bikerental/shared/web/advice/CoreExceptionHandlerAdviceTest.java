package com.github.jenkaby.bikerental.shared.web.advice;

import com.github.jenkaby.bikerental.shared.web.filter.CorrelationIdFilter;
import com.github.jenkaby.bikerental.support.web.ApiTest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.*;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ApiTest(controllers = CoreExceptionHandlerAdviceTest.StubController.class)
@Import(CoreExceptionHandlerAdvice.class)
class CoreExceptionHandlerAdviceTest {

    @Autowired
    private MockMvc mockMvc;

    @Nested
    class CorrelationId {

        @Test
        void whenHeaderPresent_thenSameValueInResponseAndBody() throws Exception {
            mockMvc.perform(post("/api/stub/validate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"name\": \"\", \"label\": \"valid\"}")
                            .header(CorrelationIdFilter.HEADER_NAME, "test-correlation-123"))
                    .andExpect(status().isBadRequest())
                    .andExpect(header().string(CorrelationIdFilter.HEADER_NAME, "test-correlation-123"))
                    .andExpect(jsonPath("$.correlationId").value("test-correlation-123"));
        }

        @Test
        void whenHeaderAbsent_thenCorrelationIdGeneratedAndReturned() throws Exception {
            mockMvc.perform(post("/api/stub/validate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"name\": \"\", \"label\": \"valid\"}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(header().exists(CorrelationIdFilter.HEADER_NAME))
                    .andExpect(jsonPath("$.correlationId").value(notNullValue()));
        }

        @Test
        void responseBodyHasNoErrorId() throws Exception {
            mockMvc.perform(get("/api/stub/error"))
                    .andExpect(jsonPath("$.errorId").doesNotExist())
                    .andExpect(jsonPath("$.correlationId").exists());
        }
    }

    @Nested
    class ValidationErrors {

        @Test
        void whenBodyHasInvalidField_thenErrorsArrayIsPresent() throws Exception {
            mockMvc.perform(post("/api/stub/validate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"name\": \"\", \"label\": \"valid\"}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.detail").value("Validation error"))
                    .andExpect(jsonPath("$.errorCode").value("shared.method_arguments.validation_failed"))
                    .andExpect(jsonPath("$.errors").isArray())
                    .andExpect(jsonPath("$.errors", hasSize(1)))
                    .andExpect(jsonPath("$.errors[0].field").value("name"))
                    .andExpect(jsonPath("$.errors[0].code").value("validation.not_blank"));
        }

        @Test
        void whenBodyHasMultipleInvalidFields_thenErrorsArrayContainsAll() throws Exception {
            mockMvc.perform(post("/api/stub/validate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"name\": \"\", \"label\": \"\"}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.detail").value("Validation error"))
                    .andExpect(jsonPath("$.errors").isArray())
                    .andExpect(jsonPath("$.errors", hasSize(2)))
                    .andExpect(jsonPath("$.errors[0].field").exists())
                    .andExpect(jsonPath("$.errors[0].code").exists())
                    .andExpect(jsonPath("$.errors[1].field").exists())
                    .andExpect(jsonPath("$.errors[1].code").exists());
        }
    }

    @Nested
    class ErrorCodes {

        @Test
        void missingBody_returnsBadRequestWithErrorCode() throws Exception {
            mockMvc.perform(post("/api/stub/validate")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("shared.request.not_readable"))
                    .andExpect(jsonPath("$.correlationId").value(notNullValue()));
        }

        @Test
        void serverError_returnsInternalErrorCode() throws Exception {
            mockMvc.perform(get("/api/stub/error"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.errorCode").value("shared.server.internal_error"))
                    .andExpect(jsonPath("$.correlationId").value(notNullValue()));
        }
    }

    @RestController
    @RequestMapping("/api/stub")
    static class StubController {

        record StubRequest(@NotBlank String name, @NotBlank String label) {
        }

        @PostMapping("/validate")
        void validate(@RequestBody @Valid StubRequest request) {
        }

        @GetMapping("/error")
        void error() {
            throw new RuntimeException("unexpected");
        }
    }
}





