package com.github.jenkaby.bikerental.customer.web.query.mapper;

import com.github.jenkaby.bikerental.customer.CustomerInfo;
import com.github.jenkaby.bikerental.customer.web.query.dto.CustomerSearchResponse;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CustomerQueryMapper {

    CustomerSearchResponse toSearchResponse(CustomerInfo customerInfo);

    List<CustomerSearchResponse> toSearchResponses(List<CustomerInfo> customers);
}
