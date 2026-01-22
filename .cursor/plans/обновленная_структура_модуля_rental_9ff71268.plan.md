---
name: Обновленная структура модуля Rental
overview: "Полный рефакторинг модуля Rental с применением: MapStruct для всех маппингов, Repository Pattern для абстракции БД, Facade Pattern для межмодульной коммуникации, Value Objects (RentTime, PayAmount и др.), интерфейсов UseCase, ClockService, DomainEventPublisher, Long ID вместо UUID, и исправлением REST endpoints согласно backend-architecture.md."
todos: []
---

# Обновленная структура модуля Rental

## Изменения относительно предыдущей версии

### Ключевые улучшения:

1. **MapStruct** для всех конвертаций (domain ↔ JPA entity, domain ↔ Web DTO, domain ↔ API DTO)
2. **Repository Pattern** с полной абстракцией от JPA
3. **Facade Pattern** для межмодульной коммуникации (публичный API в корне модуля)
4. **DomainEventPublisher** вместо прямого ApplicationEventPublisher
5. **ClockService** вместо LocalDateTime.now()
6. **Value Objects**: RentTime, PayAmount, Money, PhoneNumber, SerialNumber
7. **UseCase интерфейсы** + реализация с суффиксом Service
8. **Long ID** для всех сущностей кроме Customer (UUID)
9. **ZonedDateTime** вместо LocalDateTime
10. **REST endpoints** согласно [docs/backend-architecture.md](docs/backend-architecture.md)

---

## Полная структура модуля Rental

```
com.github.jenkaby.bikerental.rental/
│
├── package-info.java                       # @ApplicationModule
│
├── RentalFacade.java                       # PUBLIC API (Facade для других модулей)
├── RentalInfo.java                         # PUBLIC DTO
├── RentalQuery.java                        # PUBLIC DTO
│
├── event/                                  # PUBLIC (через @NamedInterface)
│   ├── RentalStarted.java
│   ├── RentalCompleted.java
│   └── RentalCancelled.java
│
├── internal/
│   │
│   ├── domain/                             # Чистая бизнес-логика
│   │   ├── model/
│   │   │   ├── Rental.java                 # Aggregate Root (чистый POJO)
│   │   │   ├── RentalStatus.java          # Enum
│   │   │   └── vo/                         # Value Objects
│   │   │       ├── RentTime.java
│   │   │       ├── PayAmount.java
│   │   │       ├── RentalId.java
│   │   │       └── CustomerId.java
│   │   │
│   │   └── repository/
│   │       └── RentalRepository.java       # Domain repository interface
│   │
│   ├── application/                        # Use Cases
│   │   ├── usecase/                        # UseCase интерфейсы
│   │   │   ├── CreateRentalUseCase.java
│   │   │   ├── StartRentalUseCase.java
│   │   │   ├── ReturnEquipmentUseCase.java
│   │   │   ├── CancelRentalUseCase.java
│   │   │   └── RentalQueryUseCase.java
│   │   │
│   │   ├── service/                        # Реализации UseCase
│   │   │   ├── CreateRentalService.java
│   │   │   ├── StartRentalService.java
│   │   │   ├── ReturnEquipmentService.java
│   │   │   ├── CancelRentalService.java
│   │   │   └── RentalQueryService.java
│   │   │
│   │   ├── port/                           # Ports (abstraction)
│   │   │   ├── DomainEventPublisher.java   # Интерфейс для событий
│   │   │   └── ClockService.java           # Интерфейс для времени
│   │   │
│   │   └── mapper/
│   │       └── RentalMapper.java           # MapStruct: domain ↔ DTO
│   │
│   ├── infrastructure/                     # Технические детали
│   │   ├── persistence/
│   │   │   ├── entity/
│   │   │   │   └── RentalJpaEntity.java
│   │   │   ├── repository/
│   │   │   │   └── RentalJpaRepository.java
│   │   │   ├── adapter/
│   │   │   │   └── RentalRepositoryAdapter.java
│   │   │   └── mapper/
│   │   │       └── RentalJpaMapper.java     # MapStruct: domain ↔ JPA
│   │   │
│   │   ├── event/
│   │   │   └── SpringDomainEventPublisher.java  # Реализация через Spring Events
│   │   │
│   │   └── time/
│   │       └── SystemClockService.java      # Реализация ClockService
│   │
│   └── web/
│       ├── command/                        # Command контроллеры
│       │   ├── RentalCommandController.java
│       │   ├── dto/
│       │   │   ├── CreateRentalRequest.java
│       │   │   ├── StartRentalRequest.java
│       │   │   ├── ReturnEquipmentRequest.java
│       │   │   └── CancelRentalRequest.java
│       │   └── mapper/
│       │       └── RentalCommandMapper.java  # MapStruct: Web DTO ↔ UseCase Command
│       │
│       └── query/                          # Query контроллеры
│           ├── RentalQueryController.java
│           ├── dto/
│           │   └── RentalResponse.java
│           └── mapper/
│               └── RentalQueryMapper.java    # MapStruct: RentalInfo ↔ Web Response
```

---

## 1. Публичный API (корень модуля)

### 1.1 package-info.java

```java
/**
 * Модуль управления арендой оборудования.
 * 
 * Публичный API:
 * - {@link com.github.jenkaby.bikerental.rental.RentalFacade}
 * - {@link com.github.jenkaby.bikerental.rental.RentalInfo}
 * - События в пакете event
 */
@ApplicationModule(
    displayName = "Rental Management",
    allowedDependencies = {"client", "equipment", "tariff"}
)
@NamedInterfaces({
    @NamedInterface(name = "events", packages = "event")
})
package com.github.jenkaby.bikerental.rental;

import org.springframework.modulith.ApplicationModule;
import org.springframework.modulith.NamedInterface;
import org.springframework.modulith.NamedInterfaces;
```

### 1.2 RentalFacade.java (Facade для других модулей)

```java
package com.github.jenkaby.bikerental.rental;

import java.util.List;
import java.util.Optional;

/**
 * Публичный фасад модуля Rental.
 * Единственная точка входа для других модулей.
 */
public interface RentalFacade {
    
    /**
     * Найти аренду по ID.
     */
    Optional<RentalInfo> findById(Long rentalId);
    
    /**
     * Найти активные аренды.
     */
    List<RentalInfo> findActiveRentals();
    
    /**
     * Найти активную аренду по оборудованию.
     */
    Optional<RentalInfo> findActiveByEquipmentId(Long equipmentId);
    
    /**
     * Найти аренды клиента.
     */
    List<RentalInfo> findByCustomerId(java.util.UUID customerId);
}
```

### 1.3 RentalInfo.java (публичный DTO)

```java
package com.github.jenkaby.bikerental.rental;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * Публичное представление аренды для других модулей.
 */
public record RentalInfo(
    Long rentalId,
    UUID customerId,
    Long equipmentId,
    String status,
    ZonedDateTime startedAt,
    ZonedDateTime expectedReturnAt,
    ZonedDateTime actualReturnAt,
    BigDecimal prepaidAmount,
    BigDecimal surchargeAmount
) {
}
```

### 1.4 События (event пакет)

```java
package com.github.jenkaby.bikerental.rental.event;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * Событие: аренда запущена.
 * Публикуется после коммита транзакции.
 */
public record RentalStarted(
    Long rentalId,
    UUID customerId,
    Long equipmentId,
    ZonedDateTime startedAt,
    BigDecimal prepaidAmount,
    ZonedDateTime occurredAt
) {
}
```

```java
package com.github.jenkaby.bikerental.rental.event;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

/**
 * Событие: аренда завершена.
 */
public record RentalCompleted(
    Long rentalId,
    Long equipmentId,
    ZonedDateTime returnedAt,
    BigDecimal surchargeAmount,
    ZonedDateTime occurredAt
) {
}
```

```java
package com.github.jenkaby.bikerental.rental.event;

import java.time.ZonedDateTime;

/**
 * Событие: аренда отменена.
 */
public record RentalCancelled(
    Long rentalId,
    Long equipmentId,
    ZonedDateTime cancelledAt,
    String reason,
    ZonedDateTime occurredAt
) {
}
```

---

## 2. Domain Layer (чистая бизнес-логика)

### 2.1 Value Objects

**RentTime.java**:

```java
package com.github.jenkaby.bikerental.rental.internal.domain.model.vo;

import java.time.Duration;
import java.time.ZonedDateTime;

/**
 * Value Object для временных рамок аренды.
 */
public record RentTime(
    ZonedDateTime startedAt,
    ZonedDateTime expectedReturnAt,
    ZonedDateTime actualReturnAt
) {
    public RentTime {
        if (startedAt != null && expectedReturnAt != null) {
            if (expectedReturnAt.isBefore(startedAt)) {
                throw new IllegalArgumentException("Expected return must be after start");
            }
        }
    }
    
    public static RentTime createNew(ZonedDateTime startedAt, int plannedMinutes) {
        return new RentTime(
            startedAt,
            startedAt.plusMinutes(plannedMinutes),
            null
        );
    }
    
    public RentTime withReturn(ZonedDateTime returnedAt) {
        return new RentTime(startedAt, expectedReturnAt, returnedAt);
    }
    
    public long actualMinutes() {
        if (startedAt == null || actualReturnAt == null) {
            return 0;
        }
        return Duration.between(startedAt, actualReturnAt).toMinutes();
    }
    
    public boolean isOvertime() {
        if (expectedReturnAt == null || actualReturnAt == null) {
            return false;
        }
        return actualReturnAt.isAfter(expectedReturnAt);
    }
}
```

**PayAmount.java**:

```java
package com.github.jenkaby.bikerental.rental.internal.domain.model.vo;

import com.github.jenkaby.bikerental.shared.domain.Money;

/**
 * Value Object для сумм оплаты.
 */
public record PayAmount(
    Money prepaid,
    Money surcharge
) {
    public PayAmount(Money prepaid) {
        this(prepaid, Money.ZERO);
    }
    
    public PayAmount withSurcharge(Money surcharge) {
        return new PayAmount(prepaid, surcharge);
    }
    
    public Money total() {
        return prepaid.add(surcharge);
    }
}
```

**RentalId.java**:

```java
package com.github.jenkaby.bikerental.rental.internal.domain.model.vo;

/**
 * Value Object для ID аренды.
 */
public record RentalId(Long value) {
    public RentalId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("Rental ID must be positive");
        }
    }
    
    public static RentalId of(Long value) {
        return new RentalId(value);
    }
}
```

**CustomerId.java**:

```java
package com.github.jenkaby.bikerental.rental.internal.domain.model.vo;

import java.util.UUID;

/**
 * Value Object для ID клиента.
 */
public record CustomerId(UUID value) {
    public CustomerId {
        if (value == null) {
            throw new IllegalArgumentException("Customer ID cannot be null");
        }
    }
    
    public static CustomerId of(UUID value) {
        return new CustomerId(value);
    }
}
```

### 2.2 Rental.java (Aggregate Root - чистый POJO)

```java
package com.github.jenkaby.bikerental.rental.internal.domain.model;

import com.github.jenkaby.bikerental.rental.internal.domain.model.vo.*;
import com.github.jenkaby.bikerental.shared.domain.Money;
import java.time.ZonedDateTime;

/**
 * Агрегат "Аренда".
 * Чистый POJO без зависимости от JPA.
 */
public class Rental {
    
    private RentalId id;
    private final CustomerId customerId;
    private final Long equipmentId;
    private final Long tariffId;
    
    private RentalStatus status;
    private RentTime rentTime;
    private final Integer plannedMinutes;
    private Integer actualMinutes;
    private PayAmount payAmount;
    
    // Конструктор для создания новой аренды
    public Rental(CustomerId customerId, Long equipmentId, Long tariffId,
                  Integer plannedMinutes, Money prepaidAmount) {
        this.customerId = customerId;
        this.equipmentId = equipmentId;
        this.tariffId = tariffId;
        this.plannedMinutes = plannedMinutes;
        this.payAmount = new PayAmount(prepaidAmount);
        this.status = RentalStatus.DRAFT;
        this.rentTime = new RentTime(null, null, null);
    }
    
    // Конструктор для восстановления из БД
    public Rental(RentalId id, CustomerId customerId, Long equipmentId, Long tariffId,
                  RentalStatus status, RentTime rentTime, Integer plannedMinutes,
                  Integer actualMinutes, PayAmount payAmount) {
        this.id = id;
        this.customerId = customerId;
        this.equipmentId = equipmentId;
        this.tariffId = tariffId;
        this.status = status;
        this.rentTime = rentTime;
        this.plannedMinutes = plannedMinutes;
        this.actualMinutes = actualMinutes;
        this.payAmount = payAmount;
    }
    
    /**
     * Бизнес-метод: запуск аренды.
     */
    public void start(ZonedDateTime now) {
        if (status != RentalStatus.DRAFT) {
            throw new IllegalStateException("Rental can only be started from DRAFT status");
        }
        this.rentTime = RentTime.createNew(now, plannedMinutes);
        this.status = RentalStatus.ACTIVE;
    }
    
    /**
     * Бизнес-метод: завершение аренды.
     */
    public void complete(ZonedDateTime now, Integer actualMinutes, Money surcharge) {
        if (status != RentalStatus.ACTIVE) {
            throw new IllegalStateException("Only ACTIVE rental can be completed");
        }
        this.rentTime = rentTime.withReturn(now);
        this.actualMinutes = actualMinutes;
        this.payAmount = payAmount.withSurcharge(surcharge);
        this.status = RentalStatus.COMPLETED;
    }
    
    /**
     * Бизнес-метод: проверка возможности отмены.
     */
    public boolean canBeCancelled(ZonedDateTime now) {
        if (status != RentalStatus.ACTIVE) {
            return false;
        }
        if (rentTime.startedAt() == null) {
            return false;
        }
        // FR-RN-008: отмена в течение 10 минут
        return rentTime.startedAt().plusMinutes(10).isAfter(now);
    }
    
    /**
     * Бизнес-метод: отмена аренды.
     */
    public void cancel() {
        if (status != RentalStatus.ACTIVE) {
            throw new IllegalStateException("Only ACTIVE rental can be cancelled");
        }
        this.status = RentalStatus.CANCELLED;
    }
    
    // Getters
    public RentalId getId() { return id; }
    public CustomerId getCustomerId() { return customerId; }
    public Long getEquipmentId() { return equipmentId; }
    public Long getTariffId() { return tariffId; }
    public RentalStatus getStatus() { return status; }
    public RentTime getRentTime() { return rentTime; }
    public Integer getPlannedMinutes() { return plannedMinutes; }
    public Integer getActualMinutes() { return actualMinutes; }
    public PayAmount getPayAmount() { return payAmount; }
    
    // Package-private setter для ID (после сохранения в БД)
    void setId(RentalId id) {
        this.id = id;
    }
}
```

### 2.3 RentalRepository.java (Domain interface)

```java
package com.github.jenkaby.bikerental.rental.internal.domain.repository;

import com.github.jenkaby.bikerental.rental.internal.domain.model.Rental;
import com.github.jenkaby.bikerental.rental.internal.domain.model.RentalStatus;
import com.github.jenkaby.bikerental.rental.internal.domain.model.vo.*;

import java.util.List;
import java.util.Optional;

/**
 * Repository интерфейс в терминах domain модели.
 * НЕ зависит от Spring Data или JPA.
 */
public interface RentalRepository {
    
    Rental save(Rental rental);
    
    Optional<Rental> findById(RentalId id);
    
    List<Rental> findByStatus(RentalStatus status);
    
    List<Rental> findByCustomerId(CustomerId customerId);
    
    Optional<Rental> findActiveByEquipmentId(Long equipmentId);
    
    void delete(Rental rental);
}
```

---

## 3. Application Layer (Use Cases)

### 3.1 Ports (абстракции)

**DomainEventPublisher.java**:

```java
package com.github.jenkaby.bikerental.rental.internal.application.port;

/**
 * Абстракция для публикации доменных событий.
 * Application слой НЕ знает об ApplicationEventPublisher.
 */
public interface DomainEventPublisher {
    
    /**
     * Публикует событие асинхронно после коммита транзакции.
     */
    void publishAfterCommit(Object event);
}
```

**ClockService.java**:

```java
package com.github.jenkaby.bikerental.rental.internal.application.port;

import java.time.ZonedDateTime;

/**
 * Абстракция для получения текущего времени.
 * Позволяет легко мокировать время в тестах.
 */
public interface ClockService {
    
    /**
     * Возвращает текущее время в системной зоне.
     */
    ZonedDateTime now();
    
    /**
     * Возвращает текущее время в указанной зоне.
     */
    ZonedDateTime now(java.time.ZoneId zone);
}
```

### 3.2 UseCase интерфейсы

**CreateRentalUseCase.java**:

```java
package com.github.jenkaby.bikerental.rental.internal.application.usecase;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * UC-001: Создание новой аренды.
 */
public interface CreateRentalUseCase {
    
    Long execute(CreateRentalCommand command);
    
    record CreateRentalCommand(
        UUID customerId,
        String equipmentNumber,
        RentalPeriod rentalPeriod
    ) {}
    
    enum RentalPeriod {
        HOUR_1(60),
        HOUR_2(120),
        DAY(1440);
        
        private final int minutes;
        
        RentalPeriod(int minutes) {
            this.minutes = minutes;
        }
        
        public int toMinutes() {
            return minutes;
        }
    }
}
```

**StartRentalUseCase.java**:

```java
package com.github.jenkaby.bikerental.rental.internal.application.usecase;

/**
 * UC-002: Запуск аренды.
 */
public interface StartRentalUseCase {
    
    void execute(StartRentalCommand command);
    
    record StartRentalCommand(Long rentalId) {}
}
```

**ReturnEquipmentUseCase.java**:

```java
package com.github.jenkaby.bikerental.rental.internal.application.usecase;

import java.math.BigDecimal;

/**
 * UC-003: Возврат оборудования.
 */
public interface ReturnEquipmentUseCase {
    
    ReturnResult execute(ReturnEquipmentCommand command);
    
    record ReturnEquipmentCommand(Long rentalId) {}
    
    record ReturnResult(
        Long rentalId,
        int actualMinutes,
        boolean overtimeForgiven,
        BigDecimal surcharge
    ) {}
}
```

**CancelRentalUseCase.java**:

```java
package com.github.jenkaby.bikerental.rental.internal.application.usecase;

/**
 * UC-004: Отмена аренды.
 */
public interface CancelRentalUseCase {
    
    void execute(CancelRentalCommand command);
    
    record CancelRentalCommand(Long rentalId, String reason) {}
}
```

**RentalQueryUseCase.java**:

```java
package com.github.jenkaby.bikerental.rental.internal.application.usecase;

import com.github.jenkaby.bikerental.rental.RentalInfo;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * UC-Query: Запросы к арендам.
 */
public interface RentalQueryUseCase {
    
    Optional<RentalInfo> findById(Long rentalId);
    
    List<RentalInfo> findActiveRentals();
    
    Optional<RentalInfo> findActiveByEquipmentId(Long equipmentId);
    
    List<RentalInfo> findByCustomerId(UUID customerId);
}
```

### 3.3 Реализации UseCase (Services)

**CreateRentalService.java**:

```java
package com.github.jenkaby.bikerental.rental.internal.application.service;

import com.github.jenkaby.bikerental.client.api.CustomerLookupService;
import com.github.jenkaby.bikerental.equipment.api.EquipmentAvailabilityService;
import com.github.jenkaby.bikerental.equipment.api.EquipmentRef;
import com.github.jenkaby.bikerental.tariff.api.TariffSelectionService;
import com.github.jenkaby.bikerental.tariff.api.TariffInfo;
import com.github.jenkaby.bikerental.rental.internal.application.usecase.CreateRentalUseCase;
import com.github.jenkaby.bikerental.rental.internal.domain.model.Rental;
import com.github.jenkaby.bikerental.rental.internal.domain.model.vo.CustomerId;
import com.github.jenkaby.bikerental.rental.internal.domain.repository.RentalRepository;
import com.github.jenkaby.bikerental.shared.domain.Money;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Реализация UC-001: Создание аренды.
 */
@Service
class CreateRentalService implements CreateRentalUseCase {
    
    private final RentalRepository rentalRepository;
    private final CustomerLookupService customerService;
    private final EquipmentAvailabilityService equipmentService;
    private final TariffSelectionService tariffService;
    
    CreateRentalService(
        RentalRepository rentalRepository,
        CustomerLookupService customerService,
        EquipmentAvailabilityService equipmentService,
        TariffSelectionService tariffService
    ) {
        this.rentalRepository = rentalRepository;
        this.customerService = customerService;
        this.equipmentService = equipmentService;
        this.tariffService = tariffService;
    }
    
    @Override
    @Transactional
    public Long execute(CreateRentalCommand command) {
        // 1. Проверка клиента
        var customer = customerService.findById(command.customerId())
            .orElseThrow(() -> new CustomerNotFoundException(command.customerId()));
        
        // 2. Проверка доступности оборудования
        EquipmentRef equipment = equipmentService.findAvailableByNumber(command.equipmentNumber())
            .orElseThrow(() -> new EquipmentNotAvailableException(command.equipmentNumber()));
        
        // 3. Автоматический подбор тарифа (FR-TR-002)
        TariffInfo tariff = tariffService.selectTariff(
            equipment.typeId(),
            command.rentalPeriod().toMinutes()
        );
        
        // 4. Создание domain объекта
        Integer plannedMinutes = command.rentalPeriod().toMinutes();
        Money prepaidAmount = Money.of(tariff.basePrice());
        
        Rental rental = new Rental(
            CustomerId.of(customer.id()),
            equipment.id(),
            tariff.id(),
            plannedMinutes,
            prepaidAmount
        );
        
        // 5. Сохранение
        Rental saved = rentalRepository.save(rental);
        
        return saved.getId().value();
    }
}
```

**StartRentalService.java**:

```java
package com.github.jenkaby.bikerental.rental.internal.application.service;

import com.github.jenkaby.bikerental.rental.event.RentalStarted;
import com.github.jenkaby.bikerental.rental.internal.application.port.ClockService;
import com.github.jenkaby.bikerental.rental.internal.application.port.DomainEventPublisher;
import com.github.jenkaby.bikerental.rental.internal.application.usecase.StartRentalUseCase;
import com.github.jenkaby.bikerental.rental.internal.domain.model.Rental;
import com.github.jenkaby.bikerental.rental.internal.domain.model.vo.RentalId;
import com.github.jenkaby.bikerental.rental.internal.domain.repository.RentalRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Реализация UC-002: Запуск аренды.
 */
@Service
class StartRentalService implements StartRentalUseCase {
    
    private final RentalRepository rentalRepository;
    private final DomainEventPublisher eventPublisher;
    private final ClockService clockService;
    
    StartRentalService(
        RentalRepository rentalRepository,
        DomainEventPublisher eventPublisher,
        ClockService clockService
    ) {
        this.rentalRepository = rentalRepository;
        this.eventPublisher = eventPublisher;
        this.clockService = clockService;
    }
    
    @Override
    @Transactional
    public void execute(StartRentalCommand command) {
        // 1. Загрузка аренды
        Rental rental = rentalRepository.findById(RentalId.of(command.rentalId()))
            .orElseThrow(() -> new RentalNotFoundException(command.rentalId()));
        
        // 2. Бизнес-логика старта
        rental.start(clockService.now());
        
        // 3. Сохранение
        rentalRepository.save(rental);
        
        // 4. Публикация события (асинхронно после коммита)
        RentalStarted event = new RentalStarted(
            rental.getId().value(),
            rental.getCustomerId().value(),
            rental.getEquipmentId(),
            rental.getRentTime().startedAt(),
            rental.getPayAmount().prepaid().amount(),
            clockService.now()
        );
        eventPublisher.publishAfterCommit(event);
    }
}
```

**ReturnEquipmentService.java**:

```java
package com.github.jenkaby.bikerental.rental.internal.application.service;

import com.github.jenkaby.bikerental.rental.event.RentalCompleted;
import com.github.jenkaby.bikerental.rental.internal.application.port.ClockService;
import com.github.jenkaby.bikerental.rental.internal.application.port.DomainEventPublisher;
import com.github.jenkaby.bikerental.rental.internal.application.usecase.ReturnEquipmentUseCase;
import com.github.jenkaby.bikerental.rental.internal.domain.model.Rental;
import com.github.jenkaby.bikerental.rental.internal.domain.model.vo.RentalId;
import com.github.jenkaby.bikerental.rental.internal.domain.repository.RentalRepository;
import com.github.jenkaby.bikerental.tariff.api.RentalCostCalculator;
import com.github.jenkaby.bikerental.tariff.api.RentalCostDetails;
import com.github.jenkaby.bikerental.shared.domain.Money;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

/**
 * Реализация UC-003: Возврат оборудования.
 */
@Service
class ReturnEquipmentService implements ReturnEquipmentUseCase {
    
    private final RentalRepository rentalRepository;
    private final RentalCostCalculator costCalculator;
    private final DomainEventPublisher eventPublisher;
    private final ClockService clockService;
    
    ReturnEquipmentService(
        RentalRepository rentalRepository,
        RentalCostCalculator costCalculator,
        DomainEventPublisher eventPublisher,
        ClockService clockService
    ) {
        this.rentalRepository = rentalRepository;
        this.costCalculator = costCalculator;
        this.eventPublisher = eventPublisher;
        this.clockService = clockService;
    }
    
    @Override
    @Transactional
    public ReturnResult execute(ReturnEquipmentCommand command) {
        // 1. Загрузка аренды
        Rental rental = rentalRepository.findById(RentalId.of(command.rentalId()))
            .orElseThrow(() -> new RentalNotFoundException(command.rentalId()));
        
        var now = clockService.now();
        
        // 2. Расчет фактического времени
        long actualMinutes = Duration.between(
            rental.getRentTime().startedAt(),
            now
        ).toMinutes();
        
        // 3. Расчет стоимости через Tariff модуль
        RentalCostDetails costDetails = costCalculator.calculate(
            rental.getTariffId(),
            rental.getPlannedMinutes(),
            (int) actualMinutes
        );
        
        // 4. Завершение аренды (domain logic)
        rental.complete(
            now,
            costDetails.actualMinutes(),
            Money.of(costDetails.surcharge())
        );
        
        // 5. Сохранение
        rentalRepository.save(rental);
        
        // 6. Публикация события
        RentalCompleted event = new RentalCompleted(
            rental.getId().value(),
            rental.getEquipmentId(),
            now,
            costDetails.surcharge(),
            clockService.now()
        );
        eventPublisher.publishAfterCommit(event);
        
        // 7. Возврат результата
        return new ReturnResult(
            rental.getId().value(),
            costDetails.actualMinutes(),
            costDetails.isForgiven(),
            costDetails.surcharge()
        );
    }
}
```

**RentalQueryService.java**:

```java
package com.github.jenkaby.bikerental.rental.internal.application.service;

import com.github.jenkaby.bikerental.rental.RentalInfo;
import com.github.jenkaby.bikerental.rental.internal.application.mapper.RentalMapper;
import com.github.jenkaby.bikerental.rental.internal.application.usecase.RentalQueryUseCase;
import com.github.jenkaby.bikerental.rental.internal.domain.model.RentalStatus;
import com.github.jenkaby.bikerental.rental.internal.domain.model.vo.CustomerId;
import com.github.jenkaby.bikerental.rental.internal.domain.model.vo.RentalId;
import com.github.jenkaby.bikerental.rental.internal.domain.repository.RentalRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Реализация UC-Query: Запросы к арендам.
 */
@Service
@Transactional(readOnly = true)
class RentalQueryService implements RentalQueryUseCase {
    
    private final RentalRepository rentalRepository;
    private final RentalMapper rentalMapper;
    
    RentalQueryService(RentalRepository rentalRepository, RentalMapper rentalMapper) {
        this.rentalRepository = rentalRepository;
        this.rentalMapper = rentalMapper;
    }
    
    @Override
    public Optional<RentalInfo> findById(Long rentalId) {
        return rentalRepository.findById(RentalId.of(rentalId))
            .map(rentalMapper::toRentalInfo);
    }
    
    @Override
    public List<RentalInfo> findActiveRentals() {
        return rentalRepository.findByStatus(RentalStatus.ACTIVE)
            .stream()
            .map(rentalMapper::toRentalInfo)
            .toList();
    }
    
    @Override
    public Optional<RentalInfo> findActiveByEquipmentId(Long equipmentId) {
        return rentalRepository.findActiveByEquipmentId(equipmentId)
            .map(rentalMapper::toRentalInfo);
    }
    
    @Override
    public List<RentalInfo> findByCustomerId(UUID customerId) {
        return rentalRepository.findByCustomerId(CustomerId.of(customerId))
            .stream()
            .map(rentalMapper::toRentalInfo)
            .toList();
    }
}
```

### 3.4 Application Mapper (MapStruct)

**RentalMapper.java**:

```java
package com.github.jenkaby.bikerental.rental.internal.application.mapper;

import com.github.jenkaby.bikerental.rental.RentalInfo;
import com.github.jenkaby.bikerental.rental.internal.domain.model.Rental;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

/**
 * MapStruct маппер: Domain → Public DTO.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface RentalMapper {
    
    @Mapping(source = "id.value", target = "rentalId")
    @Mapping(source = "customerId.value", target = "customerId")
    @Mapping(source = "rentTime.startedAt", target = "startedAt")
    @Mapping(source = "rentTime.expectedReturnAt", target = "expectedReturnAt")
    @Mapping(source = "rentTime.actualReturnAt", target = "actualReturnAt")
    @Mapping(source = "payAmount.prepaid.amount", target = "prepaidAmount")
    @Mapping(source = "payAmount.surcharge.amount", target = "surchargeAmount")
    @Mapping(source = "status", target = "status")
    RentalInfo toRentalInfo(Rental rental);
}
```

---

## 4. Infrastructure Layer (технические детали)

### 4.1 JPA Entity

**RentalJpaEntity.java**:

```java
package com.github.jenkaby.bikerental.rental.internal.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * JPA Entity для персистентности аренды.
 * Используется только в infrastructure слое.
 */
@Entity
@Table(name = "rentals")
class RentalJpaEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "customer_id", nullable = false)
    private UUID customerId;
    
    @Column(name = "equipment_id", nullable = false)
    private Long equipmentId;
    
    @Column(name = "tariff_id", nullable = false)
    private Long tariffId;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private String status;
    
    @Column(name = "started_at")
    private ZonedDateTime startedAt;
    
    @Column(name = "expected_return_at")
    private ZonedDateTime expectedReturnAt;
    
    @Column(name = "actual_return_at")
    private ZonedDateTime actualReturnAt;
    
    @Column(name = "planned_minutes", nullable = false)
    private Integer plannedMinutes;
    
    @Column(name = "actual_minutes")
    private Integer actualMinutes;
    
    @Column(name = "prepaid_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal prepaidAmount;
    
    @Column(name = "surcharge_amount", precision = 10, scale = 2)
    private BigDecimal surchargeAmount;
    
    // Getters/Setters для JPA
    // ...
}
```

### 4.2 JPA Repository (Spring Data)

**RentalJpaRepository.java**:

```java
package com.github.jenkaby.bikerental.rental.internal.infrastructure.persistence.repository;

import com.github.jenkaby.bikerental.rental.internal.infrastructure.persistence.entity.RentalJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository.
 */
interface RentalJpaRepository extends JpaRepository<RentalJpaEntity, Long> {
    
    List<RentalJpaEntity> findByStatus(String status);
    
    List<RentalJpaEntity> findByCustomerId(UUID customerId);
    
    @Query("SELECT r FROM RentalJpaEntity r WHERE r.equipmentId = :equipmentId AND r.status = 'ACTIVE'")
    Optional<RentalJpaEntity> findActiveByEquipmentId(Long equipmentId);
}
```

### 4.3 Repository Adapter

**RentalRepositoryAdapter.java**:

```java
package com.github.jenkaby.bikerental.rental.internal.infrastructure.persistence.adapter;

import com.github.jenkaby.bikerental.rental.internal.domain.model.Rental;
import com.github.jenkaby.bikerental.rental.internal.domain.model.RentalStatus;
import com.github.jenkaby.bikerental.rental.internal.domain.model.vo.*;
import com.github.jenkaby.bikerental.rental.internal.domain.repository.RentalRepository;
import com.github.jenkaby.bikerental.rental.internal.infrastructure.persistence.entity.RentalJpaEntity;
import com.github.jenkaby.bikerental.rental.internal.infrastructure.persistence.mapper.RentalJpaMapper;
import com.github.jenkaby.bikerental.rental.internal.infrastructure.persistence.repository.RentalJpaRepository;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Адаптер между domain repository и JPA.
 */
@Repository
class RentalRepositoryAdapter implements RentalRepository {
    
    private final RentalJpaRepository jpaRepository;
    private final RentalJpaMapper mapper;
    
    RentalRepositoryAdapter(RentalJpaRepository jpaRepository, RentalJpaMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }
    
    @Override
    public Rental save(Rental rental) {
        RentalJpaEntity entity = mapper.toEntity(rental);
        RentalJpaEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }
    
    @Override
    public Optional<Rental> findById(RentalId id) {
        return jpaRepository.findById(id.value())
            .map(mapper::toDomain);
    }
    
    @Override
    public List<Rental> findByStatus(RentalStatus status) {
        return jpaRepository.findByStatus(status.name())
            .stream()
            .map(mapper::toDomain)
            .toList();
    }
    
    @Override
    public List<Rental> findByCustomerId(CustomerId customerId) {
        return jpaRepository.findByCustomerId(customerId.value())
            .stream()
            .map(mapper::toDomain)
            .toList();
    }
    
    @Override
    public Optional<Rental> findActiveByEquipmentId(Long equipmentId) {
        return jpaRepository.findActiveByEquipmentId(equipmentId)
            .map(mapper::toDomain);
    }
    
    @Override
    public void delete(Rental rental) {
        jpaRepository.deleteById(rental.getId().value());
    }
}
```

### 4.4 JPA Mapper (MapStruct)

**RentalJpaMapper.java**:

```java
package com.github.jenkaby.bikerental.rental.internal.infrastructure.persistence.mapper;

import com.github.jenkaby.bikerental.rental.internal.domain.model.Rental;
import com.github.jenkaby.bikerental.rental.internal.domain.model.RentalStatus;
import com.github.jenkaby.bikerental.rental.internal.domain.model.vo.*;
import com.github.jenkaby.bikerental.rental.internal.infrastructure.persistence.entity.RentalJpaEntity;
import com.github.jenkaby.bikerental.shared.domain.Money;

import org.mapstruct.*;

/**
 * MapStruct маппер: Domain ↔ JPA Entity.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface RentalJpaMapper {
    
    @Mapping(source = "id.value", target = "id")
    @Mapping(source = "customerId.value", target = "customerId")
    @Mapping(source = "rentTime.startedAt", target = "startedAt")
    @Mapping(source = "rentTime.expectedReturnAt", target = "expectedReturnAt")
    @Mapping(source = "rentTime.actualReturnAt", target = "actualReturnAt")
    @Mapping(source = "payAmount.prepaid", target = "prepaidAmount", qualifiedByName = "moneyToDecimal")
    @Mapping(source = "payAmount.surcharge", target = "surchargeAmount", qualifiedByName = "moneyToDecimal")
    @Mapping(source = "status", target = "status", qualifiedByName = "statusToString")
    RentalJpaEntity toEntity(Rental rental);
    
    @Mapping(source = "id", target = "id", qualifiedByName = "toRentalId")
    @Mapping(source = "customerId", target = "customerId", qualifiedByName = "toCustomerId")
    @Mapping(target = "rentTime", source = ".", qualifiedByName = "toRentTime")
    @Mapping(target = "payAmount", source = ".", qualifiedByName = "toPayAmount")
    @Mapping(source = "status", target = "status", qualifiedByName = "stringToStatus")
    Rental toDomain(RentalJpaEntity entity);
    
    @Named("moneyToDecimal")
    default java.math.BigDecimal moneyToDecimal(Money money) {
        return money != null ? money.amount() : null;
    }
    
    @Named("toRentalId")
    default RentalId toRentalId(Long id) {
        return id != null ? RentalId.of(id) : null;
    }
    
    @Named("toCustomerId")
    default CustomerId toCustomerId(java.util.UUID uuid) {
        return CustomerId.of(uuid);
    }
    
    @Named("toRentTime")
    default RentTime toRentTime(RentalJpaEntity entity) {
        return new RentTime(
            entity.getStartedAt(),
            entity.getExpectedReturnAt(),
            entity.getActualReturnAt()
        );
    }
    
    @Named("toPayAmount")
    default PayAmount toPayAmount(RentalJpaEntity entity) {
        Money prepaid = Money.of(entity.getPrepaidAmount());
        Money surcharge = entity.getSurchargeAmount() != null 
            ? Money.of(entity.getSurchargeAmount()) 
            : Money.ZERO;
        return new PayAmount(prepaid, surcharge);
    }
    
    @Named("statusToString")
    default String statusToString(RentalStatus status) {
        return status.name();
    }
    
    @Named("stringToStatus")
    default RentalStatus stringToStatus(String status) {
        return RentalStatus.valueOf(status);
    }
}
```

### 4.5 Event Publisher (реализация)

**SpringDomainEventPublisher.java**:

```java
package com.github.jenkaby.bikerental.rental.internal.infrastructure.event;

import com.github.jenkaby.bikerental.rental.internal.application.port.DomainEventPublisher;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalApplicationListenerMethodAdapter;

/**
 * Реализация DomainEventPublisher через Spring ApplicationEventPublisher.
 * Application слой НЕ знает о Spring Events.
 */
@Component
class SpringDomainEventPublisher implements DomainEventPublisher {
    
    private final ApplicationEventPublisher applicationEventPublisher;
    
    SpringDomainEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }
    
    @Override
    public void publishAfterCommit(Object event) {
        // Публикация через Spring, событие будет обработано после коммита
        applicationEventPublisher.publishEvent(event);
    }
}
```

### 4.6 Clock Service (реализация)

**SystemClockService.java**:

```java
package com.github.jenkaby.bikerental.rental.internal.infrastructure.time;

import com.github.jenkaby.bikerental.rental.internal.application.port.ClockService;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * Реализация ClockService через java.time.Clock.
 * Позволяет легко мокировать в тестах.
 */
@Service
public class SystemClockService implements ClockService {
    
    private final Clock clock;
    
    public SystemClockService() {
        this.clock = Clock.systemDefaultZone();
    }
    
    // Конструктор для тестов (можно передать фиксированный Clock)
    public SystemClockService(Clock clock) {
        this.clock = clock;
    }
    
    @Override
    public ZonedDateTime now() {
        return ZonedDateTime.now(clock);
    }
    
    @Override
    public ZonedDateTime now(ZoneId zone) {
        return ZonedDateTime.now(clock.withZone(zone));
    }
}
```

---

## 5. Web Layer (REST API)

### 5.1 RentalController.java (исправленные endpoints)

```java
package com.github.jenkaby.bikerental.rental.internal.web;

import com.github.jenkaby.bikerental.rental.RentalInfo;
import com.github.jenkaby.bikerental.rental.internal.application.usecase.*;
import com.github.jenkaby.bikerental.rental.internal.web.dto.*;
import com.github.jenkaby.bikerental.rental.internal.web.mapper.RentalWebMapper;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST API для управления арендами.
 * Endpoints согласно backend-architecture.md.
 */
@RestController
@RequestMapping("/api/rentals")
class RentalController {
    
    private final CreateRentalUseCase createRentalUseCase;
    private final StartRentalUseCase startRentalUseCase;
    private final ReturnEquipmentUseCase returnEquipmentUseCase;
    private final CancelRentalUseCase cancelRentalUseCase;
    private final RentalQueryUseCase rentalQueryUseCase;
    private final RentalWebMapper webMapper;
    
    RentalController(
        CreateRentalUseCase createRentalUseCase,
        StartRentalUseCase startRentalUseCase,
        ReturnEquipmentUseCase returnEquipmentUseCase,
        CancelRentalUseCase cancelRentalUseCase,
        RentalQueryUseCase rentalQueryUseCase,
        RentalWebMapper webMapper
    ) {
        this.createRentalUseCase = createRentalUseCase;
        this.startRentalUseCase = startRentalUseCase;
        this.returnEquipmentUseCase = returnEquipmentUseCase;
        this.cancelRentalUseCase = cancelRentalUseCase;
        this.rentalQueryUseCase = rentalQueryUseCase;
        this.webMapper = webMapper;
    }
    
    /**
     * POST /api/rentals - Создание аренды
     */
    @PostMapping
    public ResponseEntity<RentalResponse> createRental(
        @Valid @RequestBody CreateRentalRequest request
    ) {
        var command = webMapper.toCreateCommand(request);
        Long rentalId = createRentalUseCase.execute(command);
        
        RentalInfo rental = rentalQueryUseCase.findById(rentalId)
            .orElseThrow();
        
        RentalResponse response = webMapper.toResponse(rental);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * POST /api/rentals/{id}/start - Запуск аренды
     */
    @PostMapping("/{id}/start")
    public ResponseEntity<Void> startRental(@PathVariable Long id) {
        var command = new StartRentalUseCase.StartRentalCommand(id);
        startRentalUseCase.execute(command);
        return ResponseEntity.ok().build();
    }
    
    /**
     * POST /api/rentals/{id}/return - Возврат оборудования
     */
    @PostMapping("/{id}/return")
    public ResponseEntity<ReturnEquipmentResponse> returnEquipment(@PathVariable Long id) {
        var command = new ReturnEquipmentUseCase.ReturnEquipmentCommand(id);
        var result = returnEquipmentUseCase.execute(command);
        
        var response = webMapper.toReturnResponse(result);
        return ResponseEntity.ok(response);
    }
    
    /**
     * POST /api/rentals/{id}/cancel - Отмена аренды
     */
    @PostMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelRental(
        @PathVariable Long id,
        @RequestBody(required = false) CancelRentalRequest request
    ) {
        String reason = request != null ? request.reason() : "Отмена по запросу клиента";
        var command = new CancelRentalUseCase.CancelRentalCommand(id, reason);
        cancelRentalUseCase.execute(command);
        return ResponseEntity.ok().build();
    }
    
    /**
     * GET /api/rentals/active - Список активных аренд
     */
    @GetMapping("/active")
    public ResponseEntity<List<RentalResponse>> getActiveRentals() {
        List<RentalResponse> rentals = rentalQueryUseCase.findActiveRentals()
            .stream()
            .map(webMapper::toResponse)
            .toList();
        
        return ResponseEntity.ok(rentals);
    }
    
    /**
     * GET /api/rentals/{id} - Получение аренды по ID
     * (дополнительный endpoint, не в спецификации, но полезен)
     */
    @GetMapping("/{id}")
    public ResponseEntity<RentalResponse> getRental(@PathVariable Long id) {
        return rentalQueryUseCase.findById(id)
            .map(webMapper::toResponse)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
}
```

### 5.2 Web DTOs

**CreateRentalRequest.java**:

```java
package com.github.jenkaby.bikerental.rental.internal.web.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateRentalRequest(
    @NotNull UUID customerId,
    @NotNull String equipmentNumber,
    @NotNull String rentalPeriod  // "HOUR_1", "HOUR_2", "DAY"
) {}
```

**RentalResponse.java**:

```java
package com.github.jenkaby.bikerental.rental.internal.web.dto;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

public record RentalResponse(
    Long id,
    UUID customerId,
    Long equipmentId,
    String status,
    ZonedDateTime startedAt,
    ZonedDateTime expectedReturnAt,
    ZonedDateTime actualReturnAt,
    BigDecimal prepaidAmount,
    BigDecimal surchargeAmount,
    BigDecimal totalAmount
) {}
```

**ReturnEquipmentResponse.java**:

```java
package com.github.jenkaby.bikerental.rental.internal.web.dto;

import java.math.BigDecimal;

public record ReturnEquipmentResponse(
    Long rentalId,
    int actualMinutes,
    boolean overtimeForgiven,
    BigDecimal surcharge
) {}
```

**CancelRentalRequest.java**:

```java
package com.github.jenkaby.bikerental.rental.internal.web.dto;

public record CancelRentalRequest(String reason) {}
```

### 5.3 Web Mapper (MapStruct)

**RentalWebMapper.java**:

```java
package com.github.jenkaby.bikerental.rental.internal.web.mapper;

import com.github.jenkaby.bikerental.rental.RentalInfo;
import com.github.jenkaby.bikerental.rental.internal.application.usecase.CreateRentalUseCase;
import com.github.jenkaby.bikerental.rental.internal.application.usecase.ReturnEquipmentUseCase;
import com.github.jenkaby.bikerental.rental.internal.web.dto.*;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

/**
 * MapStruct маппер: Web DTO ↔ Domain/UseCase.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface RentalWebMapper {
    
    @Mapping(target = "rentalPeriod", expression = "java(mapRentalPeriod(request.rentalPeriod()))")
    CreateRentalUseCase.CreateRentalCommand toCreateCommand(CreateRentalRequest request);
    
    @Mapping(source = "rentalId", target = "id")
    @Mapping(target = "totalAmount", expression = "java(calculateTotal(rentalInfo))")
    RentalResponse toResponse(RentalInfo rentalInfo);
    
    ReturnEquipmentResponse toReturnResponse(ReturnEquipmentUseCase.ReturnResult result);
    
    default CreateRentalUseCase.RentalPeriod mapRentalPeriod(String period) {
        return CreateRentalUseCase.RentalPeriod.valueOf(period);
    }
    
    default java.math.BigDecimal calculateTotal(RentalInfo info) {
        java.math.BigDecimal total = info.prepaidAmount();
        if (info.surchargeAmount() != null) {
            total = total.add(info.surchargeAmount());
        }
        return total;
    }
}
```

---

## 6. Facade Implementation (реализация публичного API)

**RentalFacadeImpl.java** (в корне модуля):

```java
package com.github.jenkaby.bikerental.rental;

import com.github.jenkaby.bikerental.rental.internal.application.usecase.RentalQueryUseCase;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Реализация фасада модуля Rental.
 */
@Service
class RentalFacadeImpl implements RentalFacade {
    
    private final RentalQueryUseCase rentalQueryUseCase;
    
    RentalFacadeImpl(RentalQueryUseCase rentalQueryUseCase) {
        this.rentalQueryUseCase = rentalQueryUseCase;
    }
    
    @Override
    public Optional<RentalInfo> findById(Long rentalId) {
        return rentalQueryUseCase.findById(rentalId);
    }
    
    @Override
    public List<RentalInfo> findActiveRentals() {
        return rentalQueryUseCase.findActiveRentals();
    }
    
    @Override
    public Optional<RentalInfo> findActiveByEquipmentId(Long equipmentId) {
        return rentalQueryUseCase.findActiveByEquipmentId(equipmentId);
    }
    
    @Override
    public List<RentalInfo> findByCustomerId(UUID customerId) {
        return rentalQueryUseCase.findByCustomerId(customerId);
    }
}
```

---

## 7. Shared Kernel (общие VO)

### Money.java (в shared модуле)

```java
package com.github.jenkaby.bikerental.shared.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Value Object для денежных сумм.
 */
public record Money(BigDecimal amount) {
    
    public static final Money ZERO = new Money(BigDecimal.ZERO);
    
    public Money {
        if (amount == null) {
            throw new IllegalArgumentException("Amount cannot be null");
        }
        amount = amount.setScale(2, RoundingMode.HALF_UP);
    }
    
    public static Money of(BigDecimal amount) {
        return new Money(amount);
    }
    
    public static Money of(String amount) {
        return new Money(new BigDecimal(amount));
    }
    
    public Money add(Money other) {
        return new Money(this.amount.add(other.amount));
    }
    
    public Money subtract(Money other) {
        return new Money(this.amount.subtract(other.amount));
    }
    
    public Money multiply(BigDecimal factor) {
        return new Money(this.amount.multiply(factor));
    }
    
    public boolean isGreaterThan(Money other) {
        return this.amount.compareTo(other.amount) > 0;
    }
    
    public boolean isZero() {
        return this.amount.compareTo(BigDecimal.ZERO) == 0;
    }
}
```

---

## 8. Сравнение со старой структурой

| Аспект | Старая версия | Новая версия |

|--------|---------------|--------------|

| **Маппинг** | Ручные мапперы | MapStruct на всех слоях |

| **БД абстракция** | JPA аннотации в domain | Repository Pattern + adapter |

| **Межмодульное API** | Пакет `api/` | Facade в корне модуля |

| **События** | ApplicationEventPublisher | DomainEventPublisher (port) |

| **Время** | LocalDateTime.now() | ClockService interface |

| **UseCase** | Только классы | Интерфейсы + Service реализация |

| **ID типы** | UUID везде | Long везде, кроме Customer (UUID) |

| **Value Objects** | Нет | RentTime, PayAmount, Money, IDs |

| **DateTime** | LocalDateTime | ZonedDateTime |

| **Endpoints** | Расхождения | Согласно backend-architecture.md |

---

## 9. Использование из другого модуля (Finance)

```java
package com.github.jenkaby.bikerental.finance.internal.application;

import com.github.jenkaby.bikerental.rental.RentalFacade;
import com.github.jenkaby.bikerental.rental.RentalInfo;
import com.github.jenkaby.bikerental.rental.event.RentalStarted;

import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Finance модуль слушает события от Rental модуля.
 */
@Service
class RentalEventHandler {
    
    private final RentalFacade rentalFacade;  // Использование фасада
    private final PaymentService paymentService;
    
    RentalEventHandler(RentalFacade rentalFacade, PaymentService paymentService) {
        this.rentalFacade = rentalFacade;
        this.paymentService = paymentService;
    }
    
    /**
     * Асинхронная обработка после коммита транзакции.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    void on(RentalStarted event) {
        // Получение дополнительной информации через фасад
        RentalInfo rental = rentalFacade.findById(event.rentalId())
            .orElseThrow();
        
        // Регистрация предоплаты
        paymentService.recordPrepayment(
            event.rentalId(),
            event.prepaidAmount(),
            PaymentType.PREPAYMENT
        );
    }
}
```

---

## Итого

Обновленная структура модуля обеспечивает:

1. **Чистую архитектуру** — domain не зависит от инфраструктуры
2. **Тестируемость** — все зависимости через интерфейсы
3. **Гибкость** — легко заменить БД, event bus, clock
4. **Модульность** — четкие границы через Facade Pattern
5. **Производительность** — MapStruct генерирует быстрый код
6. **Безопасность типов** — Value Objects предотвращают ошибки
7. **Масштабируемость** — UseCase как точки расширения