package com.github.jenkaby.bikerental.equipment.domain.model;

import com.github.jenkaby.bikerental.equipment.shared.domain.model.vo.SerialNumber;
import com.github.jenkaby.bikerental.equipment.shared.domain.model.vo.Uid;
import lombok.*;

import java.time.LocalDate;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Equipment {
    @Setter
    private Long id;
    private final SerialNumber serialNumber;
    private final Uid uid;
    private final String typeSlug;
    private final String statusSlug;
    private final String model;
    private final LocalDate commissionedAt;
    private final String condition;
}
