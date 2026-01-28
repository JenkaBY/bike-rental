package com.github.jenkaby.bikerental.customer.web.query;

import com.github.jenkaby.bikerental.customer.CustomerInfo;
import com.github.jenkaby.bikerental.customer.application.usecase.CustomerQueryUseCase;
import com.github.jenkaby.bikerental.customer.web.query.dto.CustomerSearchResponse;
import com.github.jenkaby.bikerental.customer.web.query.mapper.CustomerQueryMapper;
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

import static org.hamcrest.Matchers.containsString;
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
    private CustomerQueryMapper customerQueryMapper;

    @Nested
    class GetCustomersSearch {

        @Test
        void shouldReturn200WhenSearchIsValid() throws Exception {
            String phone = "1234";
            var customerInfo = new CustomerInfo(UUID.randomUUID(), "+79991234001", "Alex", "Doe", null, null);
            var response = new CustomerSearchResponse(customerInfo.id(), customerInfo.phone(), customerInfo.firstName(), customerInfo.lastName());
            given(customerQueryUseCase.searchByPhone(phone)).willReturn(List.of(customerInfo));
            given(customerQueryMapper.toSearchResponses(List.of(customerInfo))).willReturn(List.of(response));

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
                    .andExpect(jsonPath("$.detail").value(containsString("Phone search must be 4 to 11 digits")));
        }

        @ParameterizedTest
        @ValueSource(strings = {"", " ", "\t"})
        void shouldReturn400WhenPhoneIsBlank(String phone) throws Exception {
            mockMvc.perform(get(API_CUSTOMERS).queryParam("phone", phone))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.title").value("Bad Request"))
                    .andExpect(jsonPath("$.detail").value(containsString("Phone search must be 4 to 11 digits")));
        }
    }
}
