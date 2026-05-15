package com.github.jenkaby.bikerental.equipment.web.command;

import com.github.jenkaby.bikerental.equipment.application.usecase.CreateEquipmentUseCase;
import com.github.jenkaby.bikerental.equipment.application.usecase.UpdateEquipmentUseCase;
import com.github.jenkaby.bikerental.equipment.web.command.dto.EquipmentRequest;
import com.github.jenkaby.bikerental.equipment.web.command.mapper.EquipmentCommandMapper;
import com.github.jenkaby.bikerental.equipment.web.query.dto.EquipmentResponse;
import com.github.jenkaby.bikerental.equipment.web.query.mapper.EquipmentQueryMapper;
import com.github.jenkaby.bikerental.shared.config.OpenApiConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(path = "/api/equipments", produces = {MediaType.APPLICATION_JSON_VALUE})
@Validated
@Tag(name = OpenApiConfig.Tags.EQUIPMENT)
public class EquipmentCommandController {

    private final CreateEquipmentUseCase createUseCase;
    private final UpdateEquipmentUseCase updateUseCase;
    private final EquipmentCommandMapper commandMapper;
    private final EquipmentQueryMapper queryMapper;

    EquipmentCommandController(CreateEquipmentUseCase createUseCase,
                               UpdateEquipmentUseCase updateUseCase,
                               EquipmentCommandMapper commandMapper,
                               EquipmentQueryMapper queryMapper) {
        this.createUseCase = createUseCase;
        this.updateUseCase = updateUseCase;
        this.commandMapper = commandMapper;
        this.queryMapper = queryMapper;
    }

    @PostMapping
    @Operation(summary = "Create equipment", description = "Registers a new piece of equipment in the catalog")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Equipment created",
                    content = @Content(schema = @Schema(implementation = EquipmentResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "Serial number already exists",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<EquipmentResponse> createEquipment(@Valid @RequestBody EquipmentRequest request) {
        log.info("[POST] Create equipment with uid {}", request.uid());
        log.debug("[POST] Create equipment {}", request);
        var command = commandMapper.toCreateCommand(request);
        var created = createUseCase.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(queryMapper.toResponse(created));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update equipment", description = "Replaces all fields of an existing equipment record")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Equipment updated",
                    content = @Content(schema = @Schema(implementation = EquipmentResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Equipment not found",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<EquipmentResponse> updateEquipment(
            @Parameter(description = "Equipment ID", example = "1") @PathVariable("id") Long id,
            @Valid @RequestBody EquipmentRequest request) {
        log.info("[PUT] Update equipment and id {}", id);
        log.debug("[PUT] Update equipment {}", request);
        var command = commandMapper.toUpdateCommand(id, request);
        var updated = updateUseCase.execute(command);
        return ResponseEntity.ok(queryMapper.toResponse(updated));
    }

//    TODO : implement patch method
//    @PatchMapping("/{id}")
//    public ResponseEntity<EquipmentResponse> patchEquipment(
//            @PathVariable("id") Long id,
//            @Valid @RequestBody EquipmentRequest request) {
//        var command = commandMapper.toUpdateCommand(id, request);
//        var updated = updateUseCase.execute(command);
//        return ResponseEntity.ok(queryMapper.toResponse(updated));
//    }
}
