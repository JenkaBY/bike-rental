package com.github.jenkaby.bikerental.equipment.domain.model;

import com.github.jenkaby.bikerental.equipment.shared.domain.model.vo.SerialNumber;
import com.github.jenkaby.bikerental.equipment.shared.domain.model.vo.Uid;
import com.github.jenkaby.bikerental.shared.domain.model.Condition;
import lombok.*;

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
    private Condition conditionSlug;
    private String model;
    private LocalDate commissionedAt;
    private String condition;
}
