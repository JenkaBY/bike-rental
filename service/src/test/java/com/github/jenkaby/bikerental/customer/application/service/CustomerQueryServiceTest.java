package com.github.jenkaby.bikerental.customer.application.service;

import com.github.jenkaby.bikerental.customer.CustomerInfo;
import com.github.jenkaby.bikerental.customer.application.config.CustomerSearchProperties;
import com.github.jenkaby.bikerental.customer.application.mapper.CustomerMapper;
import com.github.jenkaby.bikerental.customer.domain.model.Customer;
import com.github.jenkaby.bikerental.customer.domain.model.vo.PhoneNumber;
import com.github.jenkaby.bikerental.customer.domain.repository.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class CustomerQueryServiceTest {

    @Mock
    private CustomerRepository repository;
    @Mock
    private CustomerMapper mapper;

    @Test
    void shouldNormalizePhoneAndApplyLimit() {
        var properties = new CustomerSearchProperties(7);
        var service = new CustomerQueryService(repository, mapper, properties);
        given(repository.searchByPhone("+79991234567", 7)).willReturn(List.of());

        service.searchByPhone("+7 (999) 123-45-67");

        ArgumentCaptor<String> phoneCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Integer> limitCaptor = ArgumentCaptor.forClass(Integer.class);
        then(repository).should().searchByPhone(phoneCaptor.capture(), limitCaptor.capture());
        assertThat(phoneCaptor.getValue()).isEqualTo("+79991234567");
        assertThat(limitCaptor.getValue()).isEqualTo(7);
    }

    @Test
    void shouldMapSearchResultsToCustomerInfo() {
        var properties = new CustomerSearchProperties(10);
        var service = new CustomerQueryService(repository, mapper, properties);
        var customer = Customer.builder()
                .id(UUID.fromString("11111111-1111-1111-1111-111111111111"))
                .phone(new PhoneNumber("+79991234001"))
                .firstName("Alex")
                .lastName("Doe")
                .build();
        var customerInfo = new CustomerInfo(customer.getId(), customer.getPhone().value(), customer.getFirstName(), customer.getLastName(), null, null);
        given(repository.searchByPhone("1234", 10)).willReturn(List.of(customer));
        given(mapper.toInfo(customer)).willReturn(customerInfo);

        List<CustomerInfo> result = service.searchByPhone("1234");

        assertThat(result).containsExactly(customerInfo);
    }
}
