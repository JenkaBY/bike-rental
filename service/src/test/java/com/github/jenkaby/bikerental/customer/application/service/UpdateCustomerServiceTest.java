package com.github.jenkaby.bikerental.customer.application.service;

import com.github.jenkaby.bikerental.customer.application.mapper.CustomerCommandToDomainMapper;
import com.github.jenkaby.bikerental.customer.application.usecase.UpdateCustomerUseCase;
import com.github.jenkaby.bikerental.customer.domain.exception.DuplicatePhoneException;
import com.github.jenkaby.bikerental.customer.domain.model.Customer;
import com.github.jenkaby.bikerental.customer.domain.model.vo.EmailAddress;
import com.github.jenkaby.bikerental.customer.domain.model.vo.PhoneNumber;
import com.github.jenkaby.bikerental.customer.domain.repository.CustomerRepository;
import com.github.jenkaby.bikerental.customer.shared.mapper.PhoneNumberMapper;
import com.github.jenkaby.bikerental.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class UpdateCustomerServiceTest {

    @Mock
    private CustomerRepository repository;
    @Mock
    private CustomerCommandToDomainMapper mapper;
    @Mock
    private PhoneNumberMapper phoneMapper;
    @InjectMocks
    private UpdateCustomerService service;

    @Test
    void shouldUpdateCustomerSuccessfully() {
        UUID customerId = UUID.randomUUID();
        UpdateCustomerUseCase.UpdateCustomerCommand command = new UpdateCustomerUseCase.UpdateCustomerCommand(
                customerId,
                "+7 (999) 888-77-66",
                "John",
                "Doe",
                "john.updated@example.com",
                LocalDate.of(1990, 1, 15),
                "Updated comments"
        );

        Customer existingCustomer = Customer.builder()
                .id(customerId)
                .phone(new PhoneNumber("+79991234567"))
                .firstName("Old")
                .lastName("Name")
                .email(new EmailAddress("old@example.com"))
                .birthDate(LocalDate.of(1985, 5, 20))
                .comments("Old comments")
                .build();

        Customer updatedCustomer = Customer.builder()
                .id(customerId)
                .phone(new PhoneNumber("+79998887766"))
                .firstName("John")
                .lastName("Doe")
                .email(new EmailAddress("john.updated@example.com"))
                .birthDate(LocalDate.of(1990, 1, 15))
                .comments("Updated comments")
                .build();

        given(repository.findById(customerId)).willReturn(Optional.of(existingCustomer));
        given(phoneMapper.toPhoneNumber("+7 (999) 888-77-66")).willReturn(new PhoneNumber("+79998887766"));
        given(repository.findByPhone("+79998887766")).willReturn(Optional.empty());
        given(mapper.toCustomer(command)).willReturn(updatedCustomer);
        given(repository.save(any(Customer.class))).willAnswer(invocation -> invocation.getArgument(0));

        Customer result = service.execute(command);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(customerId);
        assertThat(result.getPhone().value()).isEqualTo("+79998887766");
        assertThat(result.getFirstName()).isEqualTo("John");
        assertThat(result.getLastName()).isEqualTo("Doe");
        assertThat(result.getEmail().value()).isEqualTo("john.updated@example.com");
        assertThat(result.getBirthDate()).isEqualTo(LocalDate.of(1990, 1, 15));
        assertThat(result.getComments()).isEqualTo("Updated comments");

        then(repository).should().findById(customerId);
        then(repository).should().findByPhone("+79998887766");
        then(repository).should().save(any(Customer.class));
    }

    @Test
    void shouldUpdateCustomerWithSamePhoneNumber() {
        UUID customerId = UUID.randomUUID();
        UpdateCustomerUseCase.UpdateCustomerCommand command = new UpdateCustomerUseCase.UpdateCustomerCommand(
                customerId,
                "+7 (999) 888-77-66",
                "John",
                "Doe",
                "john.doe@example.com",
                LocalDate.of(1990, 1, 15),
                null
        );

        Customer existingCustomer = Customer.builder()
                .id(customerId)
                .phone(new PhoneNumber("+79998887766"))
                .firstName("John")
                .lastName("Smith")
                .build();

        Customer updatedCustomer = Customer.builder()
                .id(customerId)
                .phone(new PhoneNumber("+79998887766"))
                .firstName("John")
                .lastName("Doe")
                .email(new EmailAddress("john.doe@example.com"))
                .birthDate(LocalDate.of(1990, 1, 15))
                .build();

        given(repository.findById(customerId)).willReturn(Optional.of(existingCustomer));
        given(phoneMapper.toPhoneNumber("+7 (999) 888-77-66")).willReturn(new PhoneNumber("+79998887766"));
        given(mapper.toCustomer(command)).willReturn(updatedCustomer);
        given(repository.save(any(Customer.class))).willAnswer(invocation -> invocation.getArgument(0));

        Customer result = service.execute(command);

        assertThat(result).isNotNull();
        assertThat(result.getPhone().value()).isEqualTo("+79998887766");
        assertThat(result.getLastName()).isEqualTo("Doe");

        then(repository).should().findById(customerId);
        then(repository).should(never()).findByPhone(any());
        then(repository).should().save(any(Customer.class));
    }

    @Test
    void shouldNormalizePhoneNumberBeforeUpdate() {
        UUID customerId = UUID.randomUUID();
        UpdateCustomerUseCase.UpdateCustomerCommand command = new UpdateCustomerUseCase.UpdateCustomerCommand(
                customerId,
                "+7 (999) 888-77-66",
                "John",
                "Doe",
                null,
                null,
                null
        );

        Customer existingCustomer = Customer.builder()
                .id(customerId)
                .phone(new PhoneNumber("+79991234567"))
                .firstName("John")
                .lastName("Doe")
                .build();

        Customer updatedCustomer = Customer.builder()
                .id(customerId)
                .phone(new PhoneNumber("+79998887766"))
                .firstName("John")
                .lastName("Doe")
                .build();

        given(repository.findById(customerId)).willReturn(Optional.of(existingCustomer));
        given(phoneMapper.toPhoneNumber("+7 (999) 888-77-66")).willReturn(new PhoneNumber("+79998887766"));
        given(repository.findByPhone("+79998887766")).willReturn(Optional.empty());
        given(mapper.toCustomer(command)).willReturn(updatedCustomer);
        given(repository.save(any(Customer.class))).willAnswer(invocation -> invocation.getArgument(0));

        service.execute(command);

        then(repository).should().findByPhone("+79998887766");
    }

    @Test
    void shouldThrowExceptionWhenCustomerNotFound() {
        UUID customerId = UUID.randomUUID();
        UpdateCustomerUseCase.UpdateCustomerCommand command = new UpdateCustomerUseCase.UpdateCustomerCommand(
                customerId,
                "+7 (999) 888-77-66",
                "John",
                "Doe",
                null,
                null,
                null
        );

        given(repository.findById(customerId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Customer")
                .hasMessageContaining(customerId.toString());

        then(repository).should().findById(customerId);
        then(repository).should(never()).findByPhone(any());
        then(repository).should(never()).save(any(Customer.class));
    }

    @Test
    void shouldThrowExceptionWhenPhoneAlreadyUsedByAnotherCustomer() {
        UUID customerId = UUID.randomUUID();
        UUID otherCustomerId = UUID.randomUUID();

        UpdateCustomerUseCase.UpdateCustomerCommand command = new UpdateCustomerUseCase.UpdateCustomerCommand(
                customerId,
                "+7 (999) 888-77-66",
                "John",
                "Doe",
                null,
                null,
                null
        );

        Customer existingCustomer = Customer.builder()
                .id(customerId)
                .phone(new PhoneNumber("+79991234567"))
                .firstName("John")
                .lastName("Doe")
                .build();

        Customer otherCustomer = Customer.builder()
                .id(otherCustomerId)
                .phone(new PhoneNumber("+79998887766"))
                .firstName("Jane")
                .lastName("Smith")
                .build();

        given(repository.findById(customerId)).willReturn(Optional.of(existingCustomer));
        given(phoneMapper.toPhoneNumber("+7 (999) 888-77-66")).willReturn(new PhoneNumber("+79998887766"));
        given(repository.findByPhone("+79998887766")).willReturn(Optional.of(otherCustomer));

        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(DuplicatePhoneException.class)
                .hasMessageContaining("Customer")
                .hasMessageContaining("+79998887766");

        then(repository).should().findById(customerId);
        then(repository).should().findByPhone("+79998887766");
        then(repository).should(never()).save(any(Customer.class));
    }

    @Test
    void shouldUpdateCustomerWithMinimalData() {
        UUID customerId = UUID.randomUUID();
        UpdateCustomerUseCase.UpdateCustomerCommand command = new UpdateCustomerUseCase.UpdateCustomerCommand(
                customerId,
                "+79998887766",
                "Jane",
                "Smith",
                null,
                null,
                null
        );

        Customer existingCustomer = Customer.builder()
                .id(customerId)
                .phone(new PhoneNumber("+79998887766"))
                .firstName("Jane")
                .lastName("Doe")
                .build();

        Customer updatedCustomer = Customer.builder()
                .id(customerId)
                .phone(new PhoneNumber("+79998887766"))
                .firstName("Jane")
                .lastName("Smith")
                .build();

        given(repository.findById(customerId)).willReturn(Optional.of(existingCustomer));
        given(phoneMapper.toPhoneNumber("+79998887766")).willReturn(new PhoneNumber("+79998887766"));
        given(mapper.toCustomer(command)).willReturn(updatedCustomer);
        given(repository.save(any(Customer.class))).willAnswer(invocation -> invocation.getArgument(0));

        Customer result = service.execute(command);

        assertThat(result).isNotNull();
        assertThat(result.getPhone().value()).isEqualTo("+79998887766");
        assertThat(result.getFirstName()).isEqualTo("Jane");
        assertThat(result.getLastName()).isEqualTo("Smith");
        assertThat(result.getEmail()).isNull();
        assertThat(result.getBirthDate()).isNull();
        assertThat(result.getComments()).isNull();
    }

    @Test
    void shouldUpdateCustomerWithEmail() {
        UUID customerId = UUID.randomUUID();
        UpdateCustomerUseCase.UpdateCustomerCommand command = new UpdateCustomerUseCase.UpdateCustomerCommand(
                customerId,
                "+79998887766",
                "John",
                "Doe",
                "test@example.com",
                null,
                null
        );

        Customer existingCustomer = Customer.builder()
                .id(customerId)
                .phone(new PhoneNumber("+79998887766"))
                .firstName("John")
                .lastName("Doe")
                .build();

        Customer updatedCustomer = Customer.builder()
                .id(customerId)
                .phone(new PhoneNumber("+79998887766"))
                .firstName("John")
                .lastName("Doe")
                .email(new EmailAddress("test@example.com"))
                .build();

        given(repository.findById(customerId)).willReturn(Optional.of(existingCustomer));
        given(phoneMapper.toPhoneNumber("+79998887766")).willReturn(new PhoneNumber("+79998887766"));
        given(mapper.toCustomer(command)).willReturn(updatedCustomer);
        given(repository.save(any(Customer.class))).willAnswer(invocation -> invocation.getArgument(0));

        Customer result = service.execute(command);

        assertThat(result).isNotNull();
        assertThat(result.getEmail().value()).isEqualTo("test@example.com");
    }

    @Test
    void shouldUpdateCustomerWithBirthDate() {
        UUID customerId = UUID.randomUUID();
        UpdateCustomerUseCase.UpdateCustomerCommand command = new UpdateCustomerUseCase.UpdateCustomerCommand(
                customerId,
                "+79998887766",
                "John",
                "Doe",
                null,
                LocalDate.of(1995, 5, 20),
                null
        );

        Customer existingCustomer = Customer.builder()
                .id(customerId)
                .phone(new PhoneNumber("+79998887766"))
                .firstName("John")
                .lastName("Doe")
                .build();

        Customer updatedCustomer = Customer.builder()
                .id(customerId)
                .phone(new PhoneNumber("+79998887766"))
                .firstName("John")
                .lastName("Doe")
                .birthDate(LocalDate.of(1995, 5, 20))
                .build();

        given(repository.findById(customerId)).willReturn(Optional.of(existingCustomer));
        given(phoneMapper.toPhoneNumber("+79998887766")).willReturn(new PhoneNumber("+79998887766"));
        given(mapper.toCustomer(command)).willReturn(updatedCustomer);
        given(repository.save(any(Customer.class))).willAnswer(invocation -> invocation.getArgument(0));

        Customer result = service.execute(command);

        assertThat(result).isNotNull();
        assertThat(result.getBirthDate()).isEqualTo(LocalDate.of(1995, 5, 20));
    }

    @Test
    void shouldUpdateCustomerWithComments() {
        UUID customerId = UUID.randomUUID();
        UpdateCustomerUseCase.UpdateCustomerCommand command = new UpdateCustomerUseCase.UpdateCustomerCommand(
                customerId,
                "+79998887766",
                "John",
                "Doe",
                null,
                null,
                "VIP Customer"
        );

        Customer existingCustomer = Customer.builder()
                .id(customerId)
                .phone(new PhoneNumber("+79998887766"))
                .firstName("John")
                .lastName("Doe")
                .build();

        Customer updatedCustomer = Customer.builder()
                .id(customerId)
                .phone(new PhoneNumber("+79998887766"))
                .firstName("John")
                .lastName("Doe")
                .comments("VIP Customer")
                .build();

        given(repository.findById(customerId)).willReturn(Optional.of(existingCustomer));
        given(phoneMapper.toPhoneNumber("+79998887766")).willReturn(new PhoneNumber("+79998887766"));
        given(mapper.toCustomer(command)).willReturn(updatedCustomer);
        given(repository.save(any(Customer.class))).willAnswer(invocation -> invocation.getArgument(0));

        Customer result = service.execute(command);

        assertThat(result).isNotNull();
        assertThat(result.getComments()).isEqualTo("VIP Customer");
    }

    @Test
    void shouldSaveCustomerWithCorrectData() {
        UUID customerId = UUID.randomUUID();
        UpdateCustomerUseCase.UpdateCustomerCommand command = new UpdateCustomerUseCase.UpdateCustomerCommand(
                customerId,
                "+7 (999) 888-77-66",
                "John",
                "Doe",
                "john@example.com",
                LocalDate.of(1990, 1, 15),
                "Test comments"
        );

        Customer existingCustomer = Customer.builder()
                .id(customerId)
                .phone(new PhoneNumber("+79991234567"))
                .firstName("Old")
                .lastName("Name")
                .build();

        Customer updatedCustomer = Customer.builder()
                .id(customerId)
                .phone(new PhoneNumber("+79998887766"))
                .firstName("John")
                .lastName("Doe")
                .email(new EmailAddress("john@example.com"))
                .birthDate(LocalDate.of(1990, 1, 15))
                .comments("Test comments")
                .build();

        given(repository.findById(customerId)).willReturn(Optional.of(existingCustomer));
        given(phoneMapper.toPhoneNumber("+7 (999) 888-77-66")).willReturn(new PhoneNumber("+79998887766"));
        given(repository.findByPhone("+79998887766")).willReturn(Optional.empty());
        given(mapper.toCustomer(command)).willReturn(updatedCustomer);
        given(repository.save(any(Customer.class))).willAnswer(invocation -> invocation.getArgument(0));

        ArgumentCaptor<Customer> customerCaptor = ArgumentCaptor.forClass(Customer.class);

        service.execute(command);

        then(repository).should().save(customerCaptor.capture());
        Customer savedCustomer = customerCaptor.getValue();
        assertThat(savedCustomer.getId()).isEqualTo(customerId);
        assertThat(savedCustomer.getPhone().value()).isEqualTo("+79998887766");
        assertThat(savedCustomer.getFirstName()).isEqualTo("John");
        assertThat(savedCustomer.getLastName()).isEqualTo("Doe");
        assertThat(savedCustomer.getEmail().value()).isEqualTo("john@example.com");
        assertThat(savedCustomer.getBirthDate()).isEqualTo(LocalDate.of(1990, 1, 15));
        assertThat(savedCustomer.getComments()).isEqualTo("Test comments");
    }
}
