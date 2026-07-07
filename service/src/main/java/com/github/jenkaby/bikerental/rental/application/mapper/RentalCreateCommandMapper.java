package com.github.jenkaby.bikerental.rental.application.mapper;

import com.github.jenkaby.bikerental.rental.application.usecase.CreateOrUpdateDraftRentalUseCase;
import com.github.jenkaby.bikerental.rental.application.usecase.InitRentalForSigningUseCase;
import org.mapstruct.Mapper;

@Mapper
public interface RentalCreateCommandMapper {

    CreateOrUpdateDraftRentalUseCase.InitDraftCommand toInitDraftCommand(InitRentalForSigningUseCase.InitRentalForSigningCommand command);
}
