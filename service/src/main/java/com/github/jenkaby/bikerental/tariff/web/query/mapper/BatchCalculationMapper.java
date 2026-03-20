package com.github.jenkaby.bikerental.tariff.web.query.mapper;

import com.github.jenkaby.bikerental.shared.mapper.DiscountMapper;
import com.github.jenkaby.bikerental.shared.mapper.DurationMapper;
import com.github.jenkaby.bikerental.shared.mapper.MoneyMapper;
import com.github.jenkaby.bikerental.tariff.EquipmentCostItem;
import com.github.jenkaby.bikerental.tariff.RentalCostCalculationCommand;
import com.github.jenkaby.bikerental.tariff.RentalCostCalculationResult;
import com.github.jenkaby.bikerental.tariff.web.query.dto.CostCalculationRequest;
import com.github.jenkaby.bikerental.tariff.web.query.dto.CostCalculationResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(uses = {MoneyMapper.class, DurationMapper.class, DiscountMapper.class, CalculationBreakdownMapper.class})
public abstract class BatchCalculationMapper {

    private MoneyMapper moneyMapper;
    private DurationMapper durationMapper;
    private DiscountDetailMapper discountMapper;
    private CalculationBreakdownMapper breakdownMapper;

    @Autowired
    public void setDiscountMapper(DiscountDetailMapper discountMapper) {
        this.discountMapper = discountMapper;
    }

    @Autowired
    public void setBreakdownMapper(CalculationBreakdownMapper breakdownMapper) {
        this.breakdownMapper = breakdownMapper;
    }

    @Autowired
    public void setMoneyMapper(MoneyMapper moneyMapper) {
        this.moneyMapper = moneyMapper;
    }

    @Autowired
    public void setDurationMapper(DurationMapper durationMapper) {
        this.durationMapper = durationMapper;
    }

    @Mapping(target = "equipments", source = "equipments")
    @Mapping(target = "plannedDuration", source = "plannedDurationMinutes")
    @Mapping(target = "actualDuration", source = "actualDurationMinutes")
    @Mapping(target = "discount", source = "discountPercent")
    public abstract RentalCostCalculationCommand toCommand(CostCalculationRequest request);

    public abstract EquipmentCostItem toItem(CostCalculationRequest.EquipmentItemRequest item);

    public CostCalculationResponse toResponse(RentalCostCalculationResult result) {
        return new CostCalculationResponse(
                breakdownMapper.toBreakdownResponses(result.equipmentBreakdowns()),
                result.subtotal().amount(),
                discountMapper.toResponse(result.discount()),
                moneyMapper.toBigDecimal(result.totalCost()),
                durationMapper.toMinutes(result.effectiveDuration()),
                result.estimate(),
                result.specialPricingApplied()
        );
    }
}
