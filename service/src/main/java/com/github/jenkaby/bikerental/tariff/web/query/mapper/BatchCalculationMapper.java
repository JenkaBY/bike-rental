package com.github.jenkaby.bikerental.tariff.web.query.mapper;

import com.github.jenkaby.bikerental.shared.mapper.DiscountMapper;
import com.github.jenkaby.bikerental.shared.mapper.DurationMapper;
import com.github.jenkaby.bikerental.shared.mapper.InstantMapper;
import com.github.jenkaby.bikerental.shared.mapper.MoneyMapper;
import com.github.jenkaby.bikerental.tariff.*;
import com.github.jenkaby.bikerental.tariff.web.query.dto.CostCalculationRequest;
import com.github.jenkaby.bikerental.tariff.web.query.dto.CostCalculationResponse;
import com.github.jenkaby.bikerental.tariff.web.query.dto.CostCalculationV2Request;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(uses = {MoneyMapper.class, DurationMapper.class, DiscountMapper.class, InstantMapper.class,
        CalculationBreakdownMapper.class})
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
    @Mapping(target = "discount", source = "discountPercent")
    @Mapping(target = "actualDuration", source = "actualDurationMinutes")
    public abstract RentalCostCalculationCommand toCommand(CostCalculationRequest request);


    public abstract EquipmentCostItem toItem(CostCalculationRequest.EquipmentItemRequest item);

    @Mapping(target = "plannedDuration", source = "plannedDurationMinutes")
    @Mapping(target = "discount", source = "discountPercent")
    public abstract RentalCostCalculationV2Command toV2Command(CostCalculationV2Request request);

    public abstract EquipmentCostItemV2 toV2Item(CostCalculationV2Request.EquipmentItemRequest item);

    //    TODO use Mapstruct features
    public CostCalculationResponse toResponse(RentalCostCalculationResult result) {
        return new CostCalculationResponse(
                breakdownMapper.toBreakdownResponses(result.equipmentBreakdowns()),
                moneyMapper.toBigDecimal(result.subtotal()),
                discountMapper.toResponse(result.discount()),
                moneyMapper.toBigDecimal(result.totalCost()),
                durationMapper.toMinutes(result.effectiveDuration()),
                result.estimate(),
                result.specialPricingApplied()
        );
    }
}
