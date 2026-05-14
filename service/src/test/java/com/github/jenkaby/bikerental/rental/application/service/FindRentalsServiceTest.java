package com.github.jenkaby.bikerental.rental.application.service;

import com.github.jenkaby.bikerental.rental.application.usecase.FindRentalsUseCase;
import com.github.jenkaby.bikerental.rental.domain.model.Rental;
import com.github.jenkaby.bikerental.rental.domain.model.RentalSearchFilter;
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

import java.time.LocalDate;
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
    private static final LocalDate FROM_DATE = LocalDate.of(2026, 2, 15);
    private static final LocalDate TO_DATE = LocalDate.of(2026, 2, 20);

    @Mock
    private RentalRepository repository;

    @InjectMocks
    private FindRentalsService service;

    @Test
    @DisplayName("Should pass all filter fields to repository")
    void shouldPassAllFilterFieldsToRepository() {
        var query = new FindRentalsUseCase.FindRentalsQuery(STATUS, CUSTOMER_ID, EQUIPMENT_UID, PAGE_REQUEST, FROM_DATE, TO_DATE);
        var page = emptyPage();
        given(repository.findAll(new RentalSearchFilter(STATUS, CUSTOMER_ID, EQUIPMENT_UID, FROM_DATE, TO_DATE), PAGE_REQUEST)).willReturn(page);

        Page<Rental> result = service.execute(query);

        assertThat(result).isNotNull();
        then(repository).should().findAll(new RentalSearchFilter(STATUS, CUSTOMER_ID, EQUIPMENT_UID, FROM_DATE, TO_DATE), PAGE_REQUEST);
        then(repository).shouldHaveNoMoreInteractions();
    }

    @Test
    @DisplayName("Should pass null date fields when no date range supplied")
    void shouldPassNullDateFieldsWhenNoDateRangeSupplied() {
        var query = new FindRentalsUseCase.FindRentalsQuery(STATUS, null, null, PAGE_REQUEST, null, null);
        var page = emptyPage();
        given(repository.findAll(new RentalSearchFilter(STATUS, null, null, null, null), PAGE_REQUEST)).willReturn(page);

        Page<Rental> result = service.execute(query);

        assertThat(result).isNotNull();
        then(repository).should().findAll(new RentalSearchFilter(STATUS, null, null, null, null), PAGE_REQUEST);
        then(repository).shouldHaveNoMoreInteractions();
    }

    @Test
    @DisplayName("Should pass only from date when to is absent")
    void shouldPassOnlyFromDateWhenToIsAbsent() {
        var query = new FindRentalsUseCase.FindRentalsQuery(null, null, null, PAGE_REQUEST, FROM_DATE, null);
        var page = emptyPage();
        given(repository.findAll(new RentalSearchFilter(null, null, null, FROM_DATE, null), PAGE_REQUEST)).willReturn(page);

        Page<Rental> result = service.execute(query);

        assertThat(result).isNotNull();
        then(repository).should().findAll(new RentalSearchFilter(null, null, null, FROM_DATE, null), PAGE_REQUEST);
        then(repository).shouldHaveNoMoreInteractions();
    }

    @Test
    @DisplayName("Should pass only to date when from is absent")
    void shouldPassOnlyToDateWhenFromIsAbsent() {
        var query = new FindRentalsUseCase.FindRentalsQuery(null, CUSTOMER_ID, null, PAGE_REQUEST, null, TO_DATE);
        var page = emptyPage();
        given(repository.findAll(new RentalSearchFilter(null, CUSTOMER_ID, null, null, TO_DATE), PAGE_REQUEST)).willReturn(page);

        Page<Rental> result = service.execute(query);

        assertThat(result).isNotNull();
        then(repository).should().findAll(new RentalSearchFilter(null, CUSTOMER_ID, null, null, TO_DATE), PAGE_REQUEST);
        then(repository).shouldHaveNoMoreInteractions();
    }

    private Page<Rental> emptyPage() {
        return new Page<>(List.of(), 0L, PAGE_REQUEST);
    }
}
