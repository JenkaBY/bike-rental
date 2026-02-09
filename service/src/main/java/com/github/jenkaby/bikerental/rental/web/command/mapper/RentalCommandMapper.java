package com.github.jenkaby.bikerental.rental.web.command.mapper;

import com.github.jenkaby.bikerental.rental.application.usecase.CreateRentalUseCase;
import com.github.jenkaby.bikerental.rental.web.command.dto.CreateRentalRequest;
import com.github.jenkaby.bikerental.rental.web.command.dto.RentalPatchOperation;
import com.github.jenkaby.bikerental.rental.web.command.dto.RentalUpdateJsonPatchRequest;
import org.mapstruct.Mapper;

import java.util.HashMap;
import java.util.Map;

@Mapper
public interface RentalCommandMapper {

    CreateRentalUseCase.CreateRentalCommand toCreateCommand(CreateRentalRequest request);

    default Map<String, Object> toPatchMap(RentalUpdateJsonPatchRequest request) {
        Map<String, Object> patch = new HashMap<>();

        for (RentalPatchOperation operation : request.getOperations()) {
            String path = operation.getPath();
            // Remove leading slash from path
            String fieldName = path.startsWith("/") ? path.substring(1) : path;

            if (operation.getOp() != null) {
                patch.put(fieldName, operation.getValue());
            }
        }

        return patch;
    }
}
