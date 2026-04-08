package com.github.jenkaby.bikerental.finance.web.query.mapper;

import com.github.jenkaby.bikerental.finance.application.usecase.GetCustomerAccountBalancesUseCase.CustomerAccountBalances;
import com.github.jenkaby.bikerental.finance.web.query.dto.CustomerAccountBalancesResponse;
import org.mapstruct.Mapper;

@Mapper
public interface AccountQueryMapper {

    CustomerAccountBalancesResponse toResponse(CustomerAccountBalances domain);
}
