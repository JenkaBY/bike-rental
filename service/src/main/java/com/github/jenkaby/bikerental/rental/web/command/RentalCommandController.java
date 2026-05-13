package com.github.jenkaby.bikerental.rental.web.command;

import com.github.jenkaby.bikerental.rental.application.usecase.CreateOrUpdateDraftRentalUseCase;
import com.github.jenkaby.bikerental.rental.application.usecase.RentalLifecycleUseCase;
import com.github.jenkaby.bikerental.rental.application.usecase.ReturnEquipmentUseCase;
import com.github.jenkaby.bikerental.rental.application.usecase.UpdateRentalUseCase;
import com.github.jenkaby.bikerental.rental.domain.model.Rental;
import com.github.jenkaby.bikerental.rental.domain.model.RentalStatus;
import com.github.jenkaby.bikerental.rental.web.command.dto.*;
import com.github.jenkaby.bikerental.rental.web.command.mapper.RentalCommandMapper;
import com.github.jenkaby.bikerental.rental.web.query.dto.RentalResponse;
import com.github.jenkaby.bikerental.rental.web.query.mapper.RentalQueryMapper;
import com.github.jenkaby.bikerental.shared.config.OpenApiConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Validated
@RestController
@RequestMapping(path = "/api/rentals", produces = {MediaType.APPLICATION_JSON_VALUE})
@Slf4j
@Tag(name = OpenApiConfig.Tags.RENTALS)
class RentalCommandController {

    private final CreateOrUpdateDraftRentalUseCase updateDraftRentalUseCase;
    private final UpdateRentalUseCase updateRentalUseCase;
    private final ReturnEquipmentUseCase returnEquipmentUseCase;
    private final RentalCommandMapper commandMapper;
    private final RentalQueryMapper queryMapper;
    private final RentalLifecycleUseCase rentalLifecycleUseCase;

    RentalCommandController(
            CreateOrUpdateDraftRentalUseCase updateDraftRentalUseCase,
            UpdateRentalUseCase updateRentalUseCase,
            ReturnEquipmentUseCase returnEquipmentUseCase,
            RentalCommandMapper commandMapper,
            RentalQueryMapper queryMapper,
            RentalLifecycleUseCase rentalLifecycleUseCase) {
        this.updateDraftRentalUseCase = updateDraftRentalUseCase;
        this.updateRentalUseCase = updateRentalUseCase;
        this.returnEquipmentUseCase = returnEquipmentUseCase;
        this.commandMapper = commandMapper;
        this.queryMapper = queryMapper;
        this.rentalLifecycleUseCase = rentalLifecycleUseCase;
    }


    @PutMapping("/{rentalId}")
    @Operation(summary = "Create rental (Fast Path)", description = "Creates an active rental in one step with all required data")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Rental created",
                    content = @Content(schema = @Schema(implementation = RentalResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Customer or equipment not found",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "Equipment not available",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<RentalResponse> updateRental(
            @Positive @PathVariable("rentalId") Long rentalId,
            @Valid @RequestBody RentalRequest request) {
        log.info("[PUT] Updating rental for customerId: {}, equipmentIds: {}",
                request.customerId(), request.equipmentIds());
        var command = commandMapper.toUpdateDraftRentalCommand(rentalId, request);
        Rental rental = updateDraftRentalUseCase.execute(command);
        var response = queryMapper.toResponse(rental);
        log.info("[PUT] Rental updated successfully with id: {}", rental.getId());
        return ResponseEntity.ok(response);
    }


    @PostMapping("/draft")
    @Operation(summary = "Create rental draft (Draft Path)", description = "Creates an empty rental draft to be filled step by step")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Draft created",
                    content = @Content(schema = @Schema(implementation = RentalResponse.class))),
            @ApiResponse(responseCode = "409", description = "Equipment not available",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<RentalResponse> createDraft() {
        log.info("[POST] Creating new rental draft");
        var command = new CreateOrUpdateDraftRentalUseCase.CreateDraftCommand();
        Rental rental = updateDraftRentalUseCase.execute(command);
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
     * - Select equipment: [{"op": "replace", "path": "/equipmentIds", "value": [123,125]}]
     * - Set duration: [{"op": "replace", "path": "/duration", "value": "120"}]
     * Note: startedAt is set automatically when rental is activated
     * - Combined update: [
     * {"op": "replace", "path": "/customerId", "value": "uuid"},
     * {"op": "replace", "path": "/equipmentIds", "value": [123,125]}
     * ]
     *
     * @param id      rental ID
     * @param request validated JSON Patch request
     * @return updated rental
     */
    @PatchMapping(value = "/{id}")
    @Operation(
            hidden = true,
            summary = "Update rental via JSON Patch (RFC 6902)",
            description = "Applies partial updates to a rental. Supported paths: /customerId, /equipmentIds, /duration, /status. Setting status=ACTIVE activates the rental.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Rental updated",
                    content = @Content(schema = @Schema(implementation = RentalResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid patch document",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Rental not found",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "Equipment not available",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "422", description = "Invalid rental status transition",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
//    TODO remove
    @Deprecated(forRemoval = true)
    public ResponseEntity<RentalResponse> updateRental(
            @Parameter(description = "Rental ID", example = "1") @PathVariable(name = "id") @Positive Long id,
            @Valid @RequestBody RentalUpdateJsonPatchRequest request) {
        log.info("[PATCH] Updating rental {} with {} patch operations", id, request.getOperations().size());

        // Convert validated RentalUpdateJsonPatchRequest to Map for use case layer
        Map<String, Object> patch = commandMapper.toPatchMap(request);

        Rental rental = updateRentalUseCase.execute(id, patch);
        var response = queryMapper.toResponse(rental);
        log.info("[PATCH] Rental {} updated successfully", id);
        return ResponseEntity.ok(response);
    }


    @PostMapping("/return")
    @Operation(summary = "Return equipment", description = "Completes a rental by returning the rented equipment, calculates final cost and records additional payment if needed")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Equipment returned, rental completed",
                    content = @Content(schema = @Schema(implementation = RentalReturnResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error or rental identifier missing",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Rental or equipment not found",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "422", description = "Rental not in active state",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<RentalReturnResponse> returnEquipment(@Valid @RequestBody ReturnEquipmentRequest request) {
        log.info("[POST] Processing equipment return for rentalId={}, equipmentIds={}, equipmentUids={}",
                request.rentalId(), request.equipmentIds(), request.equipmentUids());
        var command = commandMapper.toReturnCommand(request);
        var result = returnEquipmentUseCase.execute(command);
        var response = commandMapper.toReturnResponse(result);
        log.info("[POST] Equipment return processed successfully for rental {}", result.rental().getId());
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{rentalId}/lifecycles")
    @Operation(summary = "Transition rental lifecycle status",
            description = "Transitions a rental to ACTIVE or CANCELLED status")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Rental status updated",
                    content = @Content(schema = @Schema(implementation = RentalResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Rental not found",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "422", description = "Invalid status transition",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<RentalResponse> updateLifecycle(
            @Positive @PathVariable("rentalId") Long rentalId,
            @Valid @RequestBody RentalLifecycleRequest request) {
        log.info("[PATCH] Lifecycle transition for rentalId={}, targetStatus={}", rentalId, request.status());
        var command = new RentalLifecycleUseCase.RentalLifecycleCommand(
                rentalId,
                RentalStatus.valueOf(request.status().name()),
                request.operatorId());
        var rental = rentalLifecycleUseCase.execute(command);
        var response = queryMapper.toResponse(rental);
        log.info("[PATCH] Rental {} lifecycle updated to {}", rentalId, rental.getStatus());
        return ResponseEntity.ok(response);
    }
}
