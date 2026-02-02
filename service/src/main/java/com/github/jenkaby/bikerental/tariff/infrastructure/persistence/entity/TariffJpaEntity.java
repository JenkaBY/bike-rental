package com.github.jenkaby.bikerental.tariff.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "tariffs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TariffJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(name = "equipment_type_slug", nullable = false, length = 50)
    private String equipmentTypeSlug;

    @Column(name = "base_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal basePrice;

    @Column(name = "half_hour_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal halfHourPrice;

    @Column(name = "hour_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal hourPrice;

    @Column(name = "day_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal dayPrice;

    @Column(name = "hour_discounted_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal hourDiscountedPrice;

    @Column(name = "valid_from", nullable = false)
    private LocalDate validFrom;

    @Column(name = "valid_to")
    private LocalDate validTo;

    @Column(name = "status", nullable = false, length = 50)
    private String status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
            updatedAt = createdAt;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
