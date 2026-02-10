package com.github.jenkaby.bikerental.rental.web.command.mapper;

import com.github.jenkaby.bikerental.finance.PaymentInfo;
import com.github.jenkaby.bikerental.rental.application.usecase.CreateRentalUseCase;
import com.github.jenkaby.bikerental.rental.application.usecase.RecordPrepaymentUseCase;
import com.github.jenkaby.bikerental.rental.web.command.dto.*;
import com.github.jenkaby.bikerental.shared.mapper.MoneyMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.HashMap;
import java.util.Map;

@Mapper(uses = {MoneyMapper.class})
public interface RentalCommandMapper {

    CreateRentalUseCase.CreateRentalCommand toCreateCommand(CreateRentalRequest request);

    @Mapping(target = "rentalId", expression = "java(rentalId)")
    @Mapping(target = "amount", source = "request.amount")
    @Mapping(target = "paymentMethod", source = "request.paymentMethod")
    @Mapping(target = "operatorId", source = "request.operatorId")
    RecordPrepaymentUseCase.RecordPrepaymentCommand toRecordPrepaymentCommand(Long rentalId, RecordPrepaymentRequest request);

    @Mapping(target = "paymentId", source = "id")
    @Mapping(target = "amount", source = "amount")
    PrepaymentResponse toPrepaymentResponse(PaymentInfo paymentInfo);

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
