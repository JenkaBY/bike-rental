package com.github.jenkaby.bikerental.equipment.domain.model;

import lombok.*;
import org.jspecify.annotations.NonNull;

import java.util.Set;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
@EqualsAndHashCode
public class EquipmentStatus {

    @Setter
    private Long id;
    private String slug;
    private String name;
    private String description;
    private Set<String> allowedTransitions;


    public boolean canTransitionTo(@NonNull EquipmentStatus another) {
        return canTransitionTo(another.getSlug());
    }

    public boolean canTransitionTo(@NonNull String toStatusSlug) {
        return allowedTransitions != null && allowedTransitions.contains(toStatusSlug);
    }
}
