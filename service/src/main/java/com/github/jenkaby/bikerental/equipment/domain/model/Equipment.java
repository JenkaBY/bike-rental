package com.github.jenkaby.bikerental.equipment.domain.model;

import com.github.jenkaby.bikerental.equipment.domain.service.StatusTransitionPolicy;
import com.github.jenkaby.bikerental.equipment.shared.domain.model.vo.SerialNumber;
import com.github.jenkaby.bikerental.equipment.shared.domain.model.vo.Uid;
import lombok.*;
import org.jspecify.annotations.NonNull;

import java.time.LocalDate;

@Setter
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Equipment {

    private Long id;
    private SerialNumber serialNumber;
    private Uid uid;
    private String typeSlug;
    private String statusSlug;
    private String model;
    private LocalDate commissionedAt;
    private String condition;


    public void changeStatusTo(@NonNull String newStatusSlug, @NonNull StatusTransitionPolicy policy) {
        policy.validateTransition(this.statusSlug, newStatusSlug);
        this.statusSlug = newStatusSlug;
    }

    public void setInitialStatus(@NonNull String initialStatusSlug) {
        this.statusSlug = initialStatusSlug;
    }
}
