package com.github.jenkaby.bikerental.rental.web.query.mapper;

import com.github.jenkaby.bikerental.rental.domain.model.RentalEquipment;
import com.github.jenkaby.bikerental.rental.domain.model.vo.RentalEquipmentCostBreakdown;
import com.github.jenkaby.bikerental.rental.shared.mapper.RentalEquipmentStatusMapper;
import com.github.jenkaby.bikerental.rental.web.query.dto.EquipmentItemResponse;
import com.github.jenkaby.bikerental.shared.mapper.InstantMapper;
import com.github.jenkaby.bikerental.shared.mapper.MoneyMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(uses = {MoneyMapper.class, InstantMapper.class, RentalEquipmentStatusMapper.class})
public interface RentalEquipmentWebMapper {

    @Mapping(target = "breakdown", source = "finalCostBreakdown")
    EquipmentItemResponse toEquipmentItemResponse(RentalEquipment equipment);

    default EquipmentItemResponse.CostBreakdown toCostBreakdown(RentalEquipmentCostBreakdown source) {
        if (source == null) {
            return null;
        }
        var detail = source.calculationBreakdown();
        var calculationDetail = new EquipmentItemResponse.CostBreakdown.CalculationDetail(
                detail.breakdownPatternCode(),
                detail.message(),
                detail.params()
        );
        return new EquipmentItemResponse.CostBreakdown(
                source.pricingType(),
                source.tariffName(),
                source.billedDurationMinutes(),
                source.overtimeMinutes(),
                source.forgivenMinutes(),
                source.itemCost(),
                calculationDetail
        );
    }
}
