package com.github.jenkaby.bikerental.rental.application.service;

import com.github.jenkaby.bikerental.customer.CustomerFacade;
import com.github.jenkaby.bikerental.customer.CustomerInfo;
import com.github.jenkaby.bikerental.equipment.EquipmentFacade;
import com.github.jenkaby.bikerental.equipment.EquipmentInfo;
import com.github.jenkaby.bikerental.finance.FinanceFacade;
import com.github.jenkaby.bikerental.rental.application.mapper.RentalEventMapper;
import com.github.jenkaby.bikerental.rental.application.service.validator.RequestedEquipmentValidator;
import com.github.jenkaby.bikerental.rental.domain.exception.InvalidRentalUpdateException;
import com.github.jenkaby.bikerental.rental.domain.exception.PrepaymentRequiredException;
import com.github.jenkaby.bikerental.rental.domain.model.Rental;
import com.github.jenkaby.bikerental.rental.domain.model.RentalEquipment;
import com.github.jenkaby.bikerental.rental.domain.model.RentalStatus;
import com.github.jenkaby.bikerental.rental.domain.repository.RentalRepository;
import com.github.jenkaby.bikerental.rental.infrastructure.util.PatchValueParser;
import com.github.jenkaby.bikerental.shared.domain.event.RentalStarted;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;
import com.github.jenkaby.bikerental.shared.exception.EquipmentNotAvailableException;
import com.github.jenkaby.bikerental.shared.exception.ReferenceNotFoundException;
import com.github.jenkaby.bikerental.shared.exception.ResourceNotFoundException;
import com.github.jenkaby.bikerental.shared.infrastructure.messaging.EventPublisher;
import com.github.jenkaby.bikerental.tariff.RentalCost;
import com.github.jenkaby.bikerental.tariff.TariffFacade;
import com.github.jenkaby.bikerental.tariff.TariffInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.spy;

@ExtendWith(MockitoExtension.class)
@DisplayName("UpdateRentalService Tests")
class UpdateRentalServiceTest {

    private static final Long RENTAL_ID = 1L;
    private static final UUID CUSTOMER_ID = UUID.randomUUID();
    private static final Long EQUIPMENT_ID = 100L;
    private static final Long TARIFF_ID = 200L;
    private static final Duration DURATION = Duration.ofHours(2);
    private static final LocalDateTime FIXED_TIME = LocalDateTime.of(2026, 2, 9, 10, 0);
    private static final Clock FIXED_CLOCK = Clock.fixed(
            Instant.parse("2026-02-09T10:00:00Z"),
            ZoneId.systemDefault()
    );

    @Mock
    private RentalRepository rentalRepository;
    @Mock
    private CustomerFacade customerFacade;
    @Mock
    private EquipmentFacade equipmentFacade;
    @Mock
    private TariffFacade tariffFacade;
    @Mock
    private RentalCost estimatedCost;
    @Mock
    private FinanceFacade financeFacade;
    @Mock
    private EventPublisher eventPublisher;
    @Mock
    private RentalEventMapper eventMapper;
    @Mock
    private PatchValueParser valueParser;
    @Mock
    private RequestedEquipmentValidator validator;
    @Spy
    private Clock realClock = spy(Clock.systemDefaultZone());
    @InjectMocks
    private UpdateRentalService service;

    @Test
    @DisplayName("Should throw ResourceNotFoundException when rental not found")
    void shouldThrowResourceNotFoundExceptionWhenRentalNotFound() {
        // Given
        Map<String, Object> patch = new HashMap<>();
        given(rentalRepository.findById(RENTAL_ID)).willReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> service.execute(RENTAL_ID, patch))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Rental")
                .hasMessageContaining(RENTAL_ID.toString());
    }

    @Test
    @DisplayName("Should throw ReferenceNotFoundException when customer not found")
    void shouldThrowReferenceNotFoundExceptionWhenCustomerNotFound() {
        // Given
        Rental rental = createDraftRental();
        Map<String, Object> patch = Map.of("customerId", CUSTOMER_ID.toString());

        given(rentalRepository.findById(RENTAL_ID)).willReturn(Optional.of(rental));
        given(valueParser.parseUUID(CUSTOMER_ID.toString())).willReturn(CUSTOMER_ID);
        given(customerFacade.findById(CUSTOMER_ID)).willReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> service.execute(RENTAL_ID, patch))
                .isInstanceOf(ReferenceNotFoundException.class)
                .hasMessageContaining("Customer")
                .hasMessageContaining(CUSTOMER_ID.toString());
    }

    @Test
    @DisplayName("Should throw ReferenceNotFoundException when equipment not found")
    void shouldThrowReferenceNotFoundExceptionWhenEquipmentNotFound() {
        // Given
        Rental rental = createDraftRental();
        Map<String, Object> patch = Map.of("equipmentId", EQUIPMENT_ID);

        given(rentalRepository.findById(RENTAL_ID)).willReturn(Optional.of(rental));
        given(valueParser.parseLong(EQUIPMENT_ID)).willReturn(EQUIPMENT_ID);
        given(equipmentFacade.findById(EQUIPMENT_ID)).willReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> service.execute(RENTAL_ID, patch))
                .isInstanceOf(ReferenceNotFoundException.class)
                .hasMessageContaining("Equipment")
                .hasMessageContaining(EQUIPMENT_ID.toString());
    }

    @Test
    @DisplayName("Should throw EquipmentNotAvailableException when equipment is not available")
    void shouldThrowEquipmentNotAvailableExceptionWhenEquipmentNotAvailable() {
        // Given
        Rental rental = createDraftRental();
        Map<String, Object> patch = Map.of("equipmentId", EQUIPMENT_ID);
        EquipmentInfo unavailableEquipment = new EquipmentInfo(
                EQUIPMENT_ID, "EQ-001", "BIKE-001", "bicycle", "RENTED", "Model A"
        );

        given(rentalRepository.findById(RENTAL_ID)).willReturn(Optional.of(rental));
        given(valueParser.parseLong(EQUIPMENT_ID)).willReturn(EQUIPMENT_ID);
        given(equipmentFacade.findById(EQUIPMENT_ID)).willReturn(Optional.of(unavailableEquipment));

        // When/Then
        assertThatThrownBy(() -> service.execute(RENTAL_ID, patch))
                .isInstanceOf(EquipmentNotAvailableException.class)
                .hasMessageContaining(EQUIPMENT_ID.toString())
                .hasMessageContaining("RENTED");
    }

    @Test
    @DisplayName("Should throw InvalidRentalUpdateException when duration is null")
    void shouldThrowInvalidRentalUpdateExceptionWhenDurationIsNull() {
        // Given
        Rental rental = createDraftRental();
        Map<String, Object> patch = new HashMap<>();
        patch.put("duration", null);

        given(rentalRepository.findById(RENTAL_ID)).willReturn(Optional.of(rental));
        given(valueParser.parseDuration(null)).willReturn(null);

        // When/Then
        assertThatThrownBy(() -> service.execute(RENTAL_ID, patch))
                .isInstanceOf(InvalidRentalUpdateException.class)
                .hasMessageContaining("Duration must be provided");
    }

    @Test
    @DisplayName("Should throw ReferenceNotFoundException when tariff not found")
    void shouldThrowReferenceNotFoundExceptionWhenTariffNotFound() {
        // Given
        Rental rental = createDraftRental();
        Map<String, Object> patch = Map.of("tariffId", TARIFF_ID);

        given(rentalRepository.findById(RENTAL_ID)).willReturn(Optional.of(rental));
        given(valueParser.parseLong(TARIFF_ID)).willReturn(TARIFF_ID);
        given(tariffFacade.findById(TARIFF_ID)).willReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> service.execute(RENTAL_ID, patch))
                .isInstanceOf(ReferenceNotFoundException.class)
                .hasMessageContaining("Tariff")
                .hasMessageContaining(TARIFF_ID.toString());
    }

    @Test
    @DisplayName("Should throw ReferenceNotFoundException when equipment not found during auto tariff selection")
    void shouldThrowReferenceNotFoundExceptionWhenEquipmentNotFoundDuringAutoTariffSelection() {
        // Given
        Rental rental = createDraftRentalWithEquipmentAndDuration();
        Map<String, Object> patch = new HashMap<>();

        given(rentalRepository.findById(RENTAL_ID)).willReturn(Optional.of(rental));
        given(equipmentFacade.findById(EQUIPMENT_ID)).willReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> service.execute(RENTAL_ID, patch))
                .isInstanceOf(ReferenceNotFoundException.class)
                .hasMessageContaining("Equipment")
                .hasMessageContaining(EQUIPMENT_ID.toString());
    }

    @Test
    @DisplayName("Should update customer successfully")
    void shouldUpdateCustomerSuccessfully() {
        // Given
        Rental rental = createDraftRental();
        Map<String, Object> patch = Map.of("customerId", CUSTOMER_ID.toString());
        Rental savedRental = createDraftRental();
        savedRental.selectCustomer(CUSTOMER_ID);

        given(rentalRepository.findById(RENTAL_ID)).willReturn(Optional.of(rental));
        given(valueParser.parseUUID(CUSTOMER_ID.toString())).willReturn(CUSTOMER_ID);
        given(customerFacade.findById(CUSTOMER_ID)).willReturn(Optional.of(createCustomerInfo()));
        given(rentalRepository.save(any(Rental.class))).willReturn(savedRental);

        // When
        Rental result = service.execute(RENTAL_ID, patch);

        // Then
        assertThat(result).isNotNull();
        then(rentalRepository).should().save(any(Rental.class));
    }

    @Test
    @DisplayName("Should update equipment successfully")
    void shouldUpdateEquipmentSuccessfully() {
        // Given
        Rental rental = createDraftRental();
        Map<String, Object> patch = Map.of("equipmentId", EQUIPMENT_ID);
        String equipmentUid = "BIKE-001";
        EquipmentInfo availableEquipment = new EquipmentInfo(
                EQUIPMENT_ID, "EQ-001", equipmentUid, "bicycle", "AVAILABLE", "Model A"
        );
        Rental savedRental = createDraftRental();

        given(rentalRepository.findById(RENTAL_ID)).willReturn(Optional.of(rental));
        given(valueParser.parseLong(EQUIPMENT_ID)).willReturn(EQUIPMENT_ID);
        given(equipmentFacade.findById(EQUIPMENT_ID)).willReturn(Optional.of(availableEquipment));
        given(rentalRepository.save(any(Rental.class))).willAnswer(invocation -> {
            Rental saved = invocation.getArgument(0);
            return saved;
        });

        // When
        Rental result = service.execute(RENTAL_ID, patch);

        // Then
        assertThat(result).isNotNull();
        then(rentalRepository).should().save(any(Rental.class));
    }

    @Test
    @DisplayName("Should update duration successfully")
    void shouldUpdateDurationSuccessfully() {
        // Given
        Rental rental = createDraftRental();
        Map<String, Object> patch = Map.of("duration", "PT2H");
        Rental savedRental = createDraftRental();
        savedRental.setPlannedDuration(DURATION);

        given(rentalRepository.findById(RENTAL_ID)).willReturn(Optional.of(rental));
        given(valueParser.parseDuration("PT2H")).willReturn(DURATION);
        given(rentalRepository.save(any(Rental.class))).willReturn(savedRental);

        // When
        Rental result = service.execute(RENTAL_ID, patch);

        // Then
        assertThat(result).isNotNull();
        then(rentalRepository).should().save(any(Rental.class));
    }

    @Test
    @DisplayName("Should update tariff successfully")
    void shouldUpdateTariffSuccessfully() {
        // Given
        Rental rental = createDraftRental();
        Map<String, Object> patch = Map.of("tariffId", TARIFF_ID);
        TariffInfo tariffInfo = createTariffInfo();
        Rental savedRental = createDraftRental();
        savedRental.selectTariff(TARIFF_ID);

        given(rentalRepository.findById(RENTAL_ID)).willReturn(Optional.of(rental));
        given(valueParser.parseLong(TARIFF_ID)).willReturn(TARIFF_ID);
        given(tariffFacade.findById(TARIFF_ID)).willReturn(Optional.of(tariffInfo));
        given(rentalRepository.save(any(Rental.class))).willReturn(savedRental);

        // When
        Rental result = service.execute(RENTAL_ID, patch);

        // Then
        assertThat(result).isNotNull();
        then(rentalRepository).should().save(any(Rental.class));
    }

    @Test
    @DisplayName("Should auto-select tariff when equipment and duration are set")
    void shouldSelectTariffWhenEquipmentAndDurationAreSet() {
        // Given
        Rental rental = createDraftRentalWithEquipmentAndDuration();
        Map<String, Object> patch = new HashMap<>();
        EquipmentInfo equipment = new EquipmentInfo(
                EQUIPMENT_ID, "EQ-001", "BIKE-001", "bicycle", "AVAILABLE", "Model A"
        );
        TariffInfo selectedTariff = createTariffInfo();

        given(rentalRepository.findById(RENTAL_ID)).willReturn(Optional.of(rental));
        given(equipmentFacade.findById(EQUIPMENT_ID)).willReturn(Optional.of(equipment));
        given(tariffFacade.selectTariff(any(), any(), any())).willReturn(selectedTariff);
        given(tariffFacade.calculateRentalCost(any(), any())).willReturn(estimatedCost);
        given(estimatedCost.totalCost()).willReturn(Money.of("100.00"));
        given(rentalRepository.save(any(Rental.class))).willReturn(rental);

        // When
        Rental result = service.execute(RENTAL_ID, patch);

        // Then
        assertThat(result).isNotNull();
        then(tariffFacade).should().selectTariff("bicycle", DURATION, LocalDate.now());
        then(tariffFacade).should().calculateRentalCost(TARIFF_ID, DURATION);
        then(rentalRepository).should().save(any(Rental.class));
    }

    @Test
    @DisplayName("Should calculate cost when tariff and duration are set")
    void shouldCalculateCostWhenTariffAndDurationAreSet() {
        // Given
        Rental rental = createDraftRentalWithTariffAndDuration();
        Map<String, Object> patch = new HashMap<>();
        Money expectedCost = Money.of("150.00");

        given(rentalRepository.findById(RENTAL_ID)).willReturn(Optional.of(rental));
        given(tariffFacade.calculateRentalCost(TARIFF_ID, DURATION)).willReturn(estimatedCost);
        given(estimatedCost.totalCost()).willReturn(expectedCost);
        given(rentalRepository.save(any(Rental.class))).willReturn(rental);

        // When
        Rental result = service.execute(RENTAL_ID, patch);

        // Then
        assertThat(result).isNotNull();
        then(tariffFacade).should().calculateRentalCost(TARIFF_ID, DURATION);
        then(rentalRepository).should().save(any(Rental.class));
    }

    @Test
    @DisplayName("Should activate rental successfully")
    void shouldActivateRentalSuccessfully() {
        // Given
        Rental rental = createReadyForActivationRental();
        Map<String, Object> patch = Map.of("status", "ACTIVE");
        RentalStarted event =
                new RentalStarted(
                        RENTAL_ID, CUSTOMER_ID, List.of(EQUIPMENT_ID), FIXED_TIME, FIXED_TIME.plus(DURATION)
                );

        given(rentalRepository.findById(RENTAL_ID)).willReturn(Optional.of(rental));
        given(valueParser.parseString("ACTIVE")).willReturn("ACTIVE");
        // Rental already has equipmentId and plannedDuration, so auto-select tariff will be triggered
        EquipmentInfo equipment = new EquipmentInfo(
                EQUIPMENT_ID, "EQ-001", "BIKE-001", "bicycle", "AVAILABLE", "Model A"
        );
        given(equipmentFacade.findById(EQUIPMENT_ID)).willReturn(Optional.of(equipment));
        TariffInfo selectedTariff = createTariffInfo();
        given(tariffFacade.selectTariff(any(), any(), any())).willReturn(selectedTariff);
        given(financeFacade.hasPrepayment(RENTAL_ID)).willReturn(true);
        // LocalDateTime.now(clock) uses clock internally, so we need to provide a fixed clock
        UpdateRentalService serviceWithFixedClock = new UpdateRentalService(
                rentalRepository, customerFacade, equipmentFacade, tariffFacade, financeFacade,
                eventPublisher, FIXED_CLOCK, eventMapper, valueParser, validator
        );
        given(eventMapper.toRentalStarted(any())).willReturn(event);
        given(rentalRepository.save(any(Rental.class))).willAnswer(invocation -> {
            Rental saved = invocation.getArgument(0);
            return saved;
        });
        // Rental already has tariff and duration, so cost calculation will be triggered
        given(tariffFacade.calculateRentalCost(TARIFF_ID, DURATION)).willReturn(estimatedCost);
        given(estimatedCost.totalCost()).willReturn(Money.of("100.00"));

        // When
        Rental result = serviceWithFixedClock.execute(RENTAL_ID, patch);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(RentalStatus.ACTIVE);
        then(eventPublisher).should().publish(any(), any());
        then(rentalRepository).should().save(any(Rental.class));
    }

    @Test
    @DisplayName("Should throw PrepaymentRequiredException when activating without prepayment")
    void shouldThrowPrepaymentRequiredWhenActivatingWithoutPrepayment() {
        // Given
        Rental rental = createReadyForActivationRental();
        Map<String, Object> patch = Map.of("status", "ACTIVE");

        EquipmentInfo equipment = new EquipmentInfo(
                EQUIPMENT_ID, "EQ-001", "BIKE-001", "bicycle", "AVAILABLE", "Model A"
        );
        TariffInfo selectedTariff = createTariffInfo();

        given(rentalRepository.findById(RENTAL_ID)).willReturn(Optional.of(rental));
        given(valueParser.parseString("ACTIVE")).willReturn("ACTIVE");
        given(equipmentFacade.findById(EQUIPMENT_ID)).willReturn(Optional.of(equipment));
        given(tariffFacade.selectTariff(any(), any(), any())).willReturn(selectedTariff);
        given(tariffFacade.calculateRentalCost(TARIFF_ID, DURATION)).willReturn(estimatedCost);
        given(estimatedCost.totalCost()).willReturn(Money.of("100.00"));
        given(financeFacade.hasPrepayment(RENTAL_ID)).willReturn(false);

        UpdateRentalService serviceWithFixedClock = new UpdateRentalService(
                rentalRepository, customerFacade, equipmentFacade, tariffFacade, financeFacade,
                eventPublisher, FIXED_CLOCK, eventMapper, valueParser, validator
        );

        // When/Then
        assertThatThrownBy(() -> serviceWithFixedClock.execute(RENTAL_ID, patch))
                .isInstanceOf(PrepaymentRequiredException.class)
                .hasMessageContaining("Prepayment must be received");

        then(rentalRepository).should().findById(RENTAL_ID);
        then(rentalRepository).shouldHaveNoMoreInteractions();
    }

    // Helper methods

    private Rental createDraftRental() {
        return Rental.builder()
                .id(RENTAL_ID)
                .status(RentalStatus.DRAFT)
                .createdAt(Instant.now())
                .build();
    }

    private Rental createDraftRentalWithEquipmentAndDuration() {
        Rental rental = createDraftRental();
        rental.addEquipment(RentalEquipment.assigned(EQUIPMENT_ID, "BIKE-001"));
        rental.setPlannedDuration(DURATION);
        return rental;
    }

    private Rental createDraftRentalWithTariffAndDuration() {
        Rental rental = createDraftRental();
        rental.selectTariff(TARIFF_ID);
        rental.setPlannedDuration(DURATION);
        return rental;
    }

    private Rental createReadyForActivationRental() {
        Rental rental = createDraftRental();
        rental.selectCustomer(CUSTOMER_ID);
        rental.addEquipment(RentalEquipment.assigned(EQUIPMENT_ID, "BIKE-001"));
        rental.selectTariff(TARIFF_ID);
        rental.setPlannedDuration(DURATION);
        rental.setEstimatedCost(Money.of("100.00"));
        return rental;
    }

    private CustomerInfo createCustomerInfo() {
        return new CustomerInfo(
                CUSTOMER_ID,
                "+79995551111",
                "John",
                "Doe",
                "john@example.com",
                null
        );
    }

    private TariffInfo createTariffInfo() {
        return new TariffInfo(
                TARIFF_ID,
                "Standard Rate",
                "bicycle",
                new BigDecimal("100.00"),
                new BigDecimal("100.00"),
                new BigDecimal("100.00"),
                new BigDecimal("500.00"),
                LocalDate.now(),
                null,
                true
        );
    }

}
