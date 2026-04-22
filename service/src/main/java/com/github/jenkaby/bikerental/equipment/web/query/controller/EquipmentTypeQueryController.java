package com.github.jenkaby.bikerental.equipment.web.query.controller;

import com.github.jenkaby.bikerental.equipment.application.usecase.GetEquipmentTypesUseCase;
import com.github.jenkaby.bikerental.equipment.web.query.dto.EquipmentTypeResponse;
import com.github.jenkaby.bikerental.equipment.web.query.mapper.EquipmentTypeMapper;
import com.github.jenkaby.bikerental.shared.config.OpenApiConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(path = "/api/equipment-types", produces = {MediaType.APPLICATION_JSON_VALUE})
@Tag(name = OpenApiConfig.Tags.EQUIPMENT_TYPES)
public class EquipmentTypeQueryController {

    private final GetEquipmentTypesUseCase useCase;
    private final EquipmentTypeMapper mapper;

    EquipmentTypeQueryController(GetEquipmentTypesUseCase useCase, EquipmentTypeMapper mapper) {
        this.useCase = useCase;
        this.mapper = mapper;
    }

    @GetMapping
    @Operation(summary = "Get all equipment types")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Equipment types returned",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = EquipmentTypeResponse.class)))),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<EquipmentTypeResponse>> getAllEquipmentTypes() {
        log.info("[GET] Get all equipment types");
        var types = useCase.findAll();
        var responses = types.stream().map(mapper::toResponse).toList();
        return ResponseEntity.ok(responses);
    }
}
