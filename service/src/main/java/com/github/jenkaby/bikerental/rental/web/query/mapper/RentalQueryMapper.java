package com.github.jenkaby.bikerental.rental.web.query.mapper;

import com.github.jenkaby.bikerental.rental.domain.model.Rental;
import com.github.jenkaby.bikerental.rental.shared.mapper.RentalStatusMapper;
import com.github.jenkaby.bikerental.rental.web.query.dto.RentalResponse;
import com.github.jenkaby.bikerental.rental.web.query.dto.RentalSummaryResponse;
import com.github.jenkaby.bikerental.shared.mapper.DurationMapper;
import com.github.jenkaby.bikerental.shared.mapper.InstantMapper;
import com.github.jenkaby.bikerental.shared.mapper.MoneyMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(uses = {MoneyMapper.class, InstantMapper.class, RentalStatusMapper.class, DurationMapper.class, RentalOverdueMapper.class})
public interface RentalQueryMapper {

    @Mapping(target = "actualDurationMinutes", source = "rental.actualDuration")
    @Mapping(target = "plannedDurationMinutes", source = "rental.plannedDuration")
    RentalResponse toResponse(Rental rental);

    @Mapping(target = "overdueMinutes", source = "rental")
    RentalSummaryResponse toRentalSummaryResponse(Rental rental);
}
