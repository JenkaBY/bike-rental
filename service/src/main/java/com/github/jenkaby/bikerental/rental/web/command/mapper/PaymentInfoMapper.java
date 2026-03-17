package com.github.jenkaby.bikerental.rental.web.command.mapper;

import com.github.jenkaby.bikerental.finance.PaymentInfo;
import com.github.jenkaby.bikerental.rental.web.command.dto.PaymentInfoResponse;
import com.github.jenkaby.bikerental.shared.mapper.MoneyMapper;
import org.mapstruct.Mapper;

@Mapper(uses = {MoneyMapper.class})
public interface PaymentInfoMapper {

    PaymentInfoResponse toResponse(PaymentInfo source);
}
