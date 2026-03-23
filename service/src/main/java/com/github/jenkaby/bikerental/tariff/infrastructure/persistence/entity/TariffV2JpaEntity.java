package com.github.jenkaby.bikerental.tariff.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;

@Entity
@Table(name = "tariffs_v2")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class TariffV2JpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(name = "equipment_type_slug", nullable = false, length = 50)
    private String equipmentType;

    @Column(name = "pricing_type", nullable = false, length = 50)
    private String pricingType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "params", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> params;

    @Column(name = "valid_from", nullable = false)
    private LocalDate validFrom;

    @Column(name = "valid_to")
    private LocalDate validTo;

    @Column(name = "status", nullable = false, length = 50)
    private String status;

    @Column(name = "version", nullable = false, length = 10)
    private String version;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
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
