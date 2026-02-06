package com.github.jenkaby.bikerental.rental.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "rentals")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class RentalJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_id")
    private UUID customerId;

    @Column(name = "equipment_id")
    private Long equipmentId;

    @Column(name = "tariff_id")
    private Long tariffId;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "expected_return_at")
    private LocalDateTime expectedReturnAt;

    @Column(name = "actual_return_at")
    private LocalDateTime actualReturnAt;

    @Column(name = "planned_duration_minutes")
    private Integer plannedDurationMinutes;

    @Column(name = "actual_duration_minutes")
    private Integer actualDurationMinutes;

    @Column(name = "estimated_cost", precision = 10, scale = 2)
    private BigDecimal estimatedCost;

    @Column(name = "final_cost", precision = 10, scale = 2)
    private BigDecimal finalCost;

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
