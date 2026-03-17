package com.github.jenkaby.bikerental.rental.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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

    @Fetch(FetchMode.SUBSELECT)
    @OneToMany(mappedBy = "rental", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RentalEquipmentJpaEntity> rentalEquipments = new ArrayList<>();

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

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    public BigDecimal getEstimatedCost() {
        return this.rentalEquipments.stream()
                .map(RentalEquipmentJpaEntity::getEstimatedCost)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getFinalCost() {
        return this.rentalEquipments.stream()
                .map(RentalEquipmentJpaEntity::getFinalCost)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void addRentalEquipment(RentalEquipmentJpaEntity equipment) {
        equipment.setRental(this);
        this.rentalEquipments.add(equipment);
    }

    public void removeRentalEquipment(RentalEquipmentJpaEntity equipment) {
        equipment.setRental(null);
        this.rentalEquipments.remove(equipment);
    }

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
