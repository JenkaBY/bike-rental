package com.github.jenkaby.bikerental.equipment.web.query.controller;

import com.github.jenkaby.bikerental.equipment.application.usecase.GetEquipmentStatusesUseCase;
import com.github.jenkaby.bikerental.equipment.web.query.dto.EquipmentStatusResponse;
import com.github.jenkaby.bikerental.equipment.web.query.mapper.EquipmentStatusMapper;
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
@RequestMapping(path = "/api/equipment-statuses", produces = {MediaType.APPLICATION_JSON_VALUE})
@Tag(name = OpenApiConfig.Tags.EQUIPMENT_STATUSES)
public class EquipmentStatusQueryController {

    private final GetEquipmentStatusesUseCase useCase;
    private final EquipmentStatusMapper mapper;

    EquipmentStatusQueryController(GetEquipmentStatusesUseCase useCase, EquipmentStatusMapper mapper) {
        this.useCase = useCase;
        this.mapper = mapper;
    }

    @GetMapping
    @Operation(summary = "Get all equipment statuses", description = "Returns all statuses with their allowed transitions")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Equipment statuses returned",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = EquipmentStatusResponse.class))))
    })
    public ResponseEntity<List<EquipmentStatusResponse>> getAllEquipmentStatuses() {
        log.info("[GET] Get all equipment statuses");
        var statuses = useCase.findAll();
        var responses = statuses.stream().map(mapper::toResponse).toList();
        return ResponseEntity.ok(responses);
    }
}
