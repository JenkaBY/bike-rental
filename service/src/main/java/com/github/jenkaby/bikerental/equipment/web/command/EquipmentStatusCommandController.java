package com.github.jenkaby.bikerental.equipment.web.command;

import com.github.jenkaby.bikerental.equipment.application.usecase.CreateEquipmentStatusUseCase;
import com.github.jenkaby.bikerental.equipment.application.usecase.UpdateEquipmentStatusUseCase;
import com.github.jenkaby.bikerental.equipment.web.command.dto.EquipmentStatusRequest;
import com.github.jenkaby.bikerental.equipment.web.command.dto.EquipmentStatusUpdateRequest;
import com.github.jenkaby.bikerental.equipment.web.command.mapper.EquipmentStatusCommandMapper;
import com.github.jenkaby.bikerental.equipment.web.query.dto.EquipmentStatusResponse;
import com.github.jenkaby.bikerental.equipment.web.query.mapper.EquipmentStatusMapper;
import com.github.jenkaby.bikerental.shared.config.OpenApiConfig;
import com.github.jenkaby.bikerental.shared.web.support.Slug;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping(path = "/api/equipment-statuses", produces = {MediaType.APPLICATION_JSON_VALUE})
@Tag(name = OpenApiConfig.Tags.EQUIPMENT_STATUSES)
public class EquipmentStatusCommandController {

    private final CreateEquipmentStatusUseCase createUseCase;
    private final UpdateEquipmentStatusUseCase updateUseCase;
    private final EquipmentStatusCommandMapper commandMapper;
    private final EquipmentStatusMapper queryMapper;

    EquipmentStatusCommandController(CreateEquipmentStatusUseCase createUseCase,
                                     UpdateEquipmentStatusUseCase updateUseCase,
                                     EquipmentStatusCommandMapper commandMapper,
                                     EquipmentStatusMapper queryMapper) {
        this.createUseCase = createUseCase;
        this.updateUseCase = updateUseCase;
        this.commandMapper = commandMapper;
        this.queryMapper = queryMapper;
    }

    @PostMapping
    @Operation(summary = "Create equipment status")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Equipment status created",
                    content = @Content(schema = @Schema(implementation = EquipmentStatusResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "Slug already exists",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<EquipmentStatusResponse> create(@RequestBody @Valid EquipmentStatusRequest request) {
        var command = commandMapper.toCreateCommand(request);
        var created = createUseCase.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(queryMapper.toResponse(created));
    }

    @PutMapping("/{slug}")
    @Operation(summary = "Update equipment status", description = "Updates name, description and allowed transitions for a status")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Equipment status updated",
                    content = @Content(schema = @Schema(implementation = EquipmentStatusResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Equipment status not found",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "422", description = "Invalid status transition",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<EquipmentStatusResponse> update(
            @Parameter(description = "Equipment status slug", example = "available") @PathVariable("slug") @Slug String slug,
            @RequestBody @Valid EquipmentStatusUpdateRequest request) {
        var command = commandMapper.toUpdateCommand(slug, request);
        var updated = updateUseCase.execute(command);
        return ResponseEntity.ok(queryMapper.toResponse(updated));
    }
}
