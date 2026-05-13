package com.github.jenkaby.bikerental.rental.domain.model.vo;

import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;

public record EquipmentCostResult(Long equipmentId,
                                  Long tariffId,
                                  Money estimatedCost) {
}