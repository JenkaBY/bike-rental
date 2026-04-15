package com.github.jenkaby.bikerental.rental.application.usecase;

import com.github.jenkaby.bikerental.finance.PaymentMethod;
import com.github.jenkaby.bikerental.finance.SettlementInfo;
import com.github.jenkaby.bikerental.rental.domain.model.Rental;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;

public interface ReturnEquipmentUseCase {

    @NonNull
    ReturnEquipmentResult execute(@NonNull ReturnEquipmentCommand command);

    record ReturnEquipmentCommand(
            Long rentalId,
            List<Long> equipmentIds,
            List<String> equipmentUids,
            PaymentMethod paymentMethod,
            String operatorId
    ) {
        public List<Long> getEquipmentIds() {
            return equipmentIds != null ? equipmentIds : List.of();
        }

        public List<String> getEquipmentUids() {
            return equipmentUids != null ? equipmentUids : List.of();
        }
    }

    record ReturnEquipmentResult(
            Rental rental,
            @Nullable SettlementInfo settlementInfo
    ) {
    }
}
