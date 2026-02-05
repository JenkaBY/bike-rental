package com.github.jenkaby.bikerental.equipment.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import java.util.Set;

@ToString
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "equipment_statuses")
public class EquipmentStatusJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String slug;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ElementCollection
    @CollectionTable(
            name = "equipment_status_transition_rules",
            joinColumns = @JoinColumn(name = "from_status_slug", referencedColumnName = "slug")
    )
    @Column(name = "to_status_slug")
    @BatchSize(size = 10)
    private Set<String> allowedTransitionSlugs;
}
