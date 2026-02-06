package com.github.jenkaby.bikerental.rental.web.command.mapper;

import com.github.jenkaby.bikerental.rental.application.usecase.CreateRentalUseCase;
import com.github.jenkaby.bikerental.rental.web.command.dto.CreateRentalRequest;
import org.mapstruct.Mapper;

@Mapper
public interface RentalCommandMapper {

    CreateRentalUseCase.CreateRentalCommand toCreateCommand(CreateRentalRequest request);
}
