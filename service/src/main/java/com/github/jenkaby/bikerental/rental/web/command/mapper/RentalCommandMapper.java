package com.github.jenkaby.bikerental.rental.web.command.mapper;

import com.github.jenkaby.bikerental.rental.application.usecase.CreateRentalUseCase;
import com.github.jenkaby.bikerental.rental.application.usecase.ReturnEquipmentResult;
import com.github.jenkaby.bikerental.rental.application.usecase.ReturnEquipmentUseCase;
import com.github.jenkaby.bikerental.rental.web.command.dto.*;
import com.github.jenkaby.bikerental.rental.web.query.dto.RentalResponse;
import com.github.jenkaby.bikerental.rental.web.query.mapper.RentalQueryMapper;
import com.github.jenkaby.bikerental.shared.mapper.DiscountMapper;
import com.github.jenkaby.bikerental.shared.mapper.MoneyMapper;
import com.github.jenkaby.bikerental.tariff.RentalCost;
import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

@Mapper(uses = {MoneyMapper.class, RentalQueryMapper.class, DiscountMapper.class})
public abstract class RentalCommandMapper {

    protected RentalQueryMapper rentalQueryMapper;
    protected SettlementMapper settlementMapper;

    @Autowired
    public void setSettlementMapper(SettlementMapper settlementMapper) {
        this.settlementMapper = settlementMapper;
    }

    @Autowired
    public void setQueryMapper(RentalQueryMapper queryMapper) {
        this.rentalQueryMapper = queryMapper;
    }

    public abstract CreateRentalUseCase.CreateRentalCommand toCreateCommand(CreateRentalRequest request);

    public abstract ReturnEquipmentUseCase.ReturnEquipmentCommand toReturnCommand(ReturnEquipmentRequest request);

    public RentalReturnResponse toReturnResponse(ReturnEquipmentResult result) {
        RentalResponse rentalResponse = rentalQueryMapper.toResponse(result.rental());
        var costsBreakdown = result.breakDownCosts().entrySet().stream()
                .map(entry -> toCostBreakdown(entry.getKey(), entry.getValue()))
                .toList();
        var settlementResponse = settlementMapper.toResponse(result.settlementInfo());
        return new RentalReturnResponse(rentalResponse, costsBreakdown, settlementResponse);
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
