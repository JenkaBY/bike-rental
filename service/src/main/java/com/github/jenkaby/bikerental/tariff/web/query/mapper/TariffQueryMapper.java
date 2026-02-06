package com.github.jenkaby.bikerental.tariff.web.query.mapper;

import com.github.jenkaby.bikerental.shared.mapper.MoneyMapper;
import com.github.jenkaby.bikerental.tariff.domain.model.Tariff;
import com.github.jenkaby.bikerental.tariff.web.query.dto.TariffResponse;
import org.mapstruct.Mapper;

@Mapper(uses = {MoneyMapper.class})
public interface TariffQueryMapper {

    TariffResponse toResponse(Tariff tariff);
}
