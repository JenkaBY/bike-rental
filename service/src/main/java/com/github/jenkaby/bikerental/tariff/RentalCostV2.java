package com.github.jenkaby.bikerental.tariff;

import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;

public interface RentalCostV2 {

    Money totalCost();

    BreakdownCostDetails calculationBreakdown();
}
