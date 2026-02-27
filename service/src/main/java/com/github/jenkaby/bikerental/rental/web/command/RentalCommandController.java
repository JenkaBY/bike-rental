package com.github.jenkaby.bikerental.rental.web.command;

import com.github.jenkaby.bikerental.rental.application.usecase.CreateRentalUseCase;
import com.github.jenkaby.bikerental.rental.application.usecase.RecordPrepaymentUseCase;
import com.github.jenkaby.bikerental.rental.application.usecase.ReturnEquipmentUseCase;
import com.github.jenkaby.bikerental.rental.application.usecase.UpdateRentalUseCase;
import com.github.jenkaby.bikerental.rental.domain.model.Rental;
import com.github.jenkaby.bikerental.rental.domain.model.RentalStatus;
import com.github.jenkaby.bikerental.rental.web.command.dto.*;
import com.github.jenkaby.bikerental.rental.web.command.mapper.RentalCommandMapper;
import com.github.jenkaby.bikerental.rental.web.query.dto.RentalResponse;
import com.github.jenkaby.bikerental.rental.web.query.mapper.RentalQueryMapper;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Validated
@RestController
@RequestMapping("/api/rentals")
@Slf4j
class RentalCommandController {

    private final CreateRentalUseCase createRentalUseCase;
    private final UpdateRentalUseCase updateRentalUseCase;
    private final RecordPrepaymentUseCase recordPrepaymentUseCase;
    private final ReturnEquipmentUseCase returnEquipmentUseCase;
    private final RentalCommandMapper commandMapper;
    private final RentalQueryMapper queryMapper;

    RentalCommandController(
            CreateRentalUseCase createRentalUseCase,
            UpdateRentalUseCase updateRentalUseCase,
            RecordPrepaymentUseCase recordPrepaymentUseCase,
            ReturnEquipmentUseCase returnEquipmentUseCase,
            RentalCommandMapper commandMapper,
            RentalQueryMapper queryMapper) {
        this.createRentalUseCase = createRentalUseCase;
        this.updateRentalUseCase = updateRentalUseCase;
        this.recordPrepaymentUseCase = recordPrepaymentUseCase;
        this.returnEquipmentUseCase = returnEquipmentUseCase;
        this.commandMapper = commandMapper;
        this.queryMapper = queryMapper;
    }


    @PostMapping
    public ResponseEntity<RentalResponse> createRental(@Valid @RequestBody CreateRentalRequest request) {
        log.info("[POST] Creating rental with customerId: {}, equipmentId: {}",
                request.customerId(), request.equipmentId());
        var command = commandMapper.toCreateCommand(request);
        Rental rental = createRentalUseCase.execute(command);
        var response = queryMapper.toResponse(rental);
        log.info("[POST] Rental created successfully with id: {}", rental.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    @PostMapping("/draft")
    public ResponseEntity<RentalResponse> createDraft() {
        log.info("[POST] Creating new rental draft");
        var command = new CreateRentalUseCase.CreateDraftCommand();
        Rental rental = createRentalUseCase.execute(command);
        var response = queryMapper.toResponse(rental);
        log.info("[POST] Rental draft created successfully with id: {}", rental.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Draft Path: Updates rental using JSON Patch (RFC 6902).
     * Supports partial updates and rental activation.
     * <p>
     * Examples:
     * - Select customer: [{"op": "replace", "path": "/customerId", "value": "uuid"}]
     * - Select equipment: [{"op": "replace", "path": "/equipmentId", "value": 123}]
     * - Set duration: [{"op": "replace", "path": "/duration", "value": "PT2H"}]
     * - Start rental: [{"op": "replace", "path": "/status", "value": "ACTIVE"}] (see {@link RentalStatus})
     *   Note: startedAt is set automatically when rental is activated
     * - Combined update: [
     * {"op": "replace", "path": "/customerId", "value": "uuid"},
     * {"op": "replace", "path": "/equipmentId", "value": 123}
     * ]
     *
     * @param id      rental ID
     * @param request validated JSON Patch request
     * @return updated rental
     */
    @PatchMapping(value = "/{id}")
    public ResponseEntity<RentalResponse> updateRental(
            @PathVariable(name = "id") Long id,
            @Valid @RequestBody RentalUpdateJsonPatchRequest request) {
        log.info("[PATCH] Updating rental {} with {} patch operations", id, request.getOperations().size());

        // Convert validated RentalUpdateJsonPatchRequest to Map for use case layer
        Map<String, Object> patch = commandMapper.toPatchMap(request);

        Rental rental = updateRentalUseCase.execute(id, patch);
        var response = queryMapper.toResponse(rental);
        log.info("[PATCH] Rental {} updated successfully", id);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/{id}/prepayments")
    public ResponseEntity<PrepaymentResponse> recordPrepayment(
            @PathVariable(name = "id") Long id,
            @Valid @RequestBody RecordPrepaymentRequest request) {
        log.info("[POST] Recording prepayment for rental {}", id);
        var command = commandMapper.toRecordPrepaymentCommand(id, request);
        var paymentInfo = recordPrepaymentUseCase.execute(command);
        var response = commandMapper.toPrepaymentResponse(paymentInfo);
        log.info("[POST] Prepayment recorded for rental {} with receipt {}", id, paymentInfo.receiptNumber());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/return")
    public ResponseEntity<RentalReturnResponse> returnEquipment(
            @Valid @RequestBody ReturnEquipmentRequest request) {
        log.info("[POST] Processing equipment return for rentalId={}, equipmentId={}, equipmentUid={}",
                request.rentalId(), request.equipmentId(), request.equipmentUid());
        var command = commandMapper.toReturnCommand(request);
        var result = returnEquipmentUseCase.execute(command);
        var response = commandMapper.toReturnResponse(result, queryMapper);
        log.info("[POST] Equipment return processed successfully for rental {}", result.rental().getId());
        return ResponseEntity.ok(response);
    }
}
