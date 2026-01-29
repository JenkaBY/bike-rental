package com.github.jenkaby.bikerental.customer.web.command;

import com.github.jenkaby.bikerental.customer.application.usecase.CreateCustomerUseCase;
import com.github.jenkaby.bikerental.customer.domain.model.Customer;
import com.github.jenkaby.bikerental.customer.web.command.dto.CustomerRequest;
import com.github.jenkaby.bikerental.customer.web.command.mapper.CustomerCommandMapper;
import com.github.jenkaby.bikerental.customer.web.query.dto.CustomerResponse;
import com.github.jenkaby.bikerental.support.web.ApiTest;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ApiTest(controllers = CustomerCommandController.class)
class CustomerCommandControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CreateCustomerUseCase createCustomerUseCase;

    @MockitoBean
    private CustomerCommandMapper mapper;

    @Nested
    class PostCustomers {

        @Nested
        class ShouldReturn201 {

            @Test
            void whenRequestContainsAllValidFields() throws Exception {
                CustomerRequest request = createValidRequest();

                configureMapperDefaults();
                given(createCustomerUseCase.execute(any(CreateCustomerUseCase.CreateCustomerCommand.class)))
                        .willReturn(mock(Customer.class));

                mockMvc.perform(post("/api/customers")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isCreated());

                verify(createCustomerUseCase).execute(any(CreateCustomerUseCase.CreateCustomerCommand.class));
            }

            @ParameterizedTest
            @CsvSource({
                    "'+1 (555) 123-4567'",
                    "'+7 (999) 888-77-66'",
                    "'8-800-555-35-35'",
                    "'+15551234567'"
            })
            void whenPhoneHasVariousFormats(String inputPhone) throws Exception {
                CustomerRequest request = createValidRequestWithRequiredFields(inputPhone);

                configureMapperDefaults();
                given(createCustomerUseCase.execute(any(CreateCustomerUseCase.CreateCustomerCommand.class)))
                        .willReturn(mock(Customer.class));

                mockMvc.perform(post("/api/customers")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isCreated());

                verify(createCustomerUseCase).execute(any(CreateCustomerUseCase.CreateCustomerCommand.class));
            }
        }

        @Nested
        class ShouldReturn400 {

            @ParameterizedTest
            @ValueSource(strings = {"   ", "\t"})
            @NullAndEmptySource
            void whenPhoneIsBlank(String phone) throws Exception {
                CustomerRequest request = createValidRequestWithRequiredFields(phone);

                mockMvc.perform(post("/api/customers")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.title").value("Bad Request"))
                        .andExpect(jsonPath("$.detail").value(containsString("Phone is required")));

                verify(createCustomerUseCase, never()).execute(any());
            }

            @ParameterizedTest
            @ValueSource(strings = {"invalid-phone", "abc", "@@##$$", "phone number", "+1.555.123.4567"})
            void whenPhoneFormatIsInvalid(String phone) throws Exception {
                CustomerRequest request = new CustomerRequest(
                        phone,
                        "John",
                        "Doe",
                        null,
                        null,
                        null
                );

                mockMvc.perform(post("/api/customers")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.title").value("Bad Request"))
                        .andExpect(jsonPath("$.detail").value(containsString("Phone format is invalid")));

                verify(createCustomerUseCase, never()).execute(any());
            }

            @ParameterizedTest
            @ValueSource(strings = {"   ", "\t"})
            @NullAndEmptySource
            void whenFirstNameIsBlank(String firstName) throws Exception {
                CustomerRequest request = new CustomerRequest(
                        "+79998887766",
                        firstName,
                        "Doe",
                        null,
                        null,
                        null
                );

                mockMvc.perform(post("/api/customers")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.title").value("Bad Request"))
                        .andExpect(jsonPath("$.detail").value(containsString("First name is required")));

                verify(createCustomerUseCase, never()).execute(any());
            }

            @ParameterizedTest
            @ValueSource(strings = {"   ", "\t"})
            @NullAndEmptySource
            void whenLastNameIsBlank(String lastName) throws Exception {
                CustomerRequest request = new CustomerRequest(
                        "+79998887766",
                        "John",
                        lastName,
                        null,
                        null,
                        null
                );

                mockMvc.perform(post("/api/customers")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.title").value("Bad Request"))
                        .andExpect(jsonPath("$.detail").value(containsString("Last name is required")));

                verify(createCustomerUseCase, never()).execute(any());
            }

            @Test
            void whenBirthDateIsInFuture() throws Exception {
                CustomerRequest request = new CustomerRequest(
                        "+79998887766",
                        "John",
                        "Doe",
                        null,
                        LocalDate.now().plusDays(1),
                        null
                );

                mockMvc.perform(post("/api/customers")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.title").value("Bad Request"))
                        .andExpect(jsonPath("$.detail").value(containsString("Birth date must be in the past")));

                verify(createCustomerUseCase, never()).execute(any());
            }

            @Test
            void whenRequestBodyIsEmpty() throws Exception {
                mockMvc.perform(post("/api/customers")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{}"))
                        .andExpect(status().isBadRequest());

                verify(createCustomerUseCase, never()).execute(any());
            }

            @Test
            void whenRequestBodyIsMalformed() throws Exception {
                mockMvc.perform(post("/api/customers")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{invalid json"))
                        .andExpect(status().isBadRequest());

                verify(createCustomerUseCase, never()).execute(any());
            }

            @Test
            void whenAllRequiredFieldsAreMissing() throws Exception {
                String requestJson = "{}";

                String response = mockMvc.perform(post("/api/customers")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.title").value("Bad Request"))
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

                assertThat(response)
                        .contains("Phone is required")
                        .contains("First name is required")
                        .contains("Last name is required");

                verify(createCustomerUseCase, never()).execute(any());
            }
        }

        private static @NonNull CustomerRequest createValidRequestWithRequiredFields(String phone) {
            return new CustomerRequest(
                    phone,
                    "Jane",
                    "Smith",
                    null,
                    null,
                    null
            );
        }

        private static @NonNull CustomerRequest createValidRequest() {
            return new CustomerRequest(
                    "+7 (999) 888-77-66",
                    "John",
                    "Doe",
                    "john.doe@example.com",
                    LocalDate.of(1990, 1, 15),
                    null
            );
        }

        private void configureMapperDefaults() {
            given(mapper.toCommand(any(CustomerRequest.class)))
                    .willReturn(mock(CreateCustomerUseCase.CreateCustomerCommand.class));
            given(mapper.toResponse(any(Customer.class)))
                    .willReturn(mock(CustomerResponse.class));
        }
    }
}
