package com.github.jenkaby.bikerental.shared.mapper;

import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;
import org.mapstruct.Mapper;

import java.math.BigDecimal;

@Mapper
public interface MoneyMapper {

    default Money toMoney(BigDecimal amount) {
        return amount != null ? Money.of(amount) : null;
    }

    default BigDecimal toBigDecimal(Money money) {
        return money != null ? money.amount() : null;
    }
}
