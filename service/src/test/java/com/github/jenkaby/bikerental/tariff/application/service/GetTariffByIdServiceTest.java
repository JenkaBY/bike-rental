package com.github.jenkaby.bikerental.tariff.application.service;

import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetTariffByIdService Tests")
class GetTariffByIdServiceTest {

    @Mock
    private TariffRepository repository;
    @InjectMocks
    private GetTariffByIdService service;

    @Test
    @DisplayName("Should get tariff by id successfully")
    void shouldGetTariffByIdSuccessfully() {
        Tariff tariff = Tariff.builder()
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

        given(repository.get(1L)).willReturn(tariff);

        Tariff result = service.execute(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Hourly Rate");
        then(repository).should().get(1L);
    }
}
