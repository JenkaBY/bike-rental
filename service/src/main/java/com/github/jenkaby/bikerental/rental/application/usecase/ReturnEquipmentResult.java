package com.github.jenkaby.bikerental.rental.application.usecase;

import com.github.jenkaby.bikerental.finance.PaymentInfo;
import com.github.jenkaby.bikerental.rental.domain.model.Rental;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;
import com.github.jenkaby.bikerental.tariff.RentalCost;

public record ReturnEquipmentResult(
        Rental rental,
        RentalCost cost,
        Money additionalPayment,
        PaymentInfo paymentInfo
) {
}
