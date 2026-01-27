package com.github.jenkaby.bikerental.customer.application.service;

import com.github.jenkaby.bikerental.customer.application.usecase.CreateCustomerUseCase;
import com.github.jenkaby.bikerental.customer.domain.exception.DuplicatePhoneException;
import com.github.jenkaby.bikerental.customer.domain.model.Customer;
import com.github.jenkaby.bikerental.customer.domain.model.vo.EmailAddress;
import com.github.jenkaby.bikerental.customer.domain.model.vo.PhoneNumber;
import com.github.jenkaby.bikerental.customer.domain.repository.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class CreateCustomerServiceTest {

    @Mock
    private CustomerRepository repository;
    @InjectMocks
    private CreateCustomerService service;

    @Test
    void shouldCreateCustomerSuccessfully() {
        CreateCustomerUseCase.CreateCustomerCommand command = new CreateCustomerUseCase.CreateCustomerCommand(
                "+7 (999) 888-77-66",
                "John",
                "Doe",
                "john.doe@example.com",
                LocalDate.of(1990, 1, 15)
        );
        UUID expectedId = UUID.randomUUID();
        given(repository.existsByPhone("+79998887766")).willReturn(false);
        given(repository.save(any(Customer.class))).willAnswer(invocation -> {
            Customer customer = invocation.getArgument(0);
            customer.setId(expectedId);
            return customer;
        });
        Customer result = service.execute(command);
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(expectedId);
        assertThat(result.getPhone().value()).isEqualTo("+79998887766");
        assertThat(result.getFirstName()).isEqualTo("John");
        assertThat(result.getLastName()).isEqualTo("Doe");
        assertThat(result.getEmail().value()).isEqualTo("john.doe@example.com");
        assertThat(result.getBirthDate()).isEqualTo(LocalDate.of(1990, 1, 15));
        then(repository).should().existsByPhone("+79998887766");
        then(repository).should().save(any(Customer.class));
    }

    @Test
    void shouldNormalizePhoneNumberBeforeCheckingDuplicate() {
        CreateCustomerUseCase.CreateCustomerCommand command = new CreateCustomerUseCase.CreateCustomerCommand(
                "+7 (999) 888-77-66",
                "John",
                "Doe",
                null,
                null
        );
        given(repository.existsByPhone("+79998887766")).willReturn(false);
        given(repository.save(any(Customer.class))).willAnswer(invocation -> invocation.getArgument(0));
        service.execute(command);
        then(repository).should().existsByPhone("+79998887766");
    }

    @Test
    void shouldThrowExceptionWhenPhoneAlreadyExists() {
        CreateCustomerUseCase.CreateCustomerCommand command = new CreateCustomerUseCase.CreateCustomerCommand(
                "+7 (999) 888-77-66",
                "John",
                "Doe",
                null,
                null
        );
        given(repository.existsByPhone("+79998887766")).willReturn(true);
        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(DuplicatePhoneException.class)
                .hasMessageContaining("Customer")
                .hasMessageContaining("+79998887766");
        then(repository).should().existsByPhone("+79998887766");
        then(repository).should(never()).save(any(Customer.class));
    }

    @Test
    void shouldCreateCustomerWithMinimalDataOnlyPhone() {
        CreateCustomerUseCase.CreateCustomerCommand command = new CreateCustomerUseCase.CreateCustomerCommand(
                "+79998887766",
                "John",
                "Doe",
                null,
                null
        );
        given(repository.existsByPhone("+79998887766")).willReturn(false);
        given(repository.save(any(Customer.class))).willAnswer(invocation -> invocation.getArgument(0));

        Customer result = service.execute(command);

        assertThat(result).isNotNull();
        assertThat(result.getPhone().value()).isEqualTo("+79998887766");
        assertThat(result.getFirstName()).isEqualTo("John");
        assertThat(result.getLastName()).isEqualTo("Doe");
        assertThat(result.getEmail().value()).isNull();
        assertThat(result.getBirthDate()).isNull();
    }

    @Test
    void shouldCreateCustomerWithEmailAsOptionalField() {
        CreateCustomerUseCase.CreateCustomerCommand command = new CreateCustomerUseCase.CreateCustomerCommand(
                "+79998887766",
                "John",
                "Doe",
                "john@example.com",
                null
        );
        given(repository.existsByPhone("+79998887766")).willReturn(false);
        given(repository.save(any(Customer.class))).willAnswer(invocation -> invocation.getArgument(0));
        Customer result = service.execute(command);
        assertThat(result).isNotNull();
        assertThat(result.getEmail().value()).isEqualTo("john@example.com");
    }

    @Test
    void shouldCreateCustomerWithBirthDateAsOptionalField() {
        CreateCustomerUseCase.CreateCustomerCommand command = new CreateCustomerUseCase.CreateCustomerCommand(
                "+79998887766",
                "John",
                "Doe",
                null,
                LocalDate.of(1995, 5, 20)
        );
        given(repository.existsByPhone("+79998887766")).willReturn(false);
        given(repository.save(any(Customer.class))).willAnswer(invocation -> invocation.getArgument(0));
        Customer result = service.execute(command);
        assertThat(result).isNotNull();
        assertThat(result.getBirthDate()).isEqualTo(LocalDate.of(1995, 5, 20));
    }

    @Test
    void shouldSaveCustomerWithCorrectPhoneNumber() {
        CreateCustomerUseCase.CreateCustomerCommand command = new CreateCustomerUseCase.CreateCustomerCommand(
                "+7 (999) 888-77-66",
                "John",
                "Doe",
                null,
                null
        );
        given(repository.existsByPhone("+79998887766")).willReturn(false);
        given(repository.save(any(Customer.class))).willAnswer(invocation -> invocation.getArgument(0));
        ArgumentCaptor<Customer> customerCaptor = ArgumentCaptor.forClass(Customer.class);
        service.execute(command);
        then(repository).should().save(customerCaptor.capture());
        Customer savedCustomer = customerCaptor.getValue();
        assertThat(savedCustomer.getPhone()).isEqualTo(new PhoneNumber("+79998887766"));
    }

    @Test
    void shouldSaveCustomerWithCorrectEmail() {
        CreateCustomerUseCase.CreateCustomerCommand command = new CreateCustomerUseCase.CreateCustomerCommand(
                "+79998887766",
                "John",
                "Doe",
                "test@example.com",
                null
        );
        given(repository.existsByPhone("+79998887766")).willReturn(false);
        given(repository.save(any(Customer.class))).willAnswer(invocation -> invocation.getArgument(0));
        ArgumentCaptor<Customer> customerCaptor = ArgumentCaptor.forClass(Customer.class);
        service.execute(command);
        then(repository).should().save(customerCaptor.capture());
        Customer savedCustomer = customerCaptor.getValue();
        assertThat(savedCustomer.getEmail()).isEqualTo(new EmailAddress("test@example.com"));
    }

    @Test
    void shouldThrowExceptionWhenPhoneIsInvalid() {
        CreateCustomerUseCase.CreateCustomerCommand command = new CreateCustomerUseCase.CreateCustomerCommand(
                "",
                "John",
                "Doe",
                null,
                null
        );
        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Phone number cannot be empty");
        then(repository).should(never()).existsByPhone(any());
        then(repository).should(never()).save(any(Customer.class));
    }

    @Test
    void shouldThrowExceptionWhenEmailIsInvalid() {
        CreateCustomerUseCase.CreateCustomerCommand command = new CreateCustomerUseCase.CreateCustomerCommand(
                "+79998887766",
                "John",
                "Doe",
                "invalid-email",
                null
        );
        given(repository.existsByPhone("+79998887766")).willReturn(false);
        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid email format");
        then(repository).should().existsByPhone("+79998887766");
        then(repository).should(never()).save(any(Customer.class));
    }
}
