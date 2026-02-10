package com.github.jenkaby.bikerental.tariff.application.service;

import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;
import com.github.jenkaby.bikerental.tariff.application.usecase.CalculateEstimatedCostUseCase;
import com.github.jenkaby.bikerental.tariff.application.usecase.GetTariffByIdUseCase;
import com.github.jenkaby.bikerental.tariff.domain.model.Tariff;
import com.github.jenkaby.bikerental.tariff.domain.model.TariffStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
@DisplayName("CalculateEstimatedCostService Tests")
class CalculateEstimatedCostServiceTest {

    @Mock
    private GetTariffByIdUseCase getTariffByIdUseCase;

    @InjectMocks
    private CalculateEstimatedCostService service;

    @Test
    @DisplayName("Should calculate estimated cost using base price")
    void shouldCalculateEstimatedCostUsingBasePrice() {
        Long tariffId = 1L;
        Duration duration = Duration.ofHours(2);

        Tariff tariff = Tariff.builder()
                .id(tariffId)
                .name("Test Tariff")
                .description("Test")
                .equipmentTypeSlug("bike")
                .basePrice(Money.of("100.00"))
                .halfHourPrice(Money.of("60.00"))
                .hourPrice(Money.of("100.00"))
                .dayPrice(Money.of("500.00"))
                .hourDiscountedPrice(Money.of("90.00"))
                .validFrom(LocalDate.of(2026, 1, 1))
                .validTo(null)
                .status(TariffStatus.ACTIVE)
                .build();

        CalculateEstimatedCostUseCase.CalculateEstimatedCostCommand command =
                new CalculateEstimatedCostUseCase.CalculateEstimatedCostCommand(tariffId, duration);

        given(getTariffByIdUseCase.get(tariffId))
                .willReturn(tariff);

        Money result = service.execute(command);

        assertThat(result).isNotNull();
        assertThat(result.amount()).isEqualByComparingTo(Money.of("100.00").amount());

        then(getTariffByIdUseCase).should().get(tariffId);
    }
}
