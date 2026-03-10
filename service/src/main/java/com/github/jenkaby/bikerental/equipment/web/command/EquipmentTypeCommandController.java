package com.github.jenkaby.bikerental.equipment.web.command;

import com.github.jenkaby.bikerental.equipment.application.usecase.CreateEquipmentTypeUseCase;
import com.github.jenkaby.bikerental.equipment.application.usecase.UpdateEquipmentTypeUseCase;
import com.github.jenkaby.bikerental.equipment.web.command.dto.EquipmentTypeRequest;
import com.github.jenkaby.bikerental.equipment.web.command.dto.EquipmentTypeUpdateRequest;
import com.github.jenkaby.bikerental.equipment.web.command.mapper.EquipmentTypeCommandMapper;
import com.github.jenkaby.bikerental.equipment.web.query.dto.EquipmentTypeResponse;
import com.github.jenkaby.bikerental.equipment.web.query.mapper.EquipmentTypeMapper;
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
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api/equipment-types")
@Tag(name = OpenApiConfig.Tags.EQUIPMENT_TYPES)
public class EquipmentTypeCommandController {

    private final CreateEquipmentTypeUseCase createUseCase;
    private final UpdateEquipmentTypeUseCase updateUseCase;
    private final EquipmentTypeCommandMapper commandMapper;
    private final EquipmentTypeMapper queryMapper;

    EquipmentTypeCommandController(CreateEquipmentTypeUseCase createUseCase,
                                   UpdateEquipmentTypeUseCase updateUseCase,
                                   EquipmentTypeCommandMapper commandMapper,
                                   EquipmentTypeMapper queryMapper) {
        this.createUseCase = createUseCase;
        this.updateUseCase = updateUseCase;
        this.commandMapper = commandMapper;
        this.queryMapper = queryMapper;
    }

    @PostMapping
    @Operation(summary = "Create equipment type")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Equipment type created",
                    content = @Content(schema = @Schema(implementation = EquipmentTypeResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "Slug already exists",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<EquipmentTypeResponse> create(@RequestBody @Valid EquipmentTypeRequest request) {
        var command = commandMapper.toCreateCommand(request);
        var created = createUseCase.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(queryMapper.toResponse(created));
    }

    @PutMapping("/{slug}")
    @Operation(summary = "Update equipment type")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Equipment type updated",
                    content = @Content(schema = @Schema(implementation = EquipmentTypeResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Equipment type not found",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<EquipmentTypeResponse> update(
            @Parameter(description = "Equipment type slug", example = "bike") @PathVariable("slug") @Slug String slug,
            @RequestBody @Valid EquipmentTypeUpdateRequest request) {
        var command = commandMapper.toUpdateCommand(slug, request);
        var updated = updateUseCase.execute(command);
        return ResponseEntity.ok(queryMapper.toResponse(updated));
    }
}
