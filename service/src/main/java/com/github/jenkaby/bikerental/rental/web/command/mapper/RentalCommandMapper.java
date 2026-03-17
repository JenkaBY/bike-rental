package com.github.jenkaby.bikerental.rental.web.command.mapper;

import com.github.jenkaby.bikerental.finance.PaymentInfo;
import com.github.jenkaby.bikerental.rental.application.usecase.CreateRentalUseCase;
import com.github.jenkaby.bikerental.rental.application.usecase.RecordPrepaymentUseCase;
import com.github.jenkaby.bikerental.rental.application.usecase.ReturnEquipmentResult;
import com.github.jenkaby.bikerental.rental.application.usecase.ReturnEquipmentUseCase;
import com.github.jenkaby.bikerental.rental.web.command.dto.*;
import com.github.jenkaby.bikerental.rental.web.query.dto.RentalResponse;
import com.github.jenkaby.bikerental.rental.web.query.mapper.RentalQueryMapper;
import com.github.jenkaby.bikerental.shared.mapper.MoneyMapper;
import com.github.jenkaby.bikerental.tariff.RentalCost;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Mapper(uses = {MoneyMapper.class, RentalQueryMapper.class})
public abstract class RentalCommandMapper {

    protected RentalQueryMapper rentalQueryMapper;
    protected PaymentInfoMapper paymentInfoMapper;

    @Autowired
    public void setPaymentInfoMapper(PaymentInfoMapper paymentInfoMapper) {
        this.paymentInfoMapper = paymentInfoMapper;
    }

    @Autowired
    public void setQueryMapper(RentalQueryMapper queryMapper) {
        this.rentalQueryMapper = queryMapper;
    }

    public abstract CreateRentalUseCase.CreateRentalCommand toCreateCommand(CreateRentalRequest request);

    @Mapping(target = "rentalId", expression = "java(rentalId)")
    @Mapping(target = "amount", source = "request.amount")
    @Mapping(target = "paymentMethod", source = "request.paymentMethod")
    @Mapping(target = "operatorId", source = "request.operatorId")
    public abstract RecordPrepaymentUseCase.RecordPrepaymentCommand toRecordPrepaymentCommand(Long rentalId, RecordPrepaymentRequest request);

    @Mapping(target = "paymentId", source = "id")
    @Mapping(target = "amount", source = "amount")
    public abstract PrepaymentResponse toPrepaymentResponse(PaymentInfo paymentInfo);

    public abstract ReturnEquipmentUseCase.ReturnEquipmentCommand toReturnCommand(ReturnEquipmentRequest request);

    public RentalReturnResponse toReturnResponse(ReturnEquipmentResult result) {
        RentalResponse rentalResponse = rentalQueryMapper.toResponse(result.rental());
        var costsBreakdown = result.breakDownCosts().entrySet().stream()
                .map(entry -> toCostBreakdown(entry.getKey(), entry.getValue()))
                .toList();
        BigDecimal additionalPayment = result.additionalPayment() != null ? result.additionalPayment().amount() : null;
        return new RentalReturnResponse(rentalResponse, costsBreakdown, additionalPayment, paymentInfoMapper.toResponse(result.paymentInfo()));
    }

    RentalReturnResponse.CostBreakdown toCostBreakdown(Long equipmentId, RentalCost cost) {
        return new RentalReturnResponse.CostBreakdown(
                equipmentId,
                cost.baseCost().amount(),
                cost.overtimeCost().amount(),
                cost.totalCost().amount(),
                cost.actualMinutes(),
                cost.billableMinutes(),
                cost.plannedMinutes(),
                cost.overtimeMinutes(),
                cost.forgivenessApplied(),
                cost.calculationMessage()
        );
    }

    public Map<String, Object> toPatchMap(RentalUpdateJsonPatchRequest request) {
        Map<String, Object> patch = new HashMap<>();
        for (RentalPatchOperation operation : request.getOperations()) {
            String path = operation.getPath();
            String fieldName = path.startsWith("/") ? path.substring(1) : path;
            if (operation.getOp() != null) {
                patch.put(fieldName, operation.getValue());
            }
        }
        return patch;
    }
}
