package com.github.jenkaby.bikerental.rental.application.service;

import com.github.jenkaby.bikerental.rental.application.usecase.FindRentalsUseCase;
import com.github.jenkaby.bikerental.rental.domain.model.Rental;
import com.github.jenkaby.bikerental.rental.domain.model.RentalEquipment;
import com.github.jenkaby.bikerental.rental.domain.model.RentalStatus;
import com.github.jenkaby.bikerental.rental.domain.repository.RentalRepository;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Page;
import com.github.jenkaby.bikerental.shared.domain.model.vo.PageRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
@DisplayName("FindRentalsService Tests")
class FindRentalsServiceTest {

    private static final RentalStatus STATUS = RentalStatus.ACTIVE;
    private static final UUID CUSTOMER_ID = UUID.randomUUID();
    private static final String EQUIPMENT_UID = "BIKE-001";
    private static final PageRequest PAGE_REQUEST = new PageRequest(20, 0, null);

    @Mock
    private RentalRepository repository;

    @InjectMocks
    private FindRentalsService service;

    @Test
    @DisplayName("Should find rentals by status and equipmentUid")
    void shouldFindRentalsByStatusAndEquipmentUid() {
        // Given
        var query = new FindRentalsUseCase.FindRentalsQuery(STATUS, null, EQUIPMENT_UID, PAGE_REQUEST);
        var rental = Rental.builder()
                .id(1L)
                .status(STATUS)
                .equipments(List.of(RentalEquipment.assigned(1L, EQUIPMENT_UID, null)))
                .build();
        var page = new Page<>(List.of(rental), 1L, PAGE_REQUEST);

        given(repository.findByStatusAndEquipmentUid(STATUS, EQUIPMENT_UID, PAGE_REQUEST)).willReturn(page);

        // When
        Page<Rental> result = service.execute(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.items()).hasSize(1);
        assertThat(result.items().get(0).getEquipments()).isNotEmpty();
        assertThat(result.items().get(0).getEquipments().get(0).getEquipmentUid()).isEqualTo(EQUIPMENT_UID);
        then(repository).should().findByStatusAndEquipmentUid(STATUS, EQUIPMENT_UID, PAGE_REQUEST);
        then(repository).shouldHaveNoMoreInteractions();
    }

    @Test
    @DisplayName("Should find rentals by status and customerId when both provided")
    void shouldFindRentalsByStatusAndCustomerId() {
        // Given
        var query = new FindRentalsUseCase.FindRentalsQuery(STATUS, CUSTOMER_ID, null, PAGE_REQUEST);
        var rental = Rental.builder()
                .id(1L)
                .status(STATUS)
                .customerId(CUSTOMER_ID)
                .build();
        var page = new Page<>(List.of(rental), 1L, PAGE_REQUEST);

        given(repository.findByStatusAndCustomerId(STATUS, CUSTOMER_ID, PAGE_REQUEST)).willReturn(page);

        // When
        Page<Rental> result = service.execute(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.items()).hasSize(1);
        then(repository).should().findByStatusAndCustomerId(STATUS, CUSTOMER_ID, PAGE_REQUEST);
        then(repository).shouldHaveNoMoreInteractions();
    }

    @Test
    @DisplayName("Should prioritize equipmentUid over customerId when both provided with status")
    void shouldPrioritizeEquipmentUidOverCustomerId() {
        // Given
        var query = new FindRentalsUseCase.FindRentalsQuery(STATUS, CUSTOMER_ID, EQUIPMENT_UID, PAGE_REQUEST);
        var rental = Rental.builder()
                .id(1L)
                .status(STATUS)
                .equipments(List.of(RentalEquipment.assigned(1L, EQUIPMENT_UID, null)))
                .build();
        var page = new Page<>(List.of(rental), 1L, PAGE_REQUEST);

        given(repository.findByStatusAndEquipmentUid(STATUS, EQUIPMENT_UID, PAGE_REQUEST)).willReturn(page);

        // When
        Page<Rental> result = service.execute(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.items()).hasSize(1);
        then(repository).should().findByStatusAndEquipmentUid(STATUS, EQUIPMENT_UID, PAGE_REQUEST);
        then(repository).shouldHaveNoMoreInteractions();
    }

    @Test
    @DisplayName("Should find rentals by customerId only")
    void shouldFindRentalsByCustomerId() {
        // Given
        var query = new FindRentalsUseCase.FindRentalsQuery(null, CUSTOMER_ID, null, PAGE_REQUEST);
        var rental = Rental.builder()
                .id(1L)
                .customerId(CUSTOMER_ID)
                .build();
        var page = new Page<>(List.of(rental), 1L, PAGE_REQUEST);

        given(repository.findByCustomerId(CUSTOMER_ID, PAGE_REQUEST)).willReturn(page);

        // When
        Page<Rental> result = service.execute(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.items()).hasSize(1);
        then(repository).should().findByCustomerId(CUSTOMER_ID, PAGE_REQUEST);
        then(repository).shouldHaveNoMoreInteractions();
    }

    @Test
    @DisplayName("Should find rentals by status only")
    void shouldFindRentalsByStatus() {
        // Given
        var query = new FindRentalsUseCase.FindRentalsQuery(STATUS, null, null, PAGE_REQUEST);
        var rental = Rental.builder()
                .id(1L)
                .status(STATUS)
                .build();
        var page = new Page<>(List.of(rental), 1L, PAGE_REQUEST);

        given(repository.findByStatus(STATUS, PAGE_REQUEST)).willReturn(page);

        // When
        Page<Rental> result = service.execute(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.items()).hasSize(1);
        then(repository).should().findByStatus(STATUS, PAGE_REQUEST);
        then(repository).shouldHaveNoMoreInteractions();
    }
}
