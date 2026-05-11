package com.github.jenkaby.bikerental.equipment.infrastructure.persistence.entity;

import com.github.jenkaby.bikerental.shared.domain.model.Condition;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;

@ToString
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "equipments")
public class EquipmentJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "serial_number", nullable = false, unique = true, length = 50)
    private String serialNumber;

    @Column(unique = true, length = 100)
    private String uid;

    @Column(name = "type_slug", nullable = false, length = 50)
    private String typeSlug;

    @Column(name = "status_slug", nullable = false, length = 50)
    private String statusSlug;

    @Enumerated(EnumType.STRING)
    @Column(name = "condition_slug", nullable = false, length = 50)
    private Condition conditionSlug;

    @Column(length = 200)
    private String model;

    @Column(name = "commissioned_at")
    private LocalDate commissionedAt;

    @Column(columnDefinition = "TEXT")
    private String condition;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }
}
