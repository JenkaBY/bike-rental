package com.github.jenkaby.bikerental.tariff.application.service;

import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;
import com.github.jenkaby.bikerental.tariff.application.mapper.TariffCommandToDomainMapper;
import com.github.jenkaby.bikerental.tariff.application.usecase.CreateTariffUseCase;
import com.github.jenkaby.bikerental.tariff.domain.model.Tariff;
import com.github.jenkaby.bikerental.tariff.domain.model.TariffStatus;
import com.github.jenkaby.bikerental.tariff.domain.repository.TariffRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateTariffService Tests")
class CreateTariffServiceTest {

    @Mock
    private TariffRepository repository;
    @Mock
    private TariffCommandToDomainMapper mapper;
    @InjectMocks
    private CreateTariffService service;

    @Test
    @DisplayName("Should create tariff successfully")
    void shouldCreateTariffSuccessfully() {
        CreateTariffUseCase.CreateTariffCommand command = new CreateTariffUseCase.CreateTariffCommand(
                "Hourly Rate",
                "Standard hourly rental",
                "bicycle",
                new BigDecimal("100.00"),
                new BigDecimal("60.00"),
                new BigDecimal("100.00"),
                new BigDecimal("500.00"),
                new BigDecimal("90.00"),
                LocalDate.of(2026, 1, 1),
                null,
                TariffStatus.ACTIVE.name()
        );

        Tariff tariff = Tariff.builder()
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

        Tariff savedTariff = Tariff.builder()
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

        given(mapper.toTariff(command)).willReturn(tariff);
        given(repository.save(any(Tariff.class))).willReturn(savedTariff);

        Tariff result = service.execute(command);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Hourly Rate");
        assertThat(result.getDescription()).isEqualTo("Standard hourly rental");
        assertThat(result.getEquipmentTypeSlug()).isEqualTo("bicycle");
        assertThat(result.isActive()).isTrue();
        then(mapper).should().toTariff(command);
        then(repository).should().save(tariff);
    }
}
