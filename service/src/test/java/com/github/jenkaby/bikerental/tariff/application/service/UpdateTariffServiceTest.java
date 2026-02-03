package com.github.jenkaby.bikerental.tariff.application.service;

import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;
import com.github.jenkaby.bikerental.tariff.application.mapper.TariffCommandToDomainMapper;
import com.github.jenkaby.bikerental.tariff.application.usecase.UpdateTariffUseCase;
import com.github.jenkaby.bikerental.tariff.domain.model.Tariff;
import com.github.jenkaby.bikerental.tariff.domain.model.TariffPeriod;
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
@DisplayName("UpdateTariffService Tests")
class UpdateTariffServiceTest {

    @Mock
    private TariffRepository repository;
    @Mock
    private TariffCommandToDomainMapper mapper;
    @InjectMocks
    private UpdateTariffService service;

    @Test
    @DisplayName("Should update tariff successfully")
    void shouldUpdateTariffSuccessfully() {
        UpdateTariffUseCase.UpdateTariffCommand command = new UpdateTariffUseCase.UpdateTariffCommand(
                1L,
                "Updated Hourly Rate",
                "Updated description",
                "bicycle",
                TariffPeriod.HOUR,
                new BigDecimal("120.00"),
                new BigDecimal("70.00"),
                new BigDecimal("120.00"),
                new BigDecimal("600.00"),
                new BigDecimal("110.00"),
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31),
                "ACTIVE"
        );

        Tariff updatedTariff = Tariff.builder()
                .id(1L)
                .name("Updated Hourly Rate")
                .description("Updated description")
                .equipmentTypeSlug("bicycle")
                .basePrice(Money.of("120.00"))
                .halfHourPrice(Money.of("70.00"))
                .hourPrice(Money.of("120.00"))
                .dayPrice(Money.of("600.00"))
                .hourDiscountedPrice(Money.of("110.00"))
                .validFrom(LocalDate.of(2026, 1, 1))
                .validTo(LocalDate.of(2026, 12, 31))
                .status(TariffStatus.ACTIVE)
                .build();

        given(repository.get(1L)).willReturn(updatedTariff);
        given(mapper.toTariff(command)).willReturn(updatedTariff);
        given(repository.save(any(Tariff.class))).willReturn(updatedTariff);

        Tariff result = service.execute(command);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Updated Hourly Rate");
        assertThat(result.getBasePrice().amount()).isEqualByComparingTo(new BigDecimal("120.00"));
        then(repository).should().get(1L);
        then(mapper).should().toTariff(command);
        then(repository).should().save(updatedTariff);
    }
}
