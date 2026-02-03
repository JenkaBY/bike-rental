package com.github.jenkaby.bikerental.tariff.application.service;

import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;
import com.github.jenkaby.bikerental.shared.exception.ResourceNotFoundException;
import com.github.jenkaby.bikerental.tariff.domain.model.Tariff;
import com.github.jenkaby.bikerental.tariff.domain.model.TariffStatus;
import com.github.jenkaby.bikerental.tariff.domain.repository.TariffRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
@DisplayName("ActivateTariffService Tests")
class ActivateTariffServiceTest {

    @Mock
    private TariffRepository repository;
    @InjectMocks
    private ActivateTariffService service;

    @Test
    @DisplayName("Should activate tariff successfully")
    void shouldActivateTariffSuccessfully() {
        Tariff inactiveTariff = Tariff.builder()
                .id(1L)
                .name("Hourly Rate")
                .description("Standard hourly rental")
                .equipmentTypeSlug("bicycle")
                .basePrice(Money.of("100.00"))
                .halfHourPrice(Money.of("60.00"))
                .hourPrice(Money.of("100.00"))
                .dayPrice(Money.of("500.00"))
                .hourDiscountedPrice(Money.of("90.00"))
                .validFrom(LocalDate.of(2026, 1, 1))
                .validTo(null)
                .status(TariffStatus.INACTIVE)
                .build();

        Tariff activatedTariff = Tariff.builder()
                .id(1L)
                .name("Hourly Rate")
                .description("Standard hourly rental")
                .equipmentTypeSlug("bicycle")
                .basePrice(Money.of("100.00"))
                .halfHourPrice(Money.of("60.00"))
                .hourPrice(Money.of("100.00"))
                .dayPrice(Money.of("500.00"))
                .hourDiscountedPrice(Money.of("90.00"))
                .validFrom(LocalDate.of(2026, 1, 1))
                .validTo(null)
                .status(TariffStatus.ACTIVE)
                .build();

        given(repository.findById(1L)).willReturn(Optional.of(inactiveTariff));
        given(repository.save(any(Tariff.class))).willReturn(activatedTariff);

        Tariff result = service.execute(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getStatus()).isEqualTo(TariffStatus.ACTIVE);
        assertThat(result.isActive()).isTrue();
        then(repository).should().findById(1L);
        then(repository).should().save(any(Tariff.class));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when tariff not found")
    void shouldThrowResourceNotFoundExceptionWhenTariffNotFound() {
        given(repository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Tariff")
                .hasMessageContaining("999");

        then(repository).should().findById(999L);
        then(repository).shouldHaveNoMoreInteractions();
    }
}
