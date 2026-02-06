package com.github.jenkaby.bikerental.rental.web.command.mapper;

import com.github.jenkaby.bikerental.rental.application.usecase.CreateRentalUseCase;
import com.github.jenkaby.bikerental.rental.web.command.dto.CreateRentalRequest;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface RentalCommandMapper {

    CreateRentalUseCase.CreateRentalCommand toCreateCommand(CreateRentalRequest request);
}
