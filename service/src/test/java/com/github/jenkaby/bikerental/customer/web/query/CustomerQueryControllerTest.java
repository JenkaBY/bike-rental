package com.github.jenkaby.bikerental.customer.web.query;

import com.github.jenkaby.bikerental.customer.CustomerInfo;
import com.github.jenkaby.bikerental.customer.application.usecase.CustomerQueryUseCase;
import com.github.jenkaby.bikerental.customer.application.usecase.GetCustomerByIdUseCase;
import com.github.jenkaby.bikerental.customer.domain.model.Customer;
import com.github.jenkaby.bikerental.customer.domain.model.vo.PhoneNumber;
import com.github.jenkaby.bikerental.customer.web.mapper.CustomerWebMapper;
import com.github.jenkaby.bikerental.customer.web.query.dto.CustomerResponse;
import com.github.jenkaby.bikerental.customer.web.query.dto.CustomerSearchResponse;
import com.github.jenkaby.bikerental.shared.exception.ResourceNotFoundException;
import com.github.jenkaby.bikerental.support.web.ApiTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ApiTest(controllers = CustomerQueryController.class)
class CustomerQueryControllerTest {

    public static final String API_CUSTOMERS = "/api/customers";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CustomerQueryUseCase customerQueryUseCase;

    @MockitoBean
    private GetCustomerByIdUseCase getCustomerById;

    @MockitoBean
    private CustomerWebMapper mapper;

    @Nested
    class GetCustomersSearch {

        @Test
        void shouldReturn200WhenSearchIsValid() throws Exception {
            String phone = "1234";
            var customerInfo = new CustomerInfo(UUID.randomUUID(), "+79991234001", "Alex", "Doe", null, null);
            var response = new CustomerSearchResponse(customerInfo.id(), customerInfo.phone(), customerInfo.firstName(), customerInfo.lastName());
            given(customerQueryUseCase.searchByPhone(phone)).willReturn(List.of(customerInfo));
            given(mapper.toSearchResponses(List.of(customerInfo))).willReturn(List.of(response));

            mockMvc.perform(get(API_CUSTOMERS).queryParam("phone", phone))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(customerInfo.id().toString()))
                    .andExpect(jsonPath("$[0].phone").value("+79991234001"))
                    .andExpect(jsonPath("$[0].firstName").value("Alex"))
                    .andExpect(jsonPath("$[0].lastName").value("Doe"));

            verify(customerQueryUseCase).searchByPhone(phone);
        }

        @ParameterizedTest
        @ValueSource(strings = {"123", "123456789012", "12a4", "+1234"})
        void shouldReturn400WhenPhoneIsInvalid(String phone) throws Exception {
            mockMvc.perform(get(API_CUSTOMERS).queryParam("phone", phone))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.title").value("Bad Request"))
                    .andExpect(jsonPath("$.errors[0].code").value("validation.pattern"));
        }

        @ParameterizedTest
        @ValueSource(strings = {"", " ", "\t"})
        void shouldReturn400WhenPhoneIsBlank(String phone) throws Exception {
            mockMvc.perform(get(API_CUSTOMERS).queryParam("phone", phone))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.title").value("Bad Request"))
                    .andExpect(jsonPath("$.errors[0].code").value("validation.pattern"));
        }
    }

    @Nested
    class GetCustomerById {

        @Test
        void shouldReturn200WhenCustomerExists() throws Exception {
            var id = UUID.randomUUID();
            var cust = Customer.builder()
                    .id(id)
                    .phone(new PhoneNumber("+79991234001"))
                    .firstName("Alex")
                    .lastName("Doe")
                    .email(null)
                    .birthDate(null)
                    .comments(null)
                    .build();
            var response = new CustomerResponse(cust.getId(), cust.getPhone().value(), cust.getFirstName(), cust.getLastName(), null, null, null);

            given(getCustomerById.getById(id)).willReturn(cust);
            given(mapper.toResponse(cust)).willReturn(response);

            mockMvc.perform(get(API_CUSTOMERS + "/{id}", id.toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(id.toString()))
                    .andExpect(jsonPath("$.phone").value("+79991234001"))
                    .andExpect(jsonPath("$.firstName").value("Alex"));
        }

        @Test
        void shouldReturn404WhenNotFound() throws Exception {
            var id = UUID.randomUUID();
            given(getCustomerById.getById(id)).willThrow(new ResourceNotFoundException("Customer", id.toString()));

            mockMvc.perform(get(API_CUSTOMERS + "/{id}", id.toString()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorCode").value("shared.resource.not_found"));
        }

        @ParameterizedTest
        @ValueSource(strings = {"not-a-uuid", "123"})
        void shouldReturn400ForInvalidUuid(String path) throws Exception {
            mockMvc.perform(get(API_CUSTOMERS + "/{id}", path))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.title").value("Bad Request"));
        }
    }
}
