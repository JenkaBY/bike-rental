package com.github.jenkaby.bikerental.rental.application.service;

import com.github.jenkaby.bikerental.finance.FinanceFacade;
import com.github.jenkaby.bikerental.finance.PaymentInfo;
import com.github.jenkaby.bikerental.finance.PaymentMethod;
import com.github.jenkaby.bikerental.rental.application.usecase.RecordPrepaymentUseCase;
import com.github.jenkaby.bikerental.rental.domain.exception.InvalidRentalStatusException;
import com.github.jenkaby.bikerental.rental.domain.model.Rental;
import com.github.jenkaby.bikerental.rental.domain.model.RentalEquipment;
import com.github.jenkaby.bikerental.rental.domain.model.RentalStatus;
import com.github.jenkaby.bikerental.rental.domain.repository.RentalRepository;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;
import com.github.jenkaby.bikerental.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
@DisplayName("RecordPrepaymentService Tests")
class RecordPrepaymentServiceTest {

    private static final Long RENTAL_ID = 1L;
    private static final PaymentMethod PAYMENT_METHOD = PaymentMethod.CASH;
    private static final String OPERATOR_ID = "operator-1";
    private static final UUID PAYMENT_ID = UUID.randomUUID();
    private static final String RECEIPT_NUMBER = "REC-2026-001";

    @Mock
    private RentalRepository rentalRepository;
    @Mock
    private FinanceFacade financeFacade;
    @InjectMocks
    private RecordPrepaymentService service;

    @Test
    @DisplayName("Should record prepayment successfully for draft rental")
    void shouldRecordPrepaymentSuccessfully() {
        // Given
        Rental rental = createDraftRental();
        var command = new RecordPrepaymentUseCase.RecordPrepaymentCommand(
                RENTAL_ID, Money.of("100.00"), PAYMENT_METHOD, OPERATOR_ID);

        PaymentInfo expectedPaymentInfo = new PaymentInfo(
                PAYMENT_ID, Money.of("100.00"), PAYMENT_METHOD, RECEIPT_NUMBER, Instant.now());

        given(rentalRepository.findById(RENTAL_ID)).willReturn(Optional.of(rental));
        given(financeFacade.recordPrepayment(
                eq(RENTAL_ID), eq(Money.of("100.00")), eq(PAYMENT_METHOD), eq(OPERATOR_ID)))
                .willReturn(expectedPaymentInfo);

        // When
        PaymentInfo result = service.execute(command);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(PAYMENT_ID);
        assertThat(result.amount()).isEqualTo(Money.of("100.00"));
        assertThat(result.paymentMethod()).isEqualTo(PAYMENT_METHOD);
        assertThat(result.receiptNumber()).isEqualTo(RECEIPT_NUMBER);
        then(financeFacade).should().recordPrepayment(
                eq(RENTAL_ID), eq(Money.of("100.00")), eq(PAYMENT_METHOD), eq(OPERATOR_ID));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when rental not found")
    void shouldThrowResourceNotFoundExceptionWhenRentalNotFound() {
        // Given
        var command = new RecordPrepaymentUseCase.RecordPrepaymentCommand(
                RENTAL_ID, Money.of("100.00"), PAYMENT_METHOD, OPERATOR_ID);
        given(rentalRepository.findById(RENTAL_ID)).willReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Rental")
                .hasMessageContaining(RENTAL_ID.toString());

        then(financeFacade).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("Should throw InvalidRentalStatusException when rental is not in DRAFT status")
    void shouldThrowInvalidRentalStatusWhenNotDraft() {
        // Given
        Rental rental = createDraftRental();
        rental.selectCustomer(UUID.randomUUID());
        rental.addEquipment(RentalEquipment.assigned(100L, null));
        rental.selectTariff(200L);
        rental.setPlannedDuration(java.time.Duration.ofHours(2));
        rental.setEstimatedCost(com.github.jenkaby.bikerental.shared.domain.model.vo.Money.of("100.00"));
        rental.activate(java.time.LocalDateTime.now());

        var command = new RecordPrepaymentUseCase.RecordPrepaymentCommand(
                RENTAL_ID, Money.of("100.00"), PAYMENT_METHOD, OPERATOR_ID);
        given(rentalRepository.findById(RENTAL_ID)).willReturn(Optional.of(rental));

        // When/Then
        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(InvalidRentalStatusException.class)
                .hasMessageContaining("ACTIVE")
                .hasMessageContaining("DRAFT");

        then(financeFacade).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("Should throw InvalidRentalStatusException when rental is CANCELLED")
    void shouldThrowInvalidRentalStatusWhenCancelled() {
        // Given
        Rental rental = createDraftRental();
        rental.setStatus(RentalStatus.CANCELLED);

        var command = new RecordPrepaymentUseCase.RecordPrepaymentCommand(
                RENTAL_ID, Money.of("100.00"), PAYMENT_METHOD, OPERATOR_ID);
        given(rentalRepository.findById(RENTAL_ID)).willReturn(Optional.of(rental));

        // When/Then
        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(InvalidRentalStatusException.class)
                .hasMessageContaining("CANCELLED")
                .hasMessageContaining("DRAFT");

        then(financeFacade).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("Should accept prepayment when amount equals estimated cost")
    void shouldAcceptPrepaymentWhenAmountEqualsEstimatedCost() {
        // Given - amount 100 equals estimated cost 100
        Rental rental = createDraftRental();
        var command = new RecordPrepaymentUseCase.RecordPrepaymentCommand(
                RENTAL_ID, Money.of("100.00"), PAYMENT_METHOD, OPERATOR_ID);

        PaymentInfo expectedPaymentInfo = new PaymentInfo(
                PAYMENT_ID, Money.of("100.00"), PAYMENT_METHOD, RECEIPT_NUMBER, Instant.now());

        given(rentalRepository.findById(RENTAL_ID)).willReturn(Optional.of(rental));
        given(financeFacade.recordPrepayment(
                eq(RENTAL_ID), eq(Money.of("100.00")), eq(PAYMENT_METHOD), eq(OPERATOR_ID)))
                .willReturn(expectedPaymentInfo);

        // When
        PaymentInfo result = service.execute(command);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.amount()).isEqualTo(Money.of("100.00"));
    }

    @Test
    @DisplayName("Should accept prepayment when amount exceeds estimated cost")
    void shouldAcceptPrepaymentWhenAmountExceedsEstimatedCost() {
        // Given - amount 150 exceeds estimated cost 100
        Rental rental = createDraftRental();
        var command = new RecordPrepaymentUseCase.RecordPrepaymentCommand(
                RENTAL_ID, Money.of("150.00"), PAYMENT_METHOD, OPERATOR_ID);

        PaymentInfo expectedPaymentInfo = new PaymentInfo(
                PAYMENT_ID, Money.of("150.00"), PAYMENT_METHOD, RECEIPT_NUMBER, Instant.now());

        given(rentalRepository.findById(RENTAL_ID)).willReturn(Optional.of(rental));
        given(financeFacade.recordPrepayment(
                eq(RENTAL_ID), eq(Money.of("150.00")), eq(PAYMENT_METHOD), eq(OPERATOR_ID)))
                .willReturn(expectedPaymentInfo);

        // When
        PaymentInfo result = service.execute(command);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.amount()).isEqualTo(Money.of("150.00"));
    }

    private Rental createDraftRental() {
        Rental rental = Rental.builder()
                .id(RENTAL_ID)
                .status(RentalStatus.DRAFT)
                .createdAt(Instant.now())
                .equipments(new ArrayList<>())
                .build();
        rental.selectCustomer(UUID.randomUUID());
        rental.addEquipment(RentalEquipment.assigned(100L, null));
        rental.selectTariff(200L);
        rental.setPlannedDuration(java.time.Duration.ofHours(2));
        rental.setEstimatedCost(com.github.jenkaby.bikerental.shared.domain.model.vo.Money.of("100.00"));
        return rental;
    }

    private Rental createDraftRentalWithoutEstimatedCost() {
        Rental rental = Rental.builder()
                .id(RENTAL_ID)
                .status(RentalStatus.DRAFT)
                .createdAt(Instant.now())
                .equipments(new ArrayList<>())
                .build();
        rental.selectCustomer(UUID.randomUUID());
        rental.addEquipment(RentalEquipment.assigned(100L, null));
        rental.selectTariff(200L);
        rental.setPlannedDuration(java.time.Duration.ofHours(2));
        // estimatedCost intentionally not set
        return rental;
    }
}

